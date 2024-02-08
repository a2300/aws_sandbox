package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role"
)
public class ApiHandler implements RequestHandler<Request, Response> {
	private final Gson jsonFormatter;
	private final AmazonDynamoDB amazonDynamoDB;

	public ApiHandler() {
		this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
				.withRegion("eu-central-1")
				.build();
		this.jsonFormatter = new GsonBuilder().create();
	}

	public Response handleRequest(Request request, Context context) {
		Response response = new Response();
		response.setStatusCode(HttpStatus.SC_CREATED);
		try {
			response.setEvent(process(request));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return response;

	}

	private Event process(Request request) throws IOException {
		String generatedId = UUID.randomUUID().toString();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
		String formattedDateTime = LocalDateTime.now().format(formatter);
		Map body = new ObjectMapper().readValue(jsonFormatter.toJson(request.getContent()), HashMap.class);

		DbRecord item = new DbRecord();
		item.setId(generatedId);
		item.setPrincipalId(request.getPrincipalId());
		item.setCreatedAt(formattedDateTime);
		item.setBody(body);

		DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);
		mapper.save(item);

		Event event = new Event();
		event.setBody(jsonFormatter.toJson(request.getContent()));
		event.setId(generatedId);
		event.setCreatedAt(formattedDateTime);
		event.setPrincipalId(request.getPrincipalId());
		return event;
	}
}