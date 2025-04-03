package com.example.rest;

import com.example.model.Document;
import com.example.utils.TokenUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class DocumentResourceIT {

    @Test
    public void testCreateAndAccessDocument_happyPath() {
        String adminToken = TokenUtil.getToken("admin-user", "admin123");
        String viewerToken = TokenUtil.getToken("viewer-user", "viewer123");
        // Create document
        String docId = createDocument(adminToken);

        // Viewer can access it (same tenant)
        String loadedDocumentId = loadDocumentById(viewerToken, docId);
        assertEquals(loadedDocumentId, docId);
    }

    @Test
    public void testCreateAndAccessDocument_viewerFromAnotherTenant_cannotAccessDocument() {
        String adminToken = TokenUtil.getToken("admin-user", "admin123");
        String otherViewerToken = TokenUtil.getToken("viewer-other-tenant", "viewer123");

        String docId = createDocument(adminToken);

        // Viewer from another tenant tries to access
        given()
                .auth().oauth2(otherViewerToken)
                .when()
                .get("/documents/" + docId)
                .then()
                .statusCode(403); // Forbidden
    }

    @Test
    public void testCreateAndAccessDocument_viewer_cannotCreateDocument() {
        String viewerToken = TokenUtil.getToken("viewer-user", "viewer123");

        given()
                .auth().oauth2(viewerToken)
                .contentType(ContentType.JSON)
                .body(new Document(null, "Blocked", "Should fail", null))
                .when()
                .post("/documents")
                .then()
                .statusCode(403); // Forbidden
    }

    @Test
    public void testCreateAndAccessDocument_anonymous_cannotAccessDocument() {
        String adminToken = TokenUtil.getToken("admin-user", "admin123");
        String docId = createDocument(adminToken);

        given()
                .when()
                .get("/documents/" + docId)
                .then()
                .statusCode(401); // Unauthorized
    }

    @Test
    public void testCreateAndAccessDocument_adminFromAnotherTenant_cannotAccessDocument() {
        String adminToken = TokenUtil.getToken("admin-user", "admin123");
        String otherAdminToken = TokenUtil.getToken("admin-other-tenant", "admin123");

        String docId = createDocument(adminToken);

        given()
                .auth().oauth2(otherAdminToken)
                .when()
                .get("/documents/" + docId)
                .then()
                .statusCode(403); // ABAC rule violation
    }

    @Test
    public void testAccessNonExistentDocument_shouldReturn404() {
        String viewerToken = TokenUtil.getToken("viewer-user", "viewer123");

        given()
                .auth().oauth2(viewerToken)
                .when()
                .get("/documents/" + UUID.randomUUID())
                .then()
                .statusCode(404);
    }


    private static String loadDocumentById(String viewerToken, String docId) {
        return given()
                .auth().oauth2(viewerToken)
                .when()
                .get("/documents/" + docId)
                .then()
                .statusCode(200)
                .body("id", equalTo(docId))
                .extract()
                .path("id");
    }

    private static String createDocument(String token) {
        return given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(new Document(null, "IT test doc", "Integration test content", null))
                .when()
                .post("/documents")
                .then()
                .statusCode(201)
                .body("title", equalTo("IT test doc"))
                .extract()
                .path("id");
    }
}
