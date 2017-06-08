#include <SoftwareSerial.h>

char dataString[50] = {0};
int a = 0, BUTTON = 8, DBUTT = 7, LED = 3; 

SoftwareSerial dylan(10, 11); //rx, tx

void setup() {
  pinMode(BUTTON, INPUT);
  pinMode(DBUTT, INPUT);
  pinMode(LED, OUTPUT);
  digitalWrite(BUTTON, HIGH);
  digitalWrite(DBUTT, HIGH);
  digitalWrite(LED, LOW);
  Serial.begin(9600);              //Starting serial communication
}

bool pressed = false;
bool pressed2 = false;

void loop() {
  
  if(digitalRead(BUTTON) == LOW) { //Low is pressed for buttons
    if(!pressed) {
      pressed = true;
      Serial.println(1);
    }
  } else {
    pressed = false;
  }  
  
  if(digitalRead(DBUTT) == LOW) {
    if(!pressed2) {
      pressed2 = true;
      Serial.println(2);
    }
  } else {
    pressed2 = false;
  }
  
  if (Serial.available() > 0) {
    String incoming = Serial.readString();
    
    if(incoming == "r") {
      digitalWrite(LED, HIGH);
      //Serial.println("Got your r!");
    } else {
      // send over to dylan
      //Serial.println(incoming);
      dylan.println(incoming);
    }
  }
  
  delay(20);
}

