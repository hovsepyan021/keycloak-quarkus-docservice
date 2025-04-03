package com.example.grpc;

import com.example.model.Document;
import com.example.service.DocumentService;
import com.example.utils.TokenUtil;
import io.grpc.*;
import io.grpc.stub.MetadataUtils;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class DocumentProcessorGrpcIT {

    public static final int GRPC_PORT = 8081;
    @Inject
    DocumentService documentService;

    @Test
    public void testProcess_validTenant_shouldReturnProcessed() {
        String adminToken = TokenUtil.getToken("admin-user", "admin123");
        String docId = createDocForTenant("tenant-1");

        var stub = buildStubWithMetadata(adminToken);
        DocumentResponse response = stub.process(DocumentRequest.newBuilder().setDocumentId(docId).build());
        assertEquals("Processed", response.getStatus());
    }

    @Test
    public void testProcess_viewerFromSameTenant_shouldSucceed() {
        String viewerToken = TokenUtil.getToken("viewer-user", "viewer123");
        String docId = createDocForTenant("tenant-1");

        var stub = buildStubWithMetadata(viewerToken);
        DocumentResponse response = stub.process(DocumentRequest.newBuilder().setDocumentId(docId).build());
        assertEquals("Processed", response.getStatus());
    }

    @Test
    public void testProcess_viewerFromOtherTenant_shouldBeForbidden() {
        String viewerToken = TokenUtil.getToken("viewer-other-tenant", "viewer123");
        String docId = createDocForTenant("tenant-1");

        var stub = buildStubWithMetadata(viewerToken);
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                stub.process(DocumentRequest.newBuilder().setDocumentId(docId).build()));
        assertEquals(Status.PERMISSION_DENIED.getCode(), ex.getStatus().getCode());
    }

    @Test
    public void testProcess_adminFromOtherTenant_shouldBeForbidden() {
        String otherAdminToken = TokenUtil.getToken("admin-other-tenant", "admin123");
        String docId = createDocForTenant("tenant-1");

        var stub = buildStubWithMetadata(otherAdminToken);
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                stub.process(DocumentRequest.newBuilder().setDocumentId(docId).build()));
        assertEquals(Status.PERMISSION_DENIED.getCode(), ex.getStatus().getCode());
    }

    @Test
    public void testProcess_anonymousUser_shouldFailWithUnauthenticated() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", GRPC_PORT).usePlaintext().build();
        var stub = DocumentProcessorGrpc.newBlockingStub(channel);

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                stub.process(DocumentRequest.newBuilder().setDocumentId(UUID.randomUUID().toString()).build()));
        assertEquals(Status.PERMISSION_DENIED.getCode(), ex.getStatus().getCode());
    }

    @Test
    public void testProcess_nonExistentDocument_shouldReturnNotFound() {
        String token = TokenUtil.getToken("admin-user", "admin123");
        var stub = buildStubWithMetadata(token);

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                stub.process(DocumentRequest.newBuilder().setDocumentId(UUID.randomUUID().toString()).build()));
        assertEquals(Status.NOT_FOUND.getCode(), ex.getStatus().getCode());
    }

    private String createDocForTenant(String tenantId) {
        Document doc = new Document(null, "gRPC Test", "gRPC Content", tenantId);
        return documentService.create(doc).getId().toString();
    }

    private static Metadata createAuthMetadata(String token) {
        Metadata metadata = new Metadata();
        Metadata.Key<String> authKey = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(authKey, "Bearer " + token);
        return metadata;
    }

    private static DocumentProcessorGrpc.DocumentProcessorBlockingStub buildStubWithMetadata(String token) {
        Metadata metadata = createAuthMetadata(token);
        ClientInterceptor interceptor = MetadataUtils.newAttachHeadersInterceptor(metadata);
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", GRPC_PORT).usePlaintext().build();
        return DocumentProcessorGrpc.newBlockingStub(channel).withInterceptors(interceptor);
    }
}
