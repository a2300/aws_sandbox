package com.task05;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "cmtr-50262891-Events-test")
public class Event {
    @DynamoDBHashKey(attributeName = "id")
    private String id;

    private int principalId;

    private String createdAt;

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
    private Map<String, String> body;
}
