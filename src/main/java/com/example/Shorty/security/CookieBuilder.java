package com.example.Shorty.security;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;


@Component
public class CookieBuilder {

    @Value("${app.jwt.cookie-name}")
    private String jwtCookieName;

    @Value("${app.jwt.cookie-path}")
    private String cookiePath;

    @Value("${app.jwt.cookie-domain}")
    private String cookieDomain;

    @Value("${app.jwt.cookie-max-age}")
    private int cookieMaxAge;

    @Value("${app.environment}")
    private String environment;

    @Value("${app.jwt.cookie-samesite}")
    private String sameSite;


    public void setJwtCookie(HttpServletResponse response, String token) {
        boolean isProduction = isProduction();
        boolean isSecure = isProduction;

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(jwtCookieName, token)
                .httpOnly(true)
                .secure(isSecure)
                .path(cookiePath)
                .maxAge(cookieMaxAge)
                .sameSite(sameSite);

        if(!cookieDomain.isEmpty() && !cookieDomain.contains("localhost")) {
            cookieBuilder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = cookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

    }

    public void logoutCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(jwtCookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(isProduction());
        cookie.setPath(cookiePath);
        cookie.setMaxAge(0);

        if (!cookieDomain.isEmpty() && !cookieDomain.contains("localhost")) {
            cookie.setDomain(cookieDomain);
        }
        response.addCookie(cookie);

    }

    private boolean isProduction() {
        return environment != null &&
                environment.equalsIgnoreCase("production");
    }



}
