package com.example.Shorty.user;

import com.example.Shorty.BaseModel;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class User extends BaseModel {

    private String username;
    private String email;
    private String password;
    private String providerId;
    private String apiKey;
    private boolean isActive;

    @DynamoDbAttribute("username")
    public String getUsername() {
        return username;
    }

    @DynamoDbAttribute("email")
    @DynamoDbSecondaryPartitionKey(indexNames = "email-index")
    public String getEmail() {
        return email;
    }

    @DynamoDbAttribute("password")
    public String getPassword() {
        return password;
    }

    @DynamoDbAttribute("apiKey")
    @DynamoDbSecondaryPartitionKey(indexNames = "apiKey-index")
    public String getApiKey() {
        return apiKey;
    }

    @DynamoDbAttribute("providerId")
    public String getProviderId() {
        return providerId;
    }

    @DynamoDbAttribute("isActive")
    public boolean isActive() {
        return isActive;
    }
}
