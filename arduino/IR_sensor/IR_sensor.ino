char dataString[50] = {0};
int a = 0, BUTTON = 8; 

void setup() {
  pinMode(BUTTON, INPUT);
  digitalWrite(BUTTON, HIGH);
  Serial.begin(9600);              //Starting serial communication
}

bool pressed = false;
int thing = 0;

void loop() {
  //Serial.println(digitalRead(BUTTON));
  if(digitalRead(BUTTON) == LOW) { //Low is pressed for buttons
    if(!pressed) {
      pressed = true;
      Serial.println(thing++);
    }
  } else {
    pressed = false;
  }
  
  delay(20);
}
