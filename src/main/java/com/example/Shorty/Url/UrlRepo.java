package com.example.Shorty.Url;


import com.example.Shorty.config.DynamoDbConfig;
import com.example.Shorty.exception.ResourceNotFoundException;
import com.example.Shorty.exception.UnauthorizedException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UrlRepo {


    private static final String TABLE_NAME = "Urls";
    private static final String SHORT_CODE_INDEX = "shortCode-index";
    private static final String USER_ID_INDEX = "userId-index";
    private static final String USER_SHORT_CODE_INDEX = "userId-shortCode-index";

    private final DynamoDbEnhancedClient dynamoDbConfig;

    private DynamoDbTable<Url> urlTable;

    @PostConstruct
    public void  init() {
        urlTable = dynamoDbConfig.table(TABLE_NAME, TableSchema.fromBean(Url.class));
    }

    public Url saveUrl(Url url) {
        urlTable.putItem(url);
        return url;
    }

    public List<Url> findUserUrls(String userId) {

        DynamoDbIndex<Url> userIndex = urlTable.index(USER_SHORT_CODE_INDEX);

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(userId).build());

        return userIndex.query(queryConditional)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    public Optional<Url> findByShortCode(String shortCode) {

        DynamoDbIndex<Url> codeIndex = urlTable.index(SHORT_CODE_INDEX);

        QueryConditional conditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(shortCode).build());

        return codeIndex.query(conditional)
                .stream()
                .findFirst()
                .flatMap(page -> page.items().stream().findFirst());
    }

    public Optional<Url> findByShortCodeAndUserId(String shortCode, String userId) {
        DynamoDbIndex<Url> index = urlTable.index(USER_SHORT_CODE_INDEX);

        QueryConditional qc = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(userId)
                        .sortValue(shortCode)
                        .build()
        );

        return index.query(qc)
                .stream()
                .findFirst()
                .flatMap(page -> page.items().stream().findFirst());
    }

    public void deleteByShortCodeAndUserId(String shortCode, String userId) {
        Url url = findByShortCodeAndUserId(shortCode, userId)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        urlTable.deleteItem(url);
    }


    public boolean exitsByShortCode(String shortCode) {
        return findByShortCode(shortCode).isPresent();
    }

    public void deleteByShortCode(String shortCode) {

        Url url = findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        Key key = Key.builder()
                .partitionValue(url.getId())
                .build();

        urlTable.deleteItem(key);
    }



}
