package com.serverless;

import java.util.Collections;
import java.util.Map;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.serverless.model.APIResponse;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    private AmazonDynamoDB amazonDynamoDB;
    private final String FINISHERS_DB_TABLE = System.getenv("FINISHERS_TABLE");
    private final String STAGES_DB_TABLE = System.getenv("STAGES_TABLE");
    private final Regions REGION = Regions.fromName(System.getenv("REGION"));
    private static final Logger log = Logger.getLogger(Handler.class);

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        log.info("Incoming health request");
        this.initDynamoDbClient();
        checkTableConn(FINISHERS_DB_TABLE);
        checkTableConn(STAGES_DB_TABLE);
        return ApiGatewayResponse.builder()
                .setStatusCode(HttpStatus.SC_OK)
                .setHeaders(Collections.singletonMap("Content-Type", "application/json"))
                .setObjectBody(new APIResponse(HttpStatus.SC_OK,"UP"))
                .build();
    }

    private DescribeTableResult checkTableConn(String table) {
        return this.amazonDynamoDB.describeTable(table);
    }

    private void initDynamoDbClient() {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(REGION)
                .build();
    }
}
