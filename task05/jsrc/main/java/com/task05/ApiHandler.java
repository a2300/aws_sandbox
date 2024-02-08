package com.task05;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role"
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE,
		name = "Events")
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private final ObjectMapper objectMapper;
	private final AmazonDynamoDB dynamoDB;

	public ApiHandler() {
		this.objectMapper = new ObjectMapper();
		this.dynamoDB = AmazonDynamoDBClientBuilder.standard()
				.withRegion(Regions.EU_CENTRAL_1)
				.build();
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		context.getLogger().log("Event: " + event.toString());
		Response response = generateApiResponse();
		PutItemRequest putItemRequest = new PutItemRequest("Events",
				toDynamoDBItem(response));
		dynamoDB.putItem(putItemRequest);
		APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
		responseEvent.setStatusCode(HttpStatus.SC_CREATED);
		responseEvent.setBody(getResponse(response));
		return responseEvent;
	}

	@SneakyThrows
	public String getResponse(Response response) {
		return objectMapper.writeValueAsString(response);
	}

	public Map<String, AttributeValue> parseContent(Map<String, String> content) {
		return content.entrySet()
				.stream()
				.collect(Collectors.toMap(Entry::getKey, e->new AttributeValue(e.getValue())));
	}

	public Response generateApiResponse() {
		Map<String, String> content = new HashMap<>();
		content.put("name", "John");
		content.put("surname", "Doe");

		return new Response(201,
				new Event(UUID.randomUUID().toString(),
						1,
						ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
						content));
	}

	public Map<String, AttributeValue> toDynamoDBItem(Response response) {
		Map<String, AttributeValue> item = new HashMap<>();
		item.put("id", new AttributeValue(response.getEvent().getId()));
		item.put("principalId", new AttributeValue().withN(String.valueOf(response.getEvent().getPrincipalId())));
		item.put("createdAt", new AttributeValue().withS(response.getEvent().getCreatedAt()));
		item.put("body", new AttributeValue().withM(parseContent(response.getEvent().getBody())));
		return item;
	}
}