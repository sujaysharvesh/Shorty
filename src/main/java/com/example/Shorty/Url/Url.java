package com.example.Shorty.Url;

import com.example.Shorty.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

import java.time.Instant;


@DynamoDbBean
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Url extends BaseModel {

    private String userId;
    private String originalUrl;
    private String shortCode;
    private int clickCount;
    private Instant expiresAt;
    private boolean active;


    @DynamoDbAttribute("userId")
    @DynamoDbSecondaryPartitionKey(indexNames = "userId-shortCode-index")
    public String getUserId() {
        return userId;
    }

    @DynamoDbAttribute("originalUrl")
    public String getOriginalUrl(){
        return originalUrl;
    }

    @DynamoDbAttribute("shortCode")
    @DynamoDbSecondaryPartitionKey(indexNames = "shortCode-index")
    @DynamoDbSecondarySortKey(indexNames = "userId-shortCode-index")
    public String getShortCode() {
        return shortCode;
    }


    @DynamoDbAttribute("expiresAt")
    public Instant getExpiresAt() {
        return expiresAt;
    }


    @DynamoDbAttribute("clickCount")
    public int getClickCount() {
        return clickCount;
    }

    @DynamoDbAttribute("isActive")
    public boolean isActive() {
        return active;
    }

}
