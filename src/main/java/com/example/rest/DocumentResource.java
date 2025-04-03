package com.example.rest;

import com.example.auth.AccessGuard;
import com.example.model.Document;
import com.example.service.DocumentService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;
import java.util.UUID;

@Path("/documents")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentResource {

    @Inject
    DocumentService documentService;

    @Inject
    JsonWebToken jwt;

    @Inject
    SecurityIdentity identity;

    @Inject
    AccessGuard accessGuard;

    @POST
    @RolesAllowed("admin")
    public Response createDocument(Document document) {
        String tenantId = jwt.getClaim("tenant_id");
        document.setTenantId(tenantId);

        Document saved = documentService.create(document);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"admin", "viewer"})
    public Response getDocument(@PathParam("id") UUID id) {
        Optional<Document> doc = documentService.findById(id);
        if (doc.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // ABAC check: only allow access if tenantId matches
        if (!accessGuard.isTenantPermitted(jwt, doc.get().getTenantId())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.ok(doc.get()).build();
    }
}
