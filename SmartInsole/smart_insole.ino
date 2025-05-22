#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include "I2Cdev.h"
#include "MPU6050_6Axis_MotionApps20.h"
#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
    #include "Wire.h"
#endif



// --- UUIDs (기존과 동일) ---
#define SERVICE_UUID        "5fdc7093-3882-416b-bc2b-eb76a749aef3"
#define CHARACTERISTIC_UUID_SENSOR "9b836621-335d-4f1b-9cf5-a898b283beb6"

// --- 핀 정의 (기존과 동일) ---
#define FSR_PIN1 2 //엄지발가락 아래
#define FSR_PIN2 20 //새끼 발가락 아래
#define FSR_PIN3 1 //발 뒷꿈치
#define FSR_PIN4 21 //디지털 핀, 아치 안쪽
#define FSR_PIN5 18 //디지털 핀, 아치 바깥측
#define INTERRUPT_PIN 16

// #define FSR_WAKEUP_PIN_NUM 1 

// #define FSR_PIN1 2 //엄지발가락 아래
// #define FSR_PIN2 1 //새끼 발가락 아래
// #define FSR_PIN3 0 //발 뒷꿈치
// #define FSR_PIN4 21 //디지털 핀, 아치 안쪽
// #define FSR_PIN5 18 //디지털 핀, 아치 바깥측
// #define INTERRUPT_PIN 16

// #define FSR_WAKEUP_PIN_NUM 0
// --- MPU6050 관련 변수 (기존과 동일) ---
MPU6050 mpu;
bool dmpReady = false;
uint8_t mpuIntStatus;
uint8_t devStatus;
uint16_t packetSize;
uint16_t fifoCount;
uint8_t fifoBuffer[64];
Quaternion q;
VectorFloat gravity;
float ypr[3]; // Yaw, Pitch, Roll 저장용

// --- 인터럽트 플래그 (기존과 동일) ---
volatile bool mpuInterrupt = false;
void dmpDataReady() {
    mpuInterrupt = true;
}

// --- BLE 관련 변수 (기존과 동일) ---
BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristicSensor = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;

// --- 타이밍 및 데이터 구조 ---
unsigned long lastUpdateTime = 0;
const long updateInterval = 100; // 센싱 및 전송 간격 (100ms = 10Hz) - 필요에 따라 조절

unsigned long lastDisconnectTime = 0;      // 마지막으로 연결이 끊긴 시간
const long deepSleepTimeout = 15000;

// 데이터 구조체 정의: FSR 5개 + YPR 3개
struct SensorData {
  int fsr[5];
  float ypr[3]; // Yaw, Pitch, Roll
};
SensorData combinedData; // 전송할 데이터를 담을 구조체 변수

bool isAwake = false;

// --- BLE 콜백 ---
class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      oldDeviceConnected = true;
      Serial.println("Device Connected");
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      Serial.println("Device Disconnected");
      lastDisconnectTime = millis();
      pServer->getAdvertising()->start();
      Serial.println("Start advertising");
    }
};

// --- setupBLE() 함수 (기존과 동일) ---
void setupBLE(){
  Serial.printf("Size of SensorData struct: %d bytes\n", sizeof(combinedData)); // 구조체 크기 확인
  BLEDevice::init("RunMate_SmartInsole_L");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  BLEService *pService = pServer->createService(SERVICE_UUID);
  pCharacteristicSensor = pService->createCharacteristic(
                      CHARACTERISTIC_UUID_SENSOR,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_NOTIFY
                    );    
  pCharacteristicSensor->addDescriptor(new BLE2902());    
  pService->start();
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");
}

// --- setupMPU6050() 함수 (기존과 동일 - #define OUTPUT_READABLE_YAWPITCHROLL 필요) ---
#define OUTPUT_READABLE_YAWPITCHROLL // ypr 출력을 위해 필요
void setupMPU6050(){
  mpu.initialize();
  pinMode(INTERRUPT_PIN, INPUT_PULLUP);
  Serial.println(F("Testing device connections..."));
  Serial.println(mpu.testConnection() ? F("MPU6050 connection successful") : F("MPU6050 connection failed"));
  devStatus = mpu.dmpInitialize();

  // 오프셋 설정 (기존과 동일 - 필요시 조정)
  mpu.setXGyroOffset(220);
  mpu.setYGyroOffset(76);
  mpu.setZGyroOffset(-85);
  mpu.setZAccelOffset(1788);

  if (devStatus == 0) {
      mpu.CalibrateAccel(6);
      mpu.CalibrateGyro(6);
      mpu.PrintActiveOffsets();
      Serial.println(F("Enabling DMP..."));
      mpu.setDMPEnabled(true);
      Serial.print(F("Enabling interrupt detection (Arduino external interrupt "));
      Serial.print(digitalPinToInterrupt(INTERRUPT_PIN));
      Serial.println(F(")..."));
      attachInterrupt(digitalPinToInterrupt(INTERRUPT_PIN), dmpDataReady, RISING);
      mpuIntStatus = mpu.getIntStatus();
      Serial.println(F("DMP ready! Waiting for first interrupt..."));
      dmpReady = true;
      packetSize = mpu.dmpGetFIFOPacketSize();
  } else {
      Serial.print(F("DMP Initialization failed (code "));
      Serial.print(devStatus);
      Serial.println(F(")"));
  }
}

// void goToDeepSleep() {
//     Serial.println("Configuring FSR wakeup and entering deep sleep...");
//     Serial.flush(); // 시리얼 메시지 모두 전송
//     esp_sleep_enable_ext1_wakeup(BIT(FSR_WAKEUP_PIN_NUM), ESP_EXT1_WAKEUP_ANY_HIGH);
//     esp_deep_sleep_start();
// }


// --- setup() 함수 (기존과 동일) ---
void setup() {

  Serial.begin(115200);
  Serial.println("Smart Insole Booting Up...");

  // esp_sleep_wakeup_cause_t wakeup_reason;
  // wakeup_reason = esp_sleep_get_wakeup_cause();

  // if (wakeup_reason == ESP_SLEEP_WAKEUP_EXT1) {
    Serial.println("Woke up by FSR press!");
    isAwake = true;

    pinMode(FSR_PIN4, INPUT); // 디지털 핀 모드 설정
    pinMode(FSR_PIN5, INPUT);
    #if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
      Wire.begin();
      Wire.setClock(400000);
    #elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
        Fastwire::setup(400, true);
    #endif

    setupMPU6050();
    setupBLE();
    
    // 초기 YPR 값을 0으로 설정 (선택 사항)
    combinedData.ypr[0] = 0.0f;
    combinedData.ypr[1] = 0.0f;
    combinedData.ypr[2] = 0.0f;
  // }else{
  //   Serial.println("Initial boot or other wakeup reason. Going to deep sleep.");
  //   goToDeepSleep();
  // }

  delay(100);
}

// --- printFsrValue 함수 (디버깅용 - 기존과 동일) ---
void printFsrValue(int num, int value){
  Serial.print("FSR");
  Serial.print(num);
  Serial.print(": ");
  Serial.print(value);
  Serial.print(" | ");
}

// --- loop() 함수 (수정됨) ---
void loop() {
  // if (!isAwake) {
  //   delay(1000); 
  //   return;
  // }

  // --- DMP 데이터 처리 ---
  //DMP 준비 안됐거나 인터럽트 없으면 스킵
  if (!dmpReady) return;

  // 인터럽트 발생 시 FIFO에서 데이터 읽기
  if (mpuInterrupt && fifoCount < packetSize) {
     fifoCount = mpu.getFIFOCount(); // 큐의 데이터 개수 확인
  }


  if (mpuInterrupt || fifoCount >= packetSize) { // 인터럽트 발생했거나, 처리 못한 데이터가 쌓여있으면
      mpuInterrupt = false; // 플래그 리셋
      mpuIntStatus = mpu.getIntStatus(); // MPU 인터럽트 상태 읽기

      fifoCount = mpu.getFIFOCount(); // 큐의 데이터 개수 확인

      // FIFO 오버플로우 확인
      if ((mpuIntStatus & _BV(MPU6050_INTERRUPT_FIFO_OFLOW_BIT)) || fifoCount >= 1024) {
          mpu.resetFIFO(); // FIFO 리셋
          fifoCount = mpu.getFIFOCount(); // 리셋 후 카운트 다시 확인
          // Serial.println(F("FIFO overflow!"));
      }
      // 데이터 준비 완료 인터럽트 확인 (DMP 데이터 준비됨)
      else if (mpuIntStatus & _BV(MPU6050_INTERRUPT_DMP_INT_BIT)) {
          // 필요한 만큼 데이터 읽기 (패킷 사이즈 만큼만)
          while (fifoCount < packetSize) fifoCount = mpu.getFIFOCount(); // 데이터 찰 때까지 대기 (짧게)

          // FIFO 버퍼로 데이터 읽기
          mpu.getFIFOBytes(fifoBuffer, packetSize);
          // 읽었으니 fifoCount 감소 (다음에 또 읽을 수 있도록)
          fifoCount -= packetSize;

          // Yaw, Pitch, Roll 계산 및 저장
          mpu.dmpGetQuaternion(&q, fifoBuffer);
          mpu.dmpGetGravity(&gravity, &q);
          mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);

          // 계산된 값을 combinedData 구조체에 저장 (라디안 -> 각도 변환 및 저장)
          combinedData.ypr[0] = ypr[0] * 180 / M_PI;
          combinedData.ypr[1] = ypr[1] * 180 / M_PI;
          combinedData.ypr[2] = ypr[2] * 180 / M_PI;

          // (디버깅) 계산된 YPR 값 출력
          // Serial.print("ypr\t");
          // Serial.print(combinedData.ypr[0]);
          // Serial.print("\t");
          // Serial.print(combinedData.ypr[1]);
          // Serial.print("\t");
          // Serial.println(combinedData.ypr[2]);
      }
  }                     

  // --- BLE 연결 및 데이터 전송 로직 ---
  unsigned long currentMillis = millis();
  if (deviceConnected) {
    if(!oldDeviceConnected){
      oldDeviceConnected = deviceConnected; // 연결 상태 변경 감지
    }

    // 지정된 간격마다 FSR 읽고 데이터 전송
    if (currentMillis - lastUpdateTime >= updateInterval) {
      lastUpdateTime = currentMillis; // 마지막 업데이트 시간 갱신

      // FSR 값 읽어서 combinedData 구조체에 저장
      combinedData.fsr[0] = analogRead(FSR_PIN1);
      combinedData.fsr[1] = analogRead(FSR_PIN2);
      combinedData.fsr[2] = analogRead(FSR_PIN3);
      combinedData.fsr[3] = digitalRead(FSR_PIN4) * 1000; // 디지털 값 (0 또는 1000)
      combinedData.fsr[4] = digitalRead(FSR_PIN5) * 1000; // 디지털 값 (0 또는 1000)

      // (디버깅) FSR 값 출력
      // printFsrValue(1, combinedData.fsr[0]);
      // printFsrValue(2, combinedData.fsr[1]);
      // printFsrValue(3, combinedData.fsr[2]);
      // printFsrValue(4, combinedData.fsr[3]);
      // printFsrValue(5, combinedData.fsr[4]);
      Serial.println(); // 줄바꿈


      // 통합된 데이터 전송 (SensorData 구조체 전체 전송)
      pCharacteristicSensor->setValue((uint8_t*)&combinedData, sizeof(combinedData));
      pCharacteristicSensor->notify();
      // Serial.println("Notify sent"); // 디버깅 시 활성화
    }
  } else { // 연결 끊김
    if (oldDeviceConnected) {
        Serial.println("Device Disconnected - Loop");
        oldDeviceConnected = false; // 연결 상태 변경 감지;;;;;;;;;;
        delay(500);
        // pServer->startAdvertising();
    }
    // if (isAwake && !deviceConnected && lastDisconnectTime != 0 && (currentMillis - lastDisconnectTime >= deepSleepTimeout)) {
    //     Serial.println("BLE disconnected for too long. Entering deep sleep...");
    //     isAwake = false; // 딥슬립으로 갈 것이므로 상태 변경
    //     goToDeepSleep();
    //     // goToDeepSleep() 함수는 실행을 반환하지 않으므로, 이후 코드는 실행되지 않음
    // }
  }
  delay(30);
}