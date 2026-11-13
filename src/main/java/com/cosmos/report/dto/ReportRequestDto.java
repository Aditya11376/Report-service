package com.cosmos.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {
    private String reportId; // format: CR(MONTH)(YEAR)(DATE)(3DIGITNUMBER)
    private PatientDto patient;
    private DoctorDto doctor;
    private BiomarkersDto biomarkers;
    private List<Double> f3Wave; // Microvolt values for F3 (Left lead)
    private List<Double> f4Wave; // Microvolt values for F4 (Right lead)
    private List<String> clinicalFindings;
}
