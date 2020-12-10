package com.amazonaws.lambda.demo;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/*
 * UpdateAndNotifyFunction
 * 카드가 찍힐 경우 in인지 out인지 구분하여 card_value db의 값을 바꿔주고,out이 한개라도 있을 경우 이메일 알림을 띄워주는 함수.
 * */
public class UpdateAndNotifyHandler implements RequestHandler<Document, String> {

    private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = "Card_value";
    private int DBRows = 5;

    @Override
    public String handleRequest(Document document, Context context) {
        this.initDynamoDbClient();
        
        final String AccessKey="";
       final String SecretKey="";
       final String topicArn="";

       BasicAWSCredentials awsCreds = new BasicAWSCredentials(AccessKey, SecretKey);  
       AmazonSNS sns = AmazonSNSClientBuilder.standard()
                   .withRegion(Regions.AP_NORTHEAST_2)
                   .withCredentials( new AWSStaticCredentialsProvider(awsCreds) )
                   .build();
        
        Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);
        
        //추가된 부분 (12.7)
       HashMap<String, String> nameMap = new HashMap<String, String> ();
       nameMap.put("#deviceId", "deviceId");  //#deviceId라는 변수를 생성 -해당 변수는 deviceId라는 값을 가짐
       HashMap<String, Object> valueMap = new HashMap<String, Object>();
       valueMap.put(":deviceId", "MyMKRWiFi1010"); //:deviceId라는 변수를 생성 -해당 변수는 MyMKRWiFi1010라는 값을 가짐
       QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("#deviceId = :deviceId")  
             //"#deviceId = :deviceId" 이 말은 결국 deviceId가 MyMKRWiFi1010인 값을 찾는 것임
             .withNameMap(nameMap).withValueMap(valueMap);
       
       ItemCollection<QueryOutcome> items = null;
       //Iterator<Item> iterator = null;
       //Item item = null;
       
       try {
           items = table.query(querySpec);  //해당 쿼리에 맞는 문자들을 모조리 items에 넣음
           }catch (Exception e) {
               System.err.println("Unable to query movies from 1985");
               System.err.println(e.getMessage());
           }
        
        String put = getResponse(items);  //값을 다시 aws로 전송 ->이후 안드로이드의 getCards.class가 받아서 이를 처리
        String json = ""+put;
        JsonParser parser = new JsonParser();
        JsonObject element = (JsonObject) parser.parse(json);
        JsonArray dataArray = (JsonArray) element.getAsJsonObject().get("data");

        String[][] DATA = new String[DBRows][3];
        for(int i=0; i<dataArray.size();i++)
        {
           JsonObject RFID = (JsonObject)dataArray.get(i);
           DATA[i][0] = RFID.get("Card_state").toString().split("\"")[1];
           DATA[i][1] = RFID.get("Card_name").toString().split("\"")[1];
           DATA[i][2] = RFID.get("Card_rfid").toString().split("\"")[1];
        }
        //state , name, rfid (DB값)
        
        /* 지갑의 카드 유무 가리는 부분*/
        String CName = "";
        if(document != null) 
        {
           
           context.getLogger().log("Input: " + document);
           String state = persistData(document);
           //지갑이 닫혔을 때 (CLOSE)
           if( state.equals("0")) {
              System.out.println("if( state == \"0\") {");
              for(int i=0; i<DBRows; i++) {
                 if( DATA[i][0].equals("OUT")) { //지갑에서 나간 후 다시 들어오지 않는 경우
                    
                    CName = CName+DATA[i][1]+", ";
                 }
              }
            /*  
            //경고 이메일 보내는 부분.
             final String msg = "*카드 분실*\n" + "카드( " + CName + " )가 지갑에 들어오지 않았습니다. 한 번 더 확인해주세요.";
             final String subject = "카드 분실을 감지하였습니다!";
             PublishRequest publishRequest = new PublishRequest(topicArn, msg, subject);
             PublishResult publishResponse = sns.publish(publishRequest);
            */
           }
           
           //지갑이 열려있을 때 (OPEN) = 계속 값을 읽고 있음.
           else {
              for(int i=0; i<DBRows; i++) {
                 if(DATA[i][2].equals(state)) { //들어온 RFID 값이 기등록한 카드 RFID 와 같을 경우
                    if(DATA[i][0].equals("IN")) { 
                         //LOG.info("OUT으로 변경");
                       return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                                 .putItem(new PutItemSpec().withItem(new Item().withPrimaryKey("deviceId", "MyMKRWiFi1010")
                                       .withString("Card_rfid", DATA[i][2])
                                       .withLong("time", document.timestamp)
                                       .withString("Card_name",DATA[i][1])
                                       .withString("Card_state", "OUT"))) //12.6 추가한 부분
                                 .toString();
                      }
                      else if(DATA[i][0].equals("OUT")){
                         //LOG.info("IN으로 변경");
                         return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                                     .putItem(new PutItemSpec().withItem(new Item().withPrimaryKey("deviceId", "MyMKRWiFi1010")
                                           .withString("Card_rfid", DATA[i][2])
                                           .withLong("time", document.timestamp)
                                           .withString("Card_name",DATA[i][1])
                                           .withString("Card_state", "IN"))) //12.6 추가한 부분
                                     .toString();
                      }
                 }
              }
           }
        }

        
        
            
            
            
            return "THE END";
            
            //return getResponse(items);  //값을 다시 aws로 전송 ->이후 안드로이드의 getCards.class가 받아서 이를 처리
    }

    private String getResponse(ItemCollection<QueryOutcome> items) {

        Iterator<Item> iter = items.iterator();
        String response = "{ \"data\": [";
        for (int i =0; iter.hasNext(); i++) {
            if (i!=0) 
                response +=",";
            response += iter.next().toJSON();
        }
        response += "]}";
        
        return response;
    }

    private String persistData(Document document) throws ConditionalCheckFailedException {
        String state = document.current.state.reported.RFID;
        return state;         
     }

    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        this.dynamoDb = new DynamoDB(client);
    }
}

class Event {
    public String device;
    public String from;
    public String to;
}
class Document {
    public Thing previous;       
    public Thing current;
    public long timestamp;
    public String device;       // AWS IoT에 등록된 사물 이름 
    //public String RFID;
}

class Thing {
    public State state = new State();
    public long timestamp;
    public String clientToken;
    //public String RFID;

    public class State {
        public Tag reported = new Tag();
        public Tag desired = new Tag();

        public class Tag {
            public String RFID;            
        }
    }
}