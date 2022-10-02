#include <LiquidCrystal_I2C.h>

#include <ESP8266WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <time.h>
#include "secrets.h"
#include <stdio.h>
#include <string.h>


#include <DNSServer.h>
#include <ESP8266WebServer.h >
#include <WiFiManager.h>

 
/*void wifiManager()
{
  // Creamos una instancia de la clase WiFiManager
  WiFiManager wifiManager;
 
  // Descomentar para resetear configuración
  wifiManager.resetSettings();
 
  // Cremos AP y portal cautivo
  wifiManager.autoConnect("ESP8266Temp");
 
  Serial.println("Ya estás conectado");
}*/


LiquidCrystal_I2C lcd(0x27, 16, 2);

int pinBomba=14;
int sensor_pin = A0;
int valor_humedad;
int bombaEncendida=0;

int tipoRiego=0; //0=Manual - 1=Automatico
int tiempoRiego=15; //Segundos
int tiempoEnvioMensajes=10; //Segundos
int enviarNotificaciones=0; // 0=No - 1=Si 
int humedadMinima=25; // Rango de 0 a 100 --> 0 es que no hay nada de humedad
char nombreSensor[100];
int tiempoEntreNotificaciones=180; //Segundos
int cantidadNotificaciones=0;

unsigned long lastRiego = 0;
unsigned long lastNotificacion = 0;
unsigned long lastMillis = 0;
unsigned long previousMillis = 0;
//const long interval = 5000;
 
#define AWS_IOT_PUBLISH_TOPIC   "esp8266/pub"
#define AWS_IOT_PUBLISH_NOT_TOPIC   "esp8266/not"
#define AWS_IOT_SUBSCRIBE_TOPIC "esp8266/sub"
#define AWS_IOT_SUBSCRIBE_TOPIC_RIEGO "esp8266/riego"
 
WiFiClientSecure net;

 
BearSSL::X509List cert(cacert);
BearSSL::X509List client_crt(client_cert);
BearSSL::PrivateKey key(privkey);
 
PubSubClient client(net);
 
time_t now;
time_t nowish = 1510592825;
 
struct tm timeinfo;
 
void NTPConnect(void)
{
  Serial.print("Setting time using SNTP");
  configTime(TIME_ZONE * 3600, 0 * 3600, "pool.ntp.org", "time.nist.gov");
  now = time(nullptr);
  while (now < nowish)
  {
    delay(500);
    Serial.print(".");
    now = time(nullptr);
  }
  Serial.println("done!");
  
  gmtime_r(&now, &timeinfo);
  Serial.print("Current time: ");
  Serial.print(asctime(&timeinfo));
}
 
  
void messageReceived(char *topic, byte *payload, unsigned int length)
{
  Serial.print("Mensaje recibido en el topico: [");
  Serial.print(topic);
  Serial.print("]: ");
  
  DynamicJsonDocument doc(1024);
  deserializeJson(doc, payload);

  if (strcmp(topic,AWS_IOT_SUBSCRIBE_TOPIC_RIEGO)==0){    
    //Se enciende la bomba
    if (bombaEncendida==0){
      Serial.print("Riego manual: Se encendió la bomba: ");
      Serial.print(tiempoRiego); 
      Serial.print(" segundos");
      Serial.print("%\n"); 
      const char* sensor = doc["nombre"];
      const char* accion = doc["accion"];
      Serial.print("Sensor: ");
      Serial.print(sensor);
      Serial.print(" Accion: ");
      Serial.print(accion);
      Serial.print("%\n"); 
      digitalWrite(pinBomba, 1);
      bombaEncendida=1;
      lastRiego = millis();
      // Limpiar
      lcd.clear();
      // Imprimir
      lcd.setCursor(0, 0);
      lcd.print("Riego manual...");
      lcd.setCursor(0, 1); // Segunda fila
      lcd.print("Segundos:");
      lcd.print(tiempoRiego);
    }
  }
  else if (strcmp(topic,AWS_IOT_SUBSCRIBE_TOPIC)==0) {
    const char* sensor = doc["nombre"];
    //nombreSensor = sensor;
    tipoRiego = doc["tipoRiego"];
    tiempoRiego=doc["tiempoRiego"];
    tiempoEnvioMensajes=doc["tiempoEnvioMensaje"];
    enviarNotificaciones=doc["notificaciones"];
    humedadMinima=doc["humedadMinima"];
    
    Serial.print(F("Response:"));
    Serial.println(sensor);
    Serial.println(tiempoRiego);
    Serial.println(tiempoEnvioMensajes);
    Serial.println(enviarNotificaciones);
    Serial.println(humedadMinima);
  
    // Limpiar
    lcd.clear();
    // Imprimir
    lcd.setCursor(0, 0);
    lcd.print("Recibiendo conf.");
    lcd.setCursor(0, 1); // Segunda fila
    lcd.print(sensor);
  }
}
 
 
void connectAWS()
{
  /*delay(3000);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);//probar a comentar
  Serial.println(String("Attempting to connect to SSID: ") + String(WIFI_SSID));
  
  while (WiFi.status() != WL_CONNECTED){
    Serial.print(".");
    delay(1000);
  }*/
  NTPConnect();
  net.setTrustAnchors(&cert);
  net.setClientRSACert(&client_crt, &key);
  client.setServer(MQTT_HOST, 8883);
  client.setCallback(messageReceived);
  client.setBufferSize(1024);
  // Limpiar
  lcd.clear();
  // Imprimir
  lcd.setCursor(0, 0);
  lcd.print("Conectando...");
  lcd.setCursor(0, 1); // Segunda fila
  lcd.print("AWS IOT");
  Serial.println("Connecting to AWS IOT");
  while (!client.connect(THINGNAME)){
    Serial.print(".");
    delay(1000);
  }
  if (!client.connected()) {
    // Limpiar
    lcd.clear();
    // Imprimir
    lcd.setCursor(0, 0);
    lcd.print("TimeOut...");
    lcd.setCursor(0, 1); // Segunda fila
    lcd.print("AWS IOT");
    Serial.println("AWS IoT Timeout!");
    return;
  }
 
  client.subscribe(AWS_IOT_SUBSCRIBE_TOPIC_RIEGO);
  Serial.print("AWS IoT Connected: ");
  Serial.println(AWS_IOT_SUBSCRIBE_TOPIC_RIEGO);
  // Limpiar
  lcd.clear();
  // Imprimir
  lcd.setCursor(0, 0);
  lcd.print("Conexion AWS OK");
  lcd.setCursor(0, 1); // Segunda fila
  lcd.print(AWS_IOT_SUBSCRIBE_TOPIC_RIEGO);
  delay(2000);

  // Subscribe to a topic
  client.subscribe(AWS_IOT_SUBSCRIBE_TOPIC);
  Serial.print("AWS IoT Connected: ");
  Serial.println(AWS_IOT_SUBSCRIBE_TOPIC);
  // Limpiar
  lcd.clear();
  // Imprimir
  lcd.setCursor(0, 0);
  lcd.print("Conexion AWS OK");
  lcd.setCursor(0, 1); // Segunda fila
  lcd.print(AWS_IOT_SUBSCRIBE_TOPIC);
  delay(2000);
}
 
 
void publishMessage()
{
  StaticJsonDocument<200> doc;
  doc["humedad"] = valor_humedad;
  /*doc["fecha"] = str;*/
  doc["sensor"] = 1;
  char jsonBuffer[512];
  serializeJson(doc, jsonBuffer); // print to client
  client.publish(AWS_IOT_PUBLISH_TOPIC, jsonBuffer);
}

 
void setup(){
  Serial.begin(115200);
  // Iniciar LCD
  lcd.begin(16, 2);
  lcd.init();
  lcd.backlight(); // Agregar brillo
  // Limpiar
  lcd.clear();
  // Imprimir
  lcd.setCursor(0, 0);
  lcd.print("Sistema de Riego");
  lcd.setCursor(0, 1); // Segunda fila
  lcd.print("Fernando Perez");
  delay(2000);

  
  // Limpiar
  lcd.clear();
  // Imprimir
  lcd.setCursor(0, 0);
  lcd.print("Conectate a red:");
  lcd.setCursor(0, 1); // Segunda fila
  lcd.print("SistemaRiego"); //probar a comentar
   // inserte lo de wifimanager
  // Creamos una instancia de la clase WiFiManager
  WiFiManager wifiManager;
 
  // Descomentar para resetear configuración
  //wifiManager.resetSettings();
 
  // Cremos AP y portal cautivo
  wifiManager.autoConnect("SistemaRiego");
 
  Serial.println("Ya estás conectado");


   // Limpiar
  lcd.clear();
  // Imprimir
  lcd.setCursor(0, 0);
  lcd.print("Conexion Wifi");
  lcd.setCursor(0, 1); // Segunda fila
  lcd.print("OK");

  delay(2000);
  
  connectAWS();
  pinMode(pinBomba,OUTPUT);
  //dht.begin();
  strcpy(nombreSensor, "Sensor 1");
 
}


void loop(){
  //verificando si hay conexión con AWS
  now = time(nullptr); 
  if (!client.connected()){
    connectAWS();
  }else{
    client.loop();
  }
  //Validando si hay que apagar la bomba porque se cumplio su tiempo de riego
  //if (millis() - lastRiego > (tiempoRiego*1000)){
  if((valor_humedad>humedadMinima) ||  (millis() - lastRiego > (tiempoRiego*1000))){
     //Se enciende la bomba
     if (bombaEncendida==1){
      Serial.print("Se apagó la bomba, estuvo encendida por: ");
      Serial.print((millis() - lastRiego)/1000); 
      Serial.print(" segundos");
      Serial.print("%\n"); 
      digitalWrite(pinBomba, 0);
      bombaEncendida=0;
      // Limpiar
      lcd.clear();
      // Imprimir
      lcd.setCursor(0, 0);
      lcd.print("Riego apagado");
      lcd.setCursor(0, 1); // Segunda fila
      lcd.print("Segundos: ");
      lcd.print((millis() - lastRiego)/1000);
    }
  }
  
  //Leyendo valor de humedad del sensor cada cierto tiempo definido por: tiempoEnvioMensajes
  if (millis() - lastMillis > (tiempoEnvioMensajes*1000)){
    lastMillis = millis();
    valor_humedad = analogRead(sensor_pin);
    valor_humedad = map(valor_humedad,1024,425,0,100); 
    Serial.print("Moisture : "); 
    Serial.print(valor_humedad);
    Serial.print("%\n");  
    // Limpiar
    lcd.clear();
    // Imprimir
    lcd.setCursor(0, 0);
    lcd.print("Humedad:");
    //lcd.setCursor(0, 1); // Segunda fila
    lcd.print(valor_humedad);
    if (isnan(valor_humedad)){  // Check if any reads failed and exit early (to try again).
      Serial.println(F("Fallo la lectura del sensor de humedad!"));
      return;
    }
    //Publicando mensaje con la lectura de humedad en AWS
    publishMessage();
    //Verificando si se alcanzó la humedad mínima
    if(valor_humedad<=humedadMinima){
        lcd.setCursor(0, 1); // Segunda fila
        if (bombaEncendida==1){
          lcd.print("Regando...");     
        }else{
          lcd.print("Humedad min:");
          lcd.print(humedadMinima);
        }
        Serial.print("Se ha alcanzado la humedad minima");
        Serial.print("%\n");  
        StaticJsonDocument<200> doc;
        if(tipoRiego==0){
          Serial.print("Riego manual");
          Serial.print("%\n");  
          doc["Mensaje"] = "Se alcanzó la humedad mínima, el riego está en modo manual y puede iniciarlo desde el sitio web";  
          doc["HumedadMinima"] = humedadMinima;
          doc["ValorHumedad"] = valor_humedad;
          doc["TiempoRiego"] = tiempoRiego;
        }
        else{
          Serial.print("Riego automático");
          Serial.print("%\n");  
          doc["Mensaje"] = "Se alcanzó la humedad mínima, el riego está en modo automático y se inició el riego";
          doc["HumedadMinima"] = humedadMinima;
          doc["ValorHumedad"] = valor_humedad;
          doc["TiempoRiego"] = tiempoRiego; 
          //Se enciende la bomba
          if (bombaEncendida==0){
            Serial.print("Se encendió la bomba por: ");
            Serial.print(tiempoRiego); 
            Serial.print(" segundos");
            Serial.print("%\n"); 
            digitalWrite(pinBomba, 1);
            bombaEncendida=1;
            lastRiego = millis();
            // Limpiar
            lcd.clear();
            // Imprimir
            lcd.setCursor(0, 0);
            lcd.print("Inicio Riego:");
            lcd.setCursor(0, 1); // Segunda fila
            lcd.print("Segundos:");
            lcd.print(tiempoRiego);
          }
        }
        doc["Sensor"] = nombreSensor;
        //Se valida si están activas las notificaciones
        if(enviarNotificaciones==1){
          //Se valida hace cuanto tiempo se envió la última notificación (Esto para no enviar muchas notificaciones seguidas)
          if ((millis() - lastNotificacion > (tiempoEntreNotificaciones*1000)) || (cantidadNotificaciones==0)){
            cantidadNotificaciones++;
            lastNotificacion = millis();
            Serial.print("Notificaciones encendidas, se envía notificación");
            Serial.print("%\n");  
            char jsonBuffer[512];
            serializeJson(doc, jsonBuffer); // print to client
            client.publish(AWS_IOT_PUBLISH_NOT_TOPIC, jsonBuffer);
          }
          else{
            Serial.print("No se envía nueva notificación ya que la última se envió hace menos de ");
            Serial.print(tiempoEntreNotificaciones);
            Serial.print(" segundos");
            Serial.print("%\n");  
          }
        }
        else{
            Serial.print("Notificaciones apagadas");
            Serial.print("%\n");  
        }        
      }else{
        Serial.print("No se ha alcanzado la humedad minima");
        Serial.print("%\n");  
      }
  }
}
    
