package com.amazonaws.lambda.demo;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.amazonaws.lambda.demo.Thing.State;
import com.amazonaws.lambda.demo.Thing.State.Tag;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class RegisterCardInfoHandler implements RequestHandler<Document, String> {
    private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = "Card_value";

    @Override
    public String handleRequest(Document input, Context context) {
        this.initDynamoDbClient();
        context.getLogger().log("Input: " + input);

        //return null;
        return persistData(input);
    }

    private String persistData(Document document) throws ConditionalCheckFailedException {

        // Epoch Conversion Code: https://www.epochconverter.com/
        SimpleDateFormat sdf = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String timeString = sdf.format(new java.util.Date (document.timestamp*1000));
        //*1000한 이유: 1초는 1000ms이기 때문.

        // temperature와 LED 값이 이전상태와 동일한 경우 테이블에 저장하지 않고 종료 
        if (document.current.state.reported.Card_rfid.equals(document.previous.state.reported.Card_rfid) && 
                document.current.state.reported.Card_name.equals(document.previous.state.reported.Card_name)) {
                return null;
        }

        return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                .putItem(new PutItemSpec().withItem(new Item().withPrimaryKey("deviceId", document.device)
                		.withLong("time", document.timestamp)
                        //.withString("temperature", document.current.state.reported.temperature)
                        //.withString("LED", document.current.state.reported.LED)
                		.withString("Card_name",document.current.state.reported.Card_name)
                		.withString("Card_rfid", document.current.state.reported.Card_rfid)
                		.withString("Card_state", "IN")
                        .withString("timestamp",timeString)))
                .toString();
    }

    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("ap-northeast-2").build();

        this.dynamoDb = new DynamoDB(client);
    }

}

class Document {
    public Thing previous;       
    public Thing current;
    public long timestamp;
    public String device;       // AWS IoT에 등록된 사물 이름 
}

class Thing {
    public State state = new State();
    public long timestamp;
    public String clientToken;

    public class State {
        public Tag reported = new Tag();
        public Tag desired = new Tag();

        public class Tag {
            public String Card_rfid;
            public String Card_name;
        }
    }
}