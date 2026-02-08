package com.example.Shorty.user;

import com.example.Shorty.config.DynamoDbConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepo {

    private final DynamoDbEnhancedClient dynamoDbConfig;
    private static final String TABLE_NAME = "Users";
    private static final String EMAIL_INDEX = "email-index";
    private static final String API_KEY_INDEX = "apiKey-index";

    private DynamoDbTable<User> userTable;

    @PostConstruct
    public void init() {
        userTable = dynamoDbConfig.table(TABLE_NAME, TableSchema.fromBean(User.class));
    }

    public User saveUser(User user) {
        userTable.putItem(user);
        return user;
    }

    public Optional<User> findByUserId(String userId) {
        return Optional.ofNullable(
                userTable.getItem(Key.builder().partitionValue(userId).build())
        );
    }

    public Optional<User> findByEmail(String email) {
        DynamoDbIndex<User> emailIndex = userTable.index(EMAIL_INDEX);

        QueryConditional conditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(email).build());

        return emailIndex.query(conditional)
                .stream()
                .findFirst()
                .flatMap(page -> page.items().stream().findFirst());

    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }



}
