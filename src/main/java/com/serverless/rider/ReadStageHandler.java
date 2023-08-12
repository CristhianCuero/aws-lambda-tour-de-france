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
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.Condition;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadStageHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    private DynamoDbClient dynamoDbClient;
    private final String STAGES_DB_TABLE = System.getenv("STAGES_TABLE");
    private static final Logger log = Logger.getLogger(ReadStageHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        log.info("Stage request");
        log.info("EVENT TYPE: " + event.getClass().toString());
        log.info(event);

        String stage = event.getPathParameters().get("stage");
        initDynamoDbClient();
        List<RiderDTO> riders = new ArrayList<>();

        Condition stageCondition = Condition.builder()
                .comparisonOperator(ComparisonOperator.EQ)
                .attributeValueList(
                        AttributeValue.builder()
                                .s(stage)
                                .build()
                )
                .build();

        Map<String, Condition> conditionMap = new HashMap<>();
        conditionMap.put("Stage", stageCondition);

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(STAGES_DB_TABLE)
                .keyConditions(conditionMap)
                .indexName("stage_index")
                // .projectionExpression("Rank,Time,Rider,Team")
                .build();

        QueryResponse response = dynamoDbClient.query(queryRequest);

        if (response.count()>0) {
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


        } else {
            APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
            responseEvent.setHeaders(Collections.singletonMap("Content-Type", "text/plain"));
            responseEvent.setStatusCode(HttpStatusCode.NO_CONTENT);
            responseEvent.setIsBase64Encoded(false);
            responseEvent.setBody("No content is available for the specified stage");
            return responseEvent;
        }
    }
    private void initDynamoDbClient() {
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("REGION")))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }
}
