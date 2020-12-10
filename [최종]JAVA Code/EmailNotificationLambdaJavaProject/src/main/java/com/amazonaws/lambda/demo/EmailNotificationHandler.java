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
 * ������ ī�� ������ ���� ���, �̸����� ���� �����Լ�.
 * IoT��Ģ(LostCardWarningRule)�� ����Ǿ� ����.
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

	    String test = reported.getAsJsonObject().get("DISABLED").getAsString();
	    String test_abled = String.valueOf(test);

	    final String AccessKey="AccessKey";
	    final String SecretKey="SecretKey";
	    final String topicArn="topicArn";

	    BasicAWSCredentials awsCreds = new BasicAWSCredentials(AccessKey, SecretKey);  
	    AmazonSNS sns = AmazonSNSClientBuilder.standard()
	                .withRegion(Regions.AP_NORTHEAST_2)
	                .withCredentials( new AWSStaticCredentialsProvider(awsCreds) )
	                .build();

	    final String msg = "*ī�� �н�*\n" + "ī��( " + test_abled + " )�� ������ ������ �ʾҽ��ϴ�. �� �� �� Ȯ�����ּ���.";
	    final String subject = "Critical Warning";
	    if (test_abled.equals("Disabled")) {
	        PublishRequest publishRequest = new PublishRequest(topicArn, msg, subject);
	        PublishResult publishResponse = sns.publish(publishRequest);
	    }

	    return subject+ "test_abled = " + test_abled + "!";
	}
}
