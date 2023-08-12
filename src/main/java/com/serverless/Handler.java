package com.serverless;

import java.util.Collections;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.log4j.Logger;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.awscore.defaultsmode.DefaultsMode;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;

public class Handler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {
    private DynamoDbClient dynamoDbClient;
    private final String FINISHERS_DB_TABLE = System.getenv("FINISHERS_TABLE");
    private final String STAGES_DB_TABLE = System.getenv("STAGES_TABLE");
    private static final Logger log = Logger.getLogger(Handler.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> input, Context context) {
        log.info("Incoming health request");
        this.initDynamoDbClient();
        checkTableConn(FINISHERS_DB_TABLE);
        checkTableConn(STAGES_DB_TABLE);

        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setHeaders(Collections.singletonMap("Content-Type", "text/plain"));
        responseEvent.setStatusCode(HttpStatusCode.OK);
        responseEvent.setBody("Healthy");
        responseEvent.setIsBase64Encoded(false);
        return responseEvent;
    }

    private DescribeTableResponse checkTableConn(String table) {
        return this.dynamoDbClient.describeTable(
                DescribeTableRequest.builder().
                        tableName(table).build());
    }

    private void initDynamoDbClient() {
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("REGION")))
                .defaultsMode(DefaultsMode.STANDARD)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }
}
