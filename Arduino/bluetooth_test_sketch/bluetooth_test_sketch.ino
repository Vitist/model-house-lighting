signed char blueToothVal;           //value sent over via bluetooth
signed char lastValue;              //stores last state of device (on/off)
 
void setup()
{
 Serial.begin(9600); 
 pinMode(13,OUTPUT);
}
 
 
void loop()
{
  if(Serial.available())
  {//if there is data being recieved
    blueToothVal=Serial.read(); //read it
  }
  switch(abs(blueToothVal)) {
    case 1:
      digitalWrite(13, getLEDNewState(blueToothVal));
      break;
    case 2:
      break;
    case 3:
      break;
    case 4:
      break;
    case 5:
      break;
    case 6:
      break;
    case 7:
      break;
    case 8:
      break;
    case 9:
      break;
  }
  if (blueToothVal=='n')
  {//if value from bluetooth serial is n
    digitalWrite(13,HIGH);            //switch on LED
    if (lastValue!='n')
      Serial.println(F("LED is on")); //print LED is on
    lastValue=blueToothVal;
  }
  else if (blueToothVal=='f')
  {//if value from bluetooth serial is n
    digitalWrite(13,LOW);             //turn off LED
    if (lastValue!='f')
      Serial.println(F("LED is off")); //print LED is on
    lastValue=blueToothVal;
  }
  delay(1000);
}

int getLEDNewState(char newState) {
  // Check if first bit it set
  if (newState & (1 << 7)) {
    // Bit is set -> negative number
    return LOW;
  }
  else {
    // Bit isn't set -> positive number
    return HIGH;
  }
}
