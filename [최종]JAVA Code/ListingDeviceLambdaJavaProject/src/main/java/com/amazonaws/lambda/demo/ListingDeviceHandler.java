package com.amazonaws.lambda.demo;

import java.util.List;
import com.amazonaws.services.iot.AWSIot;
import com.amazonaws.services.iot.AWSIotClientBuilder;
import com.amazonaws.services.iot.model.ListThingsRequest;
import com.amazonaws.services.iot.model.ListThingsResult;
import com.amazonaws.services.iot.model.ThingAttribute;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/*
 * ListThingsFunction
 * /devices - GET - �޼��� ����
 */
public class ListingDeviceHandler implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {

    	// AWSIot ��ü�� ��´�. 
        AWSIot iot = AWSIotClientBuilder.standard().build();

     // ListThingsRequest ��ü ����. 
        ListThingsRequest listThingsRequest = new ListThingsRequest();

        // listThings �޼ҵ� ȣ���Ͽ� ��� ����. 
        ListThingsResult result = iot.listThings(listThingsRequest);

     // result ��ü�κ��� API ����� ���ڿ� �����Ͽ� ��ȯ
        return getResponse(result);
    }
    
    /**
     * ListThingsResult ��ü�� result�� ���� ThingName�� ThingArn�� �� Json���� ������
     * ������� ����� ��ȯ�Ѵ�.
     * {
     *  "things": [ 
     *       { 
     *          "thingName": "string",
     *          "thingArn": "string"
     *       },
     *       ...
     *     ]
     * }
     */
    
    private String getResponse(ListThingsResult result) {
        List<ThingAttribute> things = result.getThings();

        String response = "{ \"things\": [";
        for (int i =0; i<things.size(); i++) {
            if (i!=0) 
                response +=",";
            response += String.format("{\"thingName\":\"%s\", \"thingArn\":\"%s\"}", 
                                                things.get(i).getThingName(),
                                                things.get(i).getThingArn());

        }
        response += "]}";
        return response;
    }

}