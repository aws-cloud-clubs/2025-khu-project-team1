package com.khu.acc.newsfeed.config;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.serverless.proxy.spring.SpringBootProxyHandlerBuilder;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.khu.acc.newsfeed.NewsFeedApplication;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    static {
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(NewsFeedApplication.class);
        } catch(ContainerInitializationException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot Lambda Container", e);
        }
    }
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, com.amazonaws.services.lambda.runtime.Context context) throws java.io.IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }
}
