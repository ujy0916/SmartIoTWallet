package com.amazonaws.lambda.demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
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
 * GetCardNameFunction
 * DynamoDB(Card_value)의 값을 불러오는 람다함수
 * /devices/{device}/log - GET - 메서드 실행
 */
public class GetCardNameHandler implements RequestHandler<Event, String> {

    private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = "Card_value";

    @Override
    public String handleRequest(Event input, Context context) {
        this.initDynamoDbClient();

        Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);

        long from=0;
        long to=0;
            SimpleDateFormat sdf = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            from = 1604536533;
            to = 1609374933;

            //deviceId가 MyMKRWiFi1010인 값을 모두 찾는 부분
            HashMap<String, String> nameMap = new HashMap<String, String> ();
            nameMap.put("#deviceId", "deviceId");  //#deviceId라는 변수를 생성 -해당 변수는 deviceId라는 값을 가짐
            HashMap<String, Object> valueMap = new HashMap<String, Object>();
            valueMap.put(":deviceId", "MyMKRWiFi1010"); //:deviceId라는 변수를 생성 -해당 변수는 MyMKRWiFi1010라는 값을 가짐
            QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("#deviceId = :deviceId")  
            		//"#deviceId = :deviceId" 이 말은 결국 deviceId가 MyMKRWiFi1010인 값을 찾는 것임
                  .withNameMap(nameMap).withValueMap(valueMap);
            
            ItemCollection<QueryOutcome> items = null;
            
            try {
                items = table.query(querySpec);  //해당 쿼리에 맞는 문자들을 모조리 items에 넣음
                }catch (Exception e) {
                    System.err.println("Unable to query movies from 1985");
                    System.err.println(e.getMessage());
                }     
            
            return getResponse(items);  //값을 다시 aws로 전송 ->이후 안드로이드의 GetCards.class가 받아서 이를 처리
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
