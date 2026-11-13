package com.cosmos.report.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtClaimParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Extracts verified doctor claims from standard JWT Bearer token
     * without direct database coupling (Database-Per-Service pattern).
     */
    public static Map<String, String> extractDoctorClaims(String authHeader) {
        Map<String, String> claims = new HashMap<>();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return claims;
        }

        try {
            String token = authHeader.substring(7).trim();
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
                String json = new String(decoded, StandardCharsets.UTF_8);
                JsonNode node = MAPPER.readTree(json);

                if (node.has("name")) claims.put("doctorName", node.get("name").asText());
                if (node.has("doctorName")) claims.put("doctorName", node.get("doctorName").asText());
                if (node.has("license")) claims.put("licenseNumber", node.get("license").asText());
                if (node.has("licenseNumber")) claims.put("licenseNumber", node.get("licenseNumber").asText());
                if (node.has("designation")) claims.put("designation", node.get("designation").asText());
                if (node.has("department")) claims.put("department", node.get("department").asText());
                if (node.has("sub")) claims.put("email", node.get("sub").asText());
                if (node.has("signatureId")) claims.put("signatureId", node.get("signatureId").asText());
                if (node.has("role")) claims.put("role", node.get("role").asText());
                if (node.has("roles")) claims.put("role", node.get("roles").asText());
            }
        } catch (Exception e) {
            log.warn("Could not decode JWT claims from header: {}", e.getMessage());
        }

        return claims;
    }
}
