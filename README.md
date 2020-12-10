# SmartIoTWallet
2020-2 IoT Cloud Platform team project

1. Arduino
>1-(1)아두이노 UNO & 블루투스 모듈 & RFID 모듈 (Arduino_Bluetooth.ino)
>>위의 부품들을 사용하여 안드로이드 어플에서  카드 등록을 할 때 필요한 RFID의 십진수 값을 블루투스를 이용하여 받아온다.
>1-(2)MKRWiFi1010 & 푸쉬 버튼 (AWS_IoT_RFID.ino)
>>아두이노 UNO에서 확인한 RFID 값을 AWS_IoT_RFID 코드에 배열로 설정하여 시리얼 모니터 창에 숫자를 입력하면 해당하는 RFID 값을 불러올 수 있다. 
*MKRWiFi1010에 RFID 모듈이 제대로 작동되지 않아 위와 같은 방법을 이용하였다.

>>AWS_IoT_RFID.ino 코드에서 WiFi 연결과 MQTT 연결을 완료하면, AWS의 섀도우에 값을 올리거나 섀도우의 값을 가져올 수 있다. MKR보드와 연결된 푸쉬버튼은 지갑의 열고 닫힘을 알게해준다. 버튼이 눌리면 state가 0인 상태로 섀도우에 올라가게 된다. 어플을 이용해 rfid모듈을 비활성화 상태로 변경시킨다면 아두이노에서는 구독을 통해 어플에서 보내진 섀도우 값(disabled)을 받아 MKR보드에서 값을 보내도 DB에 저장하지 못하게 된다.

2. Android
>메인 화면
>>어플의 메인 화면에는 카드 추가, 카드 조회, 비활성화 모드(ON/OFF) 버튼이 있다. 
>>2-(1)카드 추가
카드 추가 버튼을 누르면 MainActivity에서 RegisterCardActivity로 넘어가게 된다. RegisterCardActivity에는 아두이노 UNO 보드와 블루투스를 연결하기 위한 함수(ConnectedThread)와 카드 추가 란에서 작성한 카드 이름과 카드 rfid를 document의 reported 형태로 Shadow에 올려준다(API : /devices/{device}의 PUT 메소드-UpdateDeviceFunction). Shadow에 값이 올라가면 CardRule(IoT규칙)으로 인해 Card_value라는 DynamoDB에 카드 등록 값을 저장해준다.
>>2-(2)카드 조회
카드 조회 버튼을 누르면 MainActivity에서 CardListActivity로 넘어가게 된다. 
   
