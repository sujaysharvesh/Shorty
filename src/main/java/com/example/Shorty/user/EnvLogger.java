package com.example.Shorty.user;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class EnvLogger {

    private final Environment env;

    public EnvLogger(Environment env) {
        this.env = env;
    }
        @PostConstruct
        public void logEnv() {

            log.info("===== APPLICATION ENV VALUES =====");

            log.info("Application Name     : {}", env.getProperty("spring.application.name"));
            log.info("Server Port          : {}", env.getProperty("server.port"));
            log.info("Base URL             : {}", env.getProperty("app.base-url"));

            log.info("AWS Region           : {}", env.getProperty("aws.region"));
            log.info("AWS Access Key       : {}", mask(env.getProperty("aws.credentials.access-key")));
            log.info("AWS Secret Key       : {}", mask(env.getProperty("aws.credentials.secret-key")));

            log.info("JWT Secret Loaded    : {}", env.containsProperty("jwt.secret"));
            log.info("JWT Expiration       : {}", env.getProperty("jwt.expiration"));
            log.info("JWT Refresh Exp      : {}", env.getProperty("jwt.refresh.expiration"));

            log.info("Cookie Name          : {}", env.getProperty("app.jwt.cookie-name"));
            log.info("Cookie Path          : {}", env.getProperty("app.jwt.cookie-path"));
            log.info("Cookie Domain        : {}", env.getProperty("app.jwt.cookie-domain"));
            log.info("Cookie Max Age       : {}", env.getProperty("app.jwt.cookie-max-age"));
            log.info("Cookie Environment   : {}", env.getProperty("app.environment"));

//            log.info("User Default Desc    : {}", env.getProperty("app.juser_default.description"));
//
//            log.info("IP Limit Capacity    : {}", env.getProperty("rate-limit.rules.ip_default.capacity"));
//            log.info("IP Limit Refill      : {}", env.getProperty("rate-limit.rules.ip_default.refill-rate"));
//
//            log.info("Shorten API Capacity : {}", env.getProperty("rate-limit.rules.shorten_api.capacity"));
//            log.info("Shorten API Refill   : {}", env.getProperty("rate-limit.rules.shorten_api.refill-rate"));

            log.info("==================================");
        }

        private String mask(String value) {
            if (value == null) return "NOT_SET";
            return "****MASKED****";
        }

}