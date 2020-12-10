package com.amazonaws.lambda.demo;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/*
 * UploadRFIDValueFunction
 * RFID, state, time, timestamp 값을 DynamoDB(RFID_Value)에 올려주는 람다함수.
 * IoT규칙(RFIDRule)과 연결되어 있음.
 */

public class UploadRFIDValueHandler implements RequestHandler<Document, String> {
    private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = "RFID_value"; //RFID 값
    //private String DYNAMODB_TABLE_NAME2 = "Card_value"; //CARD 값

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

        // RFID와 LED 값이 이전상태와 동일한 경우 테이블에 저장하지 않고 종료 
        if (document.current.state.reported.RFID.equals(document.previous.state.reported.RFID) && 
                document.current.state.reported.State.equals(document.previous.state.reported.State)) {
                return null;
        }
        String state = "";
        if(document.current.state.reported.State.equals("1")) {state = "OPEN";}
        else if(document.current.state.reported.State.equals("0")) {state = "CLOSE";}

        return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                .putItem(new PutItemSpec().withItem(new Item().withPrimaryKey("RFID", document.current.state.reported.RFID)
                        .withLong("time_", document.timestamp)
                        .withString("State_", state)
                        .withString("timestamp_",timeString)))
                .toString();
    }

    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("ap-northeast-2").build();

        this.dynamoDb = new DynamoDB(client);
    }

}

/**
 * AWS IoT은(는) 섀도우 업데이트가 성공적으로 완료될 때마다 /update/documents 주제에 다음 상태문서를 게시합니다
 * JSON 형식의 상태문서는 2개의 기본 노드를 포함합니다. previous 및 current. 
 * previous 노드에는 업데이트가 수행되기 전의 전체 섀도우 문서의 내용이 포함되고, 
 * current에는 업데이트가 성공적으로 적용된 후의 전체 섀도우 문서가 포함됩니다. 
 * 섀도우가 처음 업데이트(생성)되면 previous 노드에는 null이 포함됩니다.
 * 
 * timestamp는 상태문서가 생성된 시간 정보이고, device는 상태문서에 포함된 값이 아닌 Iot규칙을 통해서 Lambda함수로 전달된 값이다. 
 * 이 값을 해당 규칙과 관련된 사물이름을 나타낸다. 
 */
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
            public String State;
        }
    }
}