package com.serverless.rider;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.model.RiderDTO;
import org.apache.log4j.Logger;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class ReadGeneralHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private DynamoDbClient dynamoDbClient;
    private final String FINISHERS_DB_TABLE = System.getenv("FINISHERS_TABLE");
    private static final Logger log = Logger.getLogger(ReadGeneralHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent  input, Context context) {
        log.info("General request");
        log.info("EVENT TYPE: " + input.getClass().toString());
        log.info(input);
        initDynamoDbClient();
        List<RiderDTO> riders = new ArrayList<>();

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(FINISHERS_DB_TABLE)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        for (Map<String, AttributeValue> item : response.items()) {
            riders.add(RiderDTO.mapToDto(item));
        }
        riders.sort(Comparator.comparing(RiderDTO::getRank));
        log.info("count:" + riders.size());

        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setHeaders(Collections.singletonMap("Content-Type", "application/json"));
        responseEvent.setStatusCode(HttpStatusCode.OK);
        responseEvent.setIsBase64Encoded(false);
        try {
            responseEvent.setBody(objectMapper.writeValueAsString(riders));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return responseEvent;
    }
    private void initDynamoDbClient() {
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("REGION")))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }
}
