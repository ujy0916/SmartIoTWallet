package com.amazonaws.lambda.demo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/*
 * EmailNotificationFunction
 * 지갑에 카드 들어오지 않을 경우, 이메일이 가는 람다함수.
 * IoT규칙(LostCardWarningRule)과 연결되어 있음.
 */

public class EmailNotificationHandler implements RequestHandler<Object, String> {

	@Override
	public String handleRequest(Object input, Context context) {
	    context.getLogger().log("Input: " + input);
	    String json = ""+input;
	    JsonParser parser = new JsonParser();
	    JsonElement element = parser.parse(json);
	    JsonElement state = element.getAsJsonObject().get("state");
	    JsonElement reported = state.getAsJsonObject().get("reported");
	    JsonElement desired = state.getAsJsonObject().get("desired");
	    //String War_str = reported.getAsJsonObject().get("").getAsString();
	    //int War = Integer.valueOf(War_str); //알림 신호
	    //String CardName = reported.getAsJsonObject().get("Card_name").getAsString();
	    //String Card_name = String.valueOf(CardName);
	    //double Card_name = Double.valueOf(CardName); //카드이름
	    String test = reported.getAsJsonObject().get("DISABLED").getAsString();
	    String test_abled = String.valueOf(test);

	    final String AccessKey="AKIASR2I45YOJ75R4UUI";
	    final String SecretKey="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";
	    final String topicArn="arn:aws:sns:ap-northeast-2:175709679132:Lost_card_warning";

	    BasicAWSCredentials awsCreds = new BasicAWSCredentials(AccessKey, SecretKey);  
	    AmazonSNS sns = AmazonSNSClientBuilder.standard()
	                .withRegion(Regions.AP_NORTHEAST_2)
	                .withCredentials( new AWSStaticCredentialsProvider(awsCreds) )
	                .build();

	    final String msg = "*카드 분실*\n" + "카드( " + test_abled + " )가 지갑에 들어오지 않았습니다. 한 번 더 확인해주세요.";
	    final String subject = "Critical Warning";
	    if (test_abled.equals("Disabled")) {
	        PublishRequest publishRequest = new PublishRequest(topicArn, msg, subject);
	        PublishResult publishResponse = sns.publish(publishRequest);
	    }

	    return subject+ "test_abled = " + test_abled + "!";
	}
}
