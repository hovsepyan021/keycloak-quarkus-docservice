package com.example.grpc;

import com.example.auth.AccessGuard;
import com.example.model.Document;
import com.example.service.DocumentService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;
import java.util.UUID;

@GrpcService
@RolesAllowed({"admin", "viewer"})
@ActivateRequestContext
public class DocumentProcessorService extends DocumentProcessorGrpc.DocumentProcessorImplBase {

    @Inject
    DocumentService documentService;

    @Inject
    SecurityIdentity identity;

    @Inject
    AccessGuard accessGuard;

    @Inject
    JsonWebToken jwt;

    @Override
    @Blocking
    public void process(DocumentRequest request, StreamObserver<DocumentResponse> responseObserver) {
        Optional<Document> docOpt;
        try {
            docOpt = documentService.findById(UUID.fromString(request.getDocumentId()));
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid UUID").asRuntimeException());
            return;
        }

        if (docOpt.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Document not found").asRuntimeException());
            return;
        }

        Document doc = docOpt.get();
        if (!accessGuard.isTenantPermitted(jwt, doc.getTenantId())) {
            responseObserver.onError(Status.PERMISSION_DENIED.withDescription("Access denied").asRuntimeException());
            return;
        }

        DocumentResponse response = DocumentResponse.newBuilder()
                .setStatus("Processed")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
