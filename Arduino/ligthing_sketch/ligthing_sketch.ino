// Lighting system for a model house including ceiling lamps and a fireplace

// White LED pins
const int led1 = 2;
const int led2 = 3;
const int led3 = 4;
const int led4 = 5;
const int led5 = 6;
const int led6 = 7;
const int led7 = 8;
const int led8 = 12;
// RGB fireplace LED pins
const int ledRed = 9;
const int ledBlue = 10;
const int ledGreen = 11;
// Green light config
const int greenMax = 235;
const int greenMin = 175;
int greenTarget = greenMin;
int greenCurrent = greenMin;
int greenFadeAmount = 0;
// Red light config
const int redMax = 50;
const int redMin = 0;
int redTarget = redMin;
int redCurrent = redMin;
int redFadeAmount = 0;
// Fire effect animation speed
int effectSmoothing = 5;
int counter = 0;


// The setup routine runs once when you press reset:
void setup() {
  randomSeed(analogRead(0));
  
  // Declare pin 8 to be an output and turn the LED on
  pinMode(led1, OUTPUT);
  pinMode(led2, OUTPUT);
  pinMode(led3, OUTPUT);
  pinMode(led4, OUTPUT);
  pinMode(led5, OUTPUT);
  pinMode(led6, OUTPUT);
  pinMode(led7, OUTPUT);
  pinMode(led8, OUTPUT);
  digitalWrite(led1, HIGH);
  digitalWrite(led2, HIGH);
  digitalWrite(led3, HIGH);
  digitalWrite(led4, HIGH);
  digitalWrite(led5, HIGH);
  digitalWrite(led6, HIGH);
  digitalWrite(led7, HIGH);
  digitalWrite(led8, HIGH);

  // Declare pins 9-11 as output for RGB LED
  pinMode(ledRed, OUTPUT);
  pinMode(ledGreen, OUTPUT);
  pinMode(ledBlue, OUTPUT);

  // Set red and green values 0 = Max, 255 = Off
  analogWrite(ledRed, redMin);
  analogWrite(ledBlue, 250);
  analogWrite(ledGreen, greenMin);
}

// The loop routine runs over and over again forever:
void loop() {
  // Set the brightness of the RGB LED:
  /*analogWrite(led_g, brightness);
  
  // change the brightness for next time through the loop:
  brightness = brightness + fadeAmount;

  // reverse the direction of the fading at the ends of the fade:
  if (brightness <= greenMin || brightness >= greenMax) {
    fadeAmount = -fadeAmount;
  }*/

  // Reset fire effect animation after it has finished
  if(counter % effectSmoothing == effectSmoothing - 1) {
    counter = 0;
    
    // Set old target as starting value
    redCurrent = redTarget;
    greenCurrent = greenTarget;
    
    // Pick new target from the allowed value range
    redTarget = random(redMin, redMax);
    greenTarget = random(greenMin, greenMax);
    
    // Calculate animation step size
    redFadeAmount = (redTarget - redCurrent) / effectSmoothing;
    greenFadeAmount = (greenTarget - greenCurrent) / effectSmoothing;
  }

  // Update LEDs
  analogWrite(ledRed, redCurrent);
  analogWrite(ledGreen, greenCurrent);

  // Calculate the next value for the LEDs
  redCurrent = redCurrent + redFadeAmount;
  // Overflow protection
  if(redCurrent < 0) {
    redCurrent = 0;
  }
  greenCurrent = greenCurrent + greenFadeAmount;

  ++counter;
  
  // wait for 10-50 milliseconds until the next animation step
  delay(random(10, 50));
}
