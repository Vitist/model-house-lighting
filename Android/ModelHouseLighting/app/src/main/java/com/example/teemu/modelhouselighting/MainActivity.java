package com.example.teemu.modelhouselighting;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_ENABLE_BT = 1;
    private boolean bluetoothEnabled = true;
    private BluetoothAdapter bluetoothAdapter;
    private final String BT_DEBUG_TAG = "MODELHOUSE/BT";
    private final String ON = "n";
    private final String OFF = "f";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final int MESSAGE_READ = 0;
    private final int MESSAGE_WRITE = 1;
    private final int MESSAGE_TOAST = 2;

    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "No bluetooth available", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothEnabled = false;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    // TODO: handle lifecycle onPause, onResume, etc... close and reopen socket?
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(BT_DEBUG_TAG, "onResume");
        // Establish connection here
        if (bluetoothEnabled) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.d(BT_DEBUG_TAG, deviceName + " " + deviceHardwareAddress);
                    if (deviceHardwareAddress.equals("20:16:06:07:68:96")) {
                        Log.d(BT_DEBUG_TAG, "HC-06 found");
                        connectThread = new ConnectThread(device);
                        connectThread.start();
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(BT_DEBUG_TAG, "onPause");
        // Close connection here
        connectThread.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            bluetoothEnabled = true;
        }
    }

    // TODO: create switches instead and write LED id as positive to turn on or negative to turn off
    public void onOnButtonClick(View view) {
        connectedThread.write(ON.getBytes());
    }

    public void onOffButtonClick(View view) {
        connectedThread.write(OFF.getBytes());
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(BT_DEBUG_TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                Thread.sleep(200);
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.e(BT_DEBUG_TAG, "Could not connect through the socket", connectException);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(BT_DEBUG_TAG, "Could not close the client socket", closeException);
                }
                return;
            } catch (InterruptedException interruptedException) {
                Log.e(BT_DEBUG_TAG, "Sleep interrupted", interruptedException);
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            Log.d(BT_DEBUG_TAG, "Connection to " + mmDevice.getName() + " successful");
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                connectedThread.cancel();
                mmSocket.close();
            } catch (IOException e) {
                Log.e(BT_DEBUG_TAG, "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream
        private StringBuilder stringBuilder = new StringBuilder();

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(BT_DEBUG_TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(BT_DEBUG_TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    String incomingMessage = new String(mmBuffer, 0, numBytes);

                    // Message could be read in multiple parts, keep appending messages until EOL
                    stringBuilder.append(incomingMessage);
                    int endOfLineIndex = stringBuilder.indexOf("\r\n");
                    if (endOfLineIndex > 0) {
                        String printMessage = stringBuilder.substring(0, endOfLineIndex);
                        stringBuilder.delete(0, stringBuilder.length());
                        Log.d(BT_DEBUG_TAG, printMessage);
                        // TODO: Send the obtained bytes to the UI activity using LocalBroadcast.
                    }
                } catch (IOException e) {
                    Log.e(BT_DEBUG_TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            Log.d(BT_DEBUG_TAG, "Sending data");
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                /*Message writtenMsg = handler.obtainMessage(
                        MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();*/
            } catch (IOException e) {
                Log.e(BT_DEBUG_TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                /*Message writeErrorMsg =
                        handler.obtainMessage(MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);*/
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmInStream.close();
                mmOutStream.close();
                mmSocket.close();
            } catch (IOException e) {
                Log.e(BT_DEBUG_TAG, "Could not close the connect socket", e);
            }
        }
    }
}
