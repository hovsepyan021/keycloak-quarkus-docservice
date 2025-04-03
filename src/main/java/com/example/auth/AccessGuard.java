package com.example.auth;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class AccessGuard {
    public boolean isTenantPermitted(JsonWebToken jwt, String actualTenantId) {
        String userTenantId = jwt.getClaim("tenant_id");
        return actualTenantId.equals(userTenantId);
    }
}
