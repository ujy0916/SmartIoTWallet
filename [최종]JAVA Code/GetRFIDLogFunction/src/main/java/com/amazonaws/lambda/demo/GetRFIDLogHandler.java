package com.amazonaws.lambda.demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.TimeZone;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/*
 * GetRFIDLogFunction
 * 해당 카드(rfid)의 DynamoDB(RFID_value) 값을 불러오는 람다함수.
 * /devices/{device}/value - GET - 메서드 실행
 */
public class GetRFIDLogHandler implements RequestHandler<Event, String> {

    private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = "RFID_value";

    @Override
    public String handleRequest(Event input, Context context) {
        this.initDynamoDbClient();

        Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);

        long from=0;  //from 시간 값
        long to=0;  //to 시간 값
        try {
            SimpleDateFormat sdf = new SimpleDateFormat ( "yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            
            //어플에서 받아온 시간 input 값에서 from과 to를 가져옴.
            from = sdf.parse(input.from).getTime() / 1000;
            to = sdf.parse(input.to).getTime() / 1000;
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("RFID = :v_id and #t between :from and :to")  
                //:v_id라는 RFID 값 중 :from에서 :to 사이의 #t의 값만 가지고 오라는 뜻
                .withNameMap(new NameMap().with("#t", "time_"))
                //#t는 RFID_value DB에 저장되어 있는 time_ 값을 뜻함.
                .withValueMap(new ValueMap().withString(":v_id",input.device).withNumber(":from", from).withNumber(":to", to)); 

        ItemCollection<QueryOutcome> items=null;  //ItemCollection은 설정한 쿼리를 통해 반환된 값들을 가져오는 객첵임.
        try {           
            items = table.query(querySpec);
        }
        catch (Exception e) {
            System.err.println("Unable to scan the table:");
            System.err.println(e.getMessage());
        }

        return getResponse(items);
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