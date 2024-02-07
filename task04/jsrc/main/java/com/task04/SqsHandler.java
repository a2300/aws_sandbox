package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

@LambdaHandler(lambdaName = "sqs_handler",
		roleName = "sqs_handler-role"
)
@SqsTriggerEventSource(targetQueue = "async_queue", batchSize = 1)
public class SqsHandler implements RequestHandler<SQSEvent, Void> {

	public Void handleRequest(SQSEvent event, Context context) {
		for (SQSEvent.SQSMessage msg : event.getRecords()) {
			String message = msg.getBody();
			// Print the message content to CloudWatch
			context.getLogger().log("Message received: " + message);
			System.out.println("Message content: " + message);
		}
		return null;
	}
}
