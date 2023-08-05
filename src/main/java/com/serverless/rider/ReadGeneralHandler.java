package com.serverless.rider;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
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


public class ReadGeneralHandler implements RequestHandler<APIGatewayProxyRequestEvent, ApiGatewayResponse> {

    private DynamoDbClient dynamoDbClient;
    private final String FINISHERS_DB_TABLE = System.getenv("FINISHERS_TABLE");
    private static final Logger log = Logger.getLogger(ReadGeneralHandler.class);

    @Override
    public ApiGatewayResponse handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        log.info("General request");
        initDynamoDbClient();
        List<RiderDTO> riders = new ArrayList<>();

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(FINISHERS_DB_TABLE)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        for (Map<String, AttributeValue> item : response.items()) {
            riders.add(mapToDto(item));
        }
        riders.sort(Comparator.comparing(RiderDTO::getRank));
        log.info("count:" + riders.size());
        return ApiGatewayResponse.builder()
                .setHeaders(Collections.singletonMap("Content-Type", "application/json"))
                .setObjectBody(riders)
                .setStatusCode(HttpStatusCode.OK).build();
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
