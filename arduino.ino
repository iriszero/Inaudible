#include <string.h>

char text[] = "Jaeseok Huh";
//char text[] = "U";

int piezoPin = 8;
bool bits[12*8+5];
int freq[3] = {19000, 20000, 21000};

void setup() {
  Serial.begin(9600);
  // put your setup code here, to run once:
  for (int i=0; i<strlen(text); i++) {
    for (int j=0; j<8; j++) {
      bits[i * 8 + j] = int(text[i]) & (1 << (7-j));
    }
  }
  
}

void loop() {
  // put your main code here, to run repeatedly:
  int code = 0;
  Serial.println(strlen(text));
  for (int i=0; i<strlen(text); i++) {
    
    for (int j=0; j<8; j++) {
      int now_idx = i * 8+ j;
      if (bits[now_idx] == false) {
        code = (code + 1) % 3;
      } else {
        code = (code -1 +3) %3;
      }
      Serial.print(code);
      
      tone(piezoPin, freq[code], 25);
      delay(120);
    }
  }
  Serial.println();
  delay(5000);
  
  //tone(piezoPin, 20000, 10000);
}
