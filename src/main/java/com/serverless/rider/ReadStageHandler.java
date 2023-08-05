package com.serverless.rider;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.serverless.ApiGatewayResponse;
import com.serverless.model.APIResponse;
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

import java.util.*;

public class ReadStageHandler implements RequestHandler<APIGatewayProxyRequestEvent, ApiGatewayResponse> {


    private DynamoDbClient dynamoDbClient;
    private final String STAGES_DB_TABLE = System.getenv("STAGES_TABLE");
    private static final Logger log = Logger.getLogger(ReadStageHandler.class);

    @Override
    public ApiGatewayResponse handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String stage = input.getPathParameters().get("stage");
        log.info("Stage request--> " + stage);

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
                riders.add(mapToDto(item));
            }
            riders.sort(Comparator.comparing(RiderDTO::getRank));
            log.info("count:" + riders.size());
            return ApiGatewayResponse.builder()
                    .setHeaders(Collections.singletonMap("Content-Type", "application/json"))
                    .setObjectBody(riders)
                    .setStatusCode(HttpStatusCode.OK).build();

        } else {
            return ApiGatewayResponse.builder()
                    .setHeaders(Collections.singletonMap("Content-Type", "application/json"))
                    .setObjectBody(new APIResponse(HttpStatusCode.NO_CONTENT, "No content is available for the specified stage"))
                    .setStatusCode(HttpStatusCode.NO_CONTENT).build();
        }
    }


    private RiderDTO mapToDto(Map<String, AttributeValue> item) {
        RiderDTO riderDTO = new RiderDTO();
        riderDTO.setId(item.get("id").s());
        riderDTO.setName(item.get("Rider").s());
        riderDTO.setRank(Integer.parseInt(item.get("Rank").n()));
        riderDTO.setTeam(item.get("Team").s());
        riderDTO.setTime(item.get("Time").s());
        log.info(riderDTO);
        return riderDTO;
    }


    private void initDynamoDbClient() {
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("REGION")))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }
}
