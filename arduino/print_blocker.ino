#include <SoftwareSerial.h>
#include <Base64.h>
#include <string.h>
#include <EEPROM.h>

SoftwareSerial bt(6, 7);
SoftwareSerial datain(9, 10);
SoftwareSerial dataout(11, 12);

long baudrate = 9600;
int cnt = 0;

int maxcnt = 1000;
char data[1000];

void base64() {
  bt.begin(9600);
  delay(50);

  char es[4];
  char ss[3];
  int c = 0; int k;
  while (true) {
    ss[0] = data[c++]; k = 1;
    if (c < cnt) {ss[1] = data[c++]; k = 2;}
    if (c < cnt) {ss[2] = data[c++]; k = 3;}
    Base64.encode(es, ss, k);
    bt.write(es[0]);
    delay(1);
    bt.write(es[1]);
    delay(1);
    bt.write(es[2]);
    delay(1);
    bt.write(es[3]);
    delay(1);
    if (c == cnt) break;
  }
  bt.write('@\n');

  Serial.println("");
  Serial.println("base64 c");
  Serial.println(c);
  Serial.println(cnt);

  delay(100);
  
}

void setup() {
  Serial.begin(9600);
  dataout.begin(baudrate);
  bt.begin(9600);

  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(2, OUTPUT);
  pinMode(3, INPUT_PULLUP);
  pinMode(4, INPUT_PULLUP);
  pinMode(5, OUTPUT);

  byte b = EEPROM.read(0);
  if (b == 1) baudrate = 9600; else 
  if (b == 2) baudrate = 19200; else 
  if (b == 3) baudrate = 38400; else 
  if (b == 4) baudrate = 57600; else 
  if (b == 5) baudrate = 115200; 
}

void loop() {
  datain.begin(baudrate);
  delay(50);

  while(true) {
    if (datain.available()) {
      digitalWrite(2, HIGH);
      char a;
      while(true) {
        a = (char) datain.read();
        if (cnt < maxcnt) data[cnt] = a;
        cnt++;
        if (a == 'i') {
          Serial.println(cnt);
          buzz(2);
          loop2();
        }
        delay(1);
        if (!datain.available()) break;
      }
      digitalWrite(2, LOW);
      cnt = 0;
      break;
    }
    else {
      if (digitalRead(4) == LOW) {
        buzz(2);
        dataout.begin(baudrate);
        delay(50);
        dataout.write("BAUDRATE : ");
        dataout.print(baudrate);
        dataout.write("     ABC ");
        dataout.write(-80);
        dataout.write(-95);
        dataout.write(-77);
        dataout.write(-86);
        dataout.write(-76);
        dataout.write(-39);        
        dataout.write("\n\n\n\n\n\n\n\n\n");
        dataout.write(27);
        dataout.write('i');        
        delay(500);
        break;
      }
      else if (digitalRead(3) == LOW) {
        buzz(1);
        setbaud();
        break;
      }
    }
    delay(1);
  }
}

void loop2() {
  bt.begin(9600);
  delay(50);

  String input = "";
  while (1) {
    if (digitalRead(4) == LOW) {
      dataout.begin(baudrate);
      delay(50);
      for (int c = 0; c < cnt; c++) {
        if (c == maxcnt) break;
        dataout.write(data[c]);
      }
      bt.begin(9600);
      delay(50);
    }

    input = "";
    while (bt.available()) {
      input += (char) bt.read();
      delay(1);
    }
    if (input != "") Serial.println(input);
    if (input == "tag") {
      buzz(1);
      base64();
    }
    else if (input == "fine") {
      buzz(1);
      return;
    }
    else if (input == "print") {
      dataout.begin(baudrate);
      delay(50);
      for (int c = 0; c < cnt; c++) {
        if (c == maxcnt) break;
        dataout.write(data[c]);
      }
      bt.begin(9600);
      delay(50);
    }
    else if (input == "error") {
      buzz(3);
    }
    else if (input == "cancel") {
      buzz(2);
      return;
    }
    else if (input == "buzz") {
      buzz(3);
    }
  }
}

void setbaud() {
    if (baudrate == 115200) {
      baudrate = 9600;
      EEPROM.write(0, 1);
    }
    else if (baudrate == 9600) {
      baudrate = 19200;
      EEPROM.write(0, 2);
    }
    else if (baudrate == 19200) {
      baudrate = 38400;
      EEPROM.write(0, 3);
    }
    else if (baudrate == 38400) {
      baudrate = 57600;
      EEPROM.write(0, 4);
    }
    else if (baudrate == 57600) {
      baudrate = 115200;
      EEPROM.write(0, 5);
    }
    dataout.begin(baudrate);
    delay(50);
    dataout.write("BAUDRATE : ");
    dataout.print(baudrate);
    dataout.write("     ABC ");
    dataout.write(-80);
    dataout.write(-95);
    dataout.write(-77);
    dataout.write(-86);
    dataout.write(-76);
    dataout.write(-39);        
    dataout.write("\n\n\n\n\n\n\n\n\n");
    delay(500);
}


void buzz(int c) {
  digitalWrite(5, HIGH);
  delay(100);
  digitalWrite(5, LOW);
  if (c == 1) return;
  delay(50);
  digitalWrite(5, HIGH);
  delay(100);
  digitalWrite(5, LOW);
  if (c == 2) return;
  delay(50);
  digitalWrite(5, HIGH);
  delay(100);
  digitalWrite(5, LOW);
}
