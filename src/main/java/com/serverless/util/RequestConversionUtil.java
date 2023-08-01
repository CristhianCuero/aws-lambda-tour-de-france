package com.serverless.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.exception.IncomingRequestParsingException;

public class RequestConversionUtil {

    ObjectMapper objectMapper = new ObjectMapper();

    public <T> T parseRequestBody(String requestBodyContent, Class<T> outPutClass) {
        try {
            return objectMapper.readValue(requestBodyContent, outPutClass);
        } catch (JsonProcessingException e) {
            throw new IncomingRequestParsingException("mensaje");
        }
    }
}
