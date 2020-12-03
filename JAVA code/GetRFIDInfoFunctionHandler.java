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

	private DynamoDB dynamoDB;
	 private String DYNAMODB_TABLE_NAME = "RFID_value";
	 
	 @Override
	 public String handleRequest(Document input, Context context) {
	     this.initDynamoDbClient();
	     context.getLogger().log("Input: " + input);
	     
	     persistData(input);
	     
	     return "end";
	 }
	
	 private void persistData(Document document) throws ConditionalCheckFailedException {	     
	     Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);
	     
	     Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
	     expressionAttributeValues.put(":id", 0);
	     
	     ItemCollection<ScanOutcome> items = table.scan("time_> :id",
	     		"RFID, time_, State_, timestamp_",
	     		null,
	     		expressionAttributeValues);
	     
	     Iterator<Item> iterator = items.iterator();
	     while (iterator.hasNext()) {
	     	System.out.println(iterator.next().toJSONPretty());
	     }
	 }
	
	 private void initDynamoDbClient() {
	     AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("ap-northeast-2").build();
	
	     this.dynamoDB = new DynamoDB(client);
	 }
	
	}
	
	class Document {}