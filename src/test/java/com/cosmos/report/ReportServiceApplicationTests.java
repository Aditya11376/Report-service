package com.cosmos.report;

import com.cosmos.report.dto.BiomarkersDto;
import com.cosmos.report.dto.DoctorDto;
import com.cosmos.report.dto.PatientDto;
import com.cosmos.report.dto.ReportRequestDto;
import com.cosmos.report.service.IReportService;
import com.cosmos.report.util.ReportIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ReportServiceApplicationTests {

    @Autowired
    private IReportService reportService;

    @Test
    void testGenerateReportPdf() {
        ReportRequestDto request = ReportRequestDto.builder()
                .reportId(ReportIdGenerator.generate())
                .patient(PatientDto.builder()
                        .patientName("Ramesh Kumar")
                        .patientId("PT2026-C0104")
                        .age(62)
                        .gender("Male")
                        .build())
                .doctor(DoctorDto.builder()
                        .doctorName("Dr. Anand K. Varma, MD, DM")
                        .licenseNumber("MED-REG-UP-88421")
                        .designation("Consultant Neurologist")
                        .department("Department of Neuro-Critical Care")
                        .signatureId("DSIG-PKI-2026-98124")
                        .build())
                .biomarkers(BiomarkersDto.builder()
                        .bsi(0.384)
                        .adr(0.420)
                        .alphaPower(4.2)
                        .deltaPower(46.8)
                        .f4Power(28.5)
                        .confidencePercentage(96.4)
                        .diagnosticStatus("ACUTE ISCHEMIC STROKE (HIGH RISK)")
                        .build())
                .clinicalFindings(List.of(
                        "Significant hemispheric electrophysiological asymmetry observed across F3-F4 leads.",
                        "Left frontal alpha attenuation accompanied by focal delta wave slowing (BSI = 0.384, ADR = 0.420).",
                        "Immediate clinical correlation advised: Urgent Non-Contrast Head CT and Acute Stroke Triage Protocol."
                ))
                .build();

        byte[] pdfBytes = reportService.generateReportPdf(request);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000);
    }
}
