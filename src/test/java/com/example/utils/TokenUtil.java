package com.example.utils;

import io.restassured.http.ContentType;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class TokenUtil {

    public static String getToken(String username, String password) {
        return given()
                .contentType(ContentType.URLENC)
                .formParams(Map.of(
                        "client_id", "documentservice",
                        "client_secret", "QvuDMibZZpkEa1hig0K9s3kHXQg9nhep",
                        "grant_type", "password",
                        "username", username,
                        "password", password,
                        "scope", "openid"
                ))
                .when()
                .post("http://localhost:8080/realms/test-realm/protocol/openid-connect/token")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("access_token");
    }
}
