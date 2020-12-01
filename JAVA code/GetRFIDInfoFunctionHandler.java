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
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;

public class GetRFIDInfoFunctionHandler implements RequestHandler<Document, String> {

	private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = "People";

    @Override
    public String handleRequest(Document input, Context context) {
        this.initDynamoDbClient();
        context.getLogger().log("Input: " + input);
        
        persistData(input);
        
        return "end";
        
        //return persistData();

        //return persistData(input);
    }

    private void persistData(Document document) throws ConditionalCheckFailedException {

        // Epoch Conversion Code: https://www.epochconverter.com/
        SimpleDateFormat sdf = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String timeString = sdf.format(new java.util.Date (document.timestamp*1000));
        //*1000한 이유: 1초는 1000ms이기 때문.
        
        Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);
        
        Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
        expressionAttributeValues.put(":id", 0);
        
        ItemCollection<ScanOutcome> items = table.scan("id > :id",
        		"firstName, lastName",
        		null,
        		expressionAttributeValues);
        
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
        	System.out.println(iterator.next().toJSONPretty());
        }
        
         
        
        /*테이블에서 항목 하나 뽑아오기
         * return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
        		.getItem(new GetItemSpec().withPrimaryKey("id", 1)
        				.withProjectionExpression("id, firstName, lastName")
        				.withConsistentRead(true)).toString();*/

        /*테이블에 저장
         * return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                .putItem(new PutItemSpec().withItem(new Item().withPrimaryKey("Card_rfid", document.current.state.reported.Card_rfid)
                		.withString("Card_name",document.current.state.reported.Card_name)
                        .withString("timestamp",timeString)))
                .toString();*/
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
            public String RFID;
            public String Card_name;
        }
    }
}