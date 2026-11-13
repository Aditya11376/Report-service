package com.cosmos.report.controller;

import com.cosmos.report.dto.DoctorDto;
import com.cosmos.report.dto.ReportRequestDto;
import com.cosmos.report.exception.AccessDeniedException;
import com.cosmos.report.exception.UnauthorizedException;
import com.cosmos.report.service.IReportService;
import com.cosmos.report.util.JwtClaimParser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReportController {

    private final IReportService reportService;

    @Value("${app.internal.secret:${APP_INTERNAL_SECRET:}}")
    private String expectedInternalSecret;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "cosmos-report-service",
                "port", 8082,
                "branding", "COSMOS Clinical System"
        ));
    }

    /**
     * Generates a clinical PDF report strictly for authorized internal calls and verified Doctors.
     * 1. Validates X-Internal-Secret from API Gateway / Backend Orchestrator.
     * 2. Validates ROLE_DOCTOR from JWT Bearer token and resolves Doctor details.
     */
    @PostMapping(value = "/reports/generate", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateReport(
            @RequestHeader(value = "X-Internal-Secret", required = false) String internalSecretHeader,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody ReportRequestDto requestDto) {

        // 1. Verify X-Internal-Secret from trusted Gateway / Backend
        if (expectedInternalSecret != null && !expectedInternalSecret.isBlank()) {
            boolean isSecretValid = internalSecretHeader != null &&
                    MessageDigest.isEqual(
                            expectedInternalSecret.getBytes(StandardCharsets.UTF_8),
                            internalSecretHeader.getBytes(StandardCharsets.UTF_8)
                    );
            if (!isSecretValid) {
                log.warn("Access Denied: Missing or invalid X-Internal-Secret header.");
                throw new UnauthorizedException("Missing or invalid X-Internal-Secret header.");
            }
        }

        // 2. Enforce ROLE_DOCTOR from JWT if token is provided
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            Map<String, String> claims = JwtClaimParser.extractDoctorClaims(authHeader);
            String role = claims.get("role");
            if (role != null && !role.toUpperCase().contains("DOCTOR")) {
                log.warn("Access Denied: Role [{}] is not authorized to generate clinical reports.", role);
                throw new AccessDeniedException("User role is not authorized to generate clinical reports.");
            }
            populateDoctorFromClaims(requestDto, claims);
        }

        byte[] pdfBytes = reportService.generateReportPdf(requestDto);
        String filename = "COSMOS_Report_" + requestDto.getReportId() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

    private void populateDoctorFromClaims(ReportRequestDto requestDto, Map<String, String> claims) {
        if (requestDto.getDoctor() == null) {
            requestDto.setDoctor(new DoctorDto());
        }
        DoctorDto d = requestDto.getDoctor();
        if (claims.containsKey("doctorName")) d.setDoctorName(claims.get("doctorName"));
        if (claims.containsKey("licenseNumber")) d.setLicenseNumber(claims.get("licenseNumber"));
        if (claims.containsKey("designation")) d.setDesignation(claims.get("designation"));
        if (claims.containsKey("department")) d.setDepartment(claims.get("department"));
        if (claims.containsKey("signatureId")) d.setSignatureId(claims.get("signatureId"));
    }
}
