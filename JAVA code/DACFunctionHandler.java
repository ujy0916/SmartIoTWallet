package com.amazonaws.lambda.demo;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Iterator;

import com.amazonaws.lambda.demo.Thing.State;
import com.amazonaws.lambda.demo.Thing.State.Tag;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DACFunctionHandler implements RequestHandler<Document, String> {

	private static DynamoDB dynamoDb;
    private static String DYNAMODB_TABLE_NAME = "Test";
    private final static Logger LOG = Logger.getGlobal();
    static String deviceId_DB, State_DB, RFID_DB;
    static int time_; 

    @Override
    public String handleRequest(Document input, Context context) {
        this.initDynamoDbClient();
        context.getLogger().log("Input: " + input);
        
        return persistData(input);
    }

    private String persistData(Document document) throws ConditionalCheckFailedException {

        // Epoch Conversion Code: https://www.epochconverter.com/
        SimpleDateFormat sdf = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String timeString = sdf.format(new java.util.Date (document.timestamp*1000));
        //int timeInt = Integer.parseInt(sdf.format(new java.util.Date (document.timestamp*1000)));
        //*1000한 이유: 1초는 1000ms이기 때문.
        
        Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);
        
       //현재 섀도우에 올라온 id(RFID대신) 값을 가져옴
        //String name = document.current.state.reported.firstName; 
        String deviceId = "MyMKRWiFi1010";
        time_ = 1604543018;
        
        //해당 이름을 가진 DB의 항목들을 가져옴
        deviceId_DB = this.dynamoDb.getTable(DYNAMODB_TABLE_NAME).getItem(new GetItemSpec().withPrimaryKey("deviceId", deviceId,"time_", time_)
				.withProjectionExpression("deviceId")
				.withConsistentRead(true)).toString();
        
        State_DB = this.dynamoDb.getTable(DYNAMODB_TABLE_NAME).getItem(new GetItemSpec().withPrimaryKey("deviceId", deviceId,"time_", time_)
				.withProjectionExpression("state_")
				.withConsistentRead(true)).toString();
        
        RFID_DB = this.dynamoDb.getTable(DYNAMODB_TABLE_NAME).getItem(new GetItemSpec().withPrimaryKey("deviceId", deviceId,"time_", time_)
				.withProjectionExpression("RFID")
				.withConsistentRead(true)).toString();
        
        //원하는 문자만 처리해서 가져오기
        deviceId_DB = StringChange(deviceId_DB);        
        State_DB = StringChange(State_DB);
        RFID_DB = StringChange(RFID_DB);
        
        //제대로 가지고 왔는지 확인
        LOG.info("deviceId_DB= "+deviceId_DB+" RFID_DB= "+RFID_DB+" State_DB= "+State_DB);
        //return State_DB;
        
        //DB에서 원하는 항목 삭제
        deleteItem();      
        
        
        
        //DB에 다시 항목 추가
        if(State_DB.equals("IN")) {
        	//LOG.info("OUT으로 변경");
        	return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                    .putItem(new PutItemSpec().withItem(new Item().withPrimaryKey("deviceId", deviceId_DB,"time_",time_)
                    		.withString("RFID",RFID_DB)
                    		.withString("State_DB","OUT"))).toString();
        }
        else if(State_DB.equals("OUT")){
        	//LOG.info("IN으로 변경");
        	return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                    .putItem(new PutItemSpec().withItem(new Item().withPrimaryKey("deviceId", deviceId_DB,"time_",time_)
                    		.withString("RFID", RFID_DB)
                    		.withString("State_DB", "IN"))).toString();
        }
        
      return "DB에 아무값도 들어가지 않음";
    }

    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("ap-northeast-2").build();

        this.dynamoDb = new DynamoDB(client);
    }
    
    private String StringChange(String s) {
    	String str = s.replace("{", " ");
    	str = str.replace("}", " ");
    	str = str.replaceAll(" ","");
    	str = str.split("=")[1];
    	return str;    	
    }
    
    //항목 삭제
    public void deleteItem() {
    	Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);
    	try {
    		DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
    				.withPrimaryKey("deviceId", deviceId_DB,"time_",time_);
    		DeleteItemOutcome outcome = table.deleteItem(deleteItemSpec);
    				
    	} catch (Exception e) {
    		
    	}
    }

}

class Document {
    public Thing previous;       
    public Thing current;
    public long timestamp;
    //public String device;       // AWS IoT에 등록된 사물 이름 
}

class Thing {
    public State state = new State();
    public long timestamp;
    public String clientToken;

    public class State {
        public Tag reported = new Tag();
        public Tag desired = new Tag();

        public class Tag {
            public String deviceId;
            public int time_;
            public String RFID;
            public String state_;           
            
            //public String Card_name;
        }
    }
}
