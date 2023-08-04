package com.serverless.rider;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.serverless.Handler;
import com.serverless.model.APIResponse;
import com.serverless.model.RiderDTO;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReadHandler implements RequestHandler<APIGatewayProxyRequestEvent, ApiGatewayResponse> {

    private AmazonDynamoDB amazonDynamoDB;
    private final String FINISHERS_DB_TABLE = System.getenv("FINISHERS_TABLE");
    private final String STAGES_DB_TABLE = System.getenv("STAGES_TABLE");
    private Regions REGION = Regions.fromName(System.getenv("REGION"));
    private static final Logger log = Logger.getLogger(ReadHandler.class);

    @Override
    public ApiGatewayResponse handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        this.initDynamoDbClient();
        Map<String, String> queryParams = input.getQueryStringParameters();


        log.info("General request");
        //General
        Map<String, AttributeValue> lastKeyEvaluated = null;
        List<RiderDTO> riders = new ArrayList<>();
        do {
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(FINISHERS_DB_TABLE)
                    .withLimit(10)
                    .withExclusiveStartKey(lastKeyEvaluated);
            ScanResult result = amazonDynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> item : result.getItems()) {
                riders.add(mapToDto(item));
            }
            lastKeyEvaluated = result.getLastEvaluatedKey();
        } while (lastKeyEvaluated != null);
        log.info("count:" + riders.size());
        log.info(riders);
        return ApiGatewayResponse.builder()
                .setHeaders(Collections.singletonMap("Content-Type", "application/json"))
                .setObjectBody(riders)
                .setStatusCode(HttpStatus.SC_OK).build();
    }


    private RiderDTO mapToDto(Map<String, AttributeValue> item) {
        RiderDTO riderDTO = new RiderDTO();
        riderDTO.setId(item.get("id").getS());
        riderDTO.setName(item.get("Rider").getS());
        riderDTO.setRank(Integer.parseInt(item.get("Rank").getN()));
        riderDTO.setTeam(item.get("Team").getS());
        riderDTO.setTime(item.get("Time").getS());
        return riderDTO;
    }


    private void initDynamoDbClient() {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(REGION)
                .build();
    }


}
