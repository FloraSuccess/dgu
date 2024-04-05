#include<SoftwareSerial.h>
#include<Wire.h>
#include <LiquidCrystal_I2C.h> 
#include<dht.h>
SoftwareSerial sim800l(9,8);
LiquidCrystal_I2C lcd(0x27,16,2); 

dht DHT_11;
#define DHT_Pin A0 

char Received_SMS;
short natija_OK=-1;
short xat=-1;
short tel=-1;
short telefon=-1;
short telsms=-1;
String Data_SMS;

float Humidity;
float Temperature;
float T_namlik;
int Time=0;

void setup()
{
  lcd.init();                       
  lcd.backlight();                 
  lcd.setCursor(0,0);              
  lcd.print("TATU FF Magistri");       
  lcd.setCursor(0,1);              
  lcd.print("Alimjonova A.");       
  
  pinMode(A1,INPUT);
  
  sim800l.begin(9600); //
  Serial.begin(9600);  //

  Serial.println("Starting ...");//
  delay(3000);//
  sim800l.println("AT+DDET=1,0,0"); 
  delay(1000);
  ReceiveMode();
}

void loop(){
  String RSMS;
  while(sim800l.available()>0){
    Received_SMS=sim800l.read();
    Serial.print(Received_SMS);
    RSMS.concat(Received_SMS);
    
    xat=RSMS.indexOf("+998916557705");  //nomer un
    if(xat!=-1){natija_OK=RSMS.indexOf("Data");}

    tel=RSMS.indexOf("+CLIP:");
    if(tel!=-1){
      telefon=RSMS.indexOf("+998908448646");
      if(telefon!=-1){
        Telol(); 
        ReceiveMode();
        telefon=-1;
      }
    }
    telsms=RSMS.indexOf("+DTMF:");
    if(telsms!=-1){natija_OK=RSMS.indexOf("0");}      
  }
    
if(natija_OK!=-1){
  Data_SMS = "Tarmoqga ulangan\n Sersorlar ishlayabdi\nHavo xarorati: " +String(Temperature,1)+ "\nHavo namligi: " +String(Humidity,1)+ "\nTuproq namligi: " +String(T_namlik,1); 
  Send_Data();
  ReceiveMode();
  natija_OK=-1;
  }

  if(Time == 2000){
    LCD();
    Time = 0;
  }
  
  //Serialcom();

  delay(1);
  Time++;
}

void Serialcom(){
  delay(500);
  while(Serial.available()){sim800l.write(Serial.read());}
  while(sim800l.available()){Serial.write(sim800l.read());}
}

void ReceiveMode(){
  sim800l.println("AT");
  Serialcom();
  sim800l.println("AT+CMGF=1");
  Serialcom();
  sim800l.println("AT+CLIP=1");
  Serialcom();
  sim800l.println("AT+CNMI=2,2,0,0,0");
  Serialcom(); 
}

void Send_Data(){
  Serial.println("Sending Data...");
  sim800l.print("AT+CMGF=1\r");
  delay(100);
  sim800l.print("AT+CMGS=\"+998908448646\"\r");
  delay(500);
  sim800l.print(Data_SMS);
  delay(500);
  sim800l.print((char)26);
  delay(500);
  sim800l.println();
  Serial.println("Data Sent.");
  delay(500);
 }
 
 void Telol(){
  sim800l.println("ATA");
  delay(500);
  sim800l.print((char)26);
  delay(500);
  sim800l.println("AT+DDET=1,0,0"); 
  delay(500);
  }
 
void Teloch(){
  sim800l.println("ATH0");
  delay(100);
  sim800l.print((char)26);
  delay(100);
 }

void DHT_Sensor(){
  int chk = DHT_11.read11(DHT_Pin);
  Humidity = DHT_11.humidity;
  Temperature = DHT_11.temperature;

  T_namlik = analogRead(A1);
  T_namlik = map(T_namlik,1023,0,0,100);
  
  Serial.print(Temperature);
  Serial.print("C.  ");
  Serial.print(Humidity);
  Serial.print("%.   ");
  Serial.print(T_namlik);
  Serial.println("~%.");
}

void LCD(){
  DHT_Sensor();
  lcd.setCursor(0,0);
  lcd.print(Temperature);
  lcd.setCursor(2,0);
  lcd.print("C ");
  lcd.setCursor(4,0);
  lcd.print(Humidity);
  lcd.setCursor(6,0);
  lcd.print("% "); 
  lcd.setCursor(8,0);
  lcd.print(T_namlik); 
  lcd.print("_%       ");
  lcd.setCursor(0,1);
  if(T_namlik <= 15){lcd.print("Ground is dry  ");}
  else{lcd.print("Ground is wet  ");}
}
