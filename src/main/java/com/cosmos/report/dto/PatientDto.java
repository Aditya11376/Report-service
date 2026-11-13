package com.cosmos.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {
    private String patientName;
    private String patientId; // e.g. PT2026-C0104 (Managed via Auth Service / Patient Registry)
    private Integer age;
    private String gender;
}
