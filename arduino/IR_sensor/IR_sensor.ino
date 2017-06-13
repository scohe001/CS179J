char dataString[50] = {0};
int a = 0, BUTTON = 8; 

void setup() {
  pinMode(BUTTON, INPUT);
  pinMode(7, INPUT);
  digitalWrite(7, HIGH);
  digitalWrite(BUTTON, HIGH);
  Serial.begin(9600);              //Starting serial communication
}

bool pressed = false;
bool pressed2 = false;

void loop() {
  //Serial.println(digitalRead(BUTTON));
  if(digitalRead(BUTTON) == LOW) { //Low is pressed for buttons
    if(!pressed) {
      pressed = true;
      Serial.println(1);
    }
  } else {
    pressed = false;
  }
  
  if(digitalRead(7) == LOW) {
    if(!pressed2) {
      pressed2 = true;
      Serial.println(2);
    }
  } else {
    pressed2 = false;
  }
  
  if (Serial.available() > 0) {
    String incoming = Serial.readString();
    
    // say what you got:
    Serial.print("I received: ");
    Serial.println(incoming);
  }
  
  delay(20);
}

