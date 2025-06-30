package com.khu.acc.newsfeed.common.config;

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
            // Lambda 환경에서는 자동으로 lambda 프로파일 사용
            System.setProperty("spring.profiles.active", "lambda");
            
            handler = new SpringBootProxyHandlerBuilder<AwsProxyRequest>()
                    .defaultProxy()
                    .asyncInit()
                    .springBootApplication(NewsFeedApplication.class)
                    .buildAndInitialize();
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
