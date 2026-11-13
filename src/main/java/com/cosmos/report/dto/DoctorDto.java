package com.cosmos.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDto {
    private String doctorName;
    private String licenseNumber; // e.g. MED-REG-UP-88421
    private String designation;
    private String department;
    private String signatureId;   // e.g. DSIG-PKI-2026-98124
}
