package com.cosmos.report.service.impl;

import com.cosmos.report.dto.ReportRequestDto;
import com.cosmos.report.exception.ReportGenerationException;
import com.cosmos.report.service.IReportService;
import com.cosmos.report.util.PdfGeneratorUtil;
import com.cosmos.report.util.ReportIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReportServiceImpl implements IReportService {

    @Override
    public byte[] generateReportPdf(ReportRequestDto requestDto) {
        try {
            if (requestDto.getReportId() == null || requestDto.getReportId().isBlank()) {
                requestDto.setReportId(ReportIdGenerator.generate());
            }
            log.info("Generating Clinical Medical Report PDF: {}", requestDto.getReportId());
            return PdfGeneratorUtil.generateMedicalReportPdf(requestDto);
        } catch (Exception e) {
            log.error("Failed to generate PDF report for ID: {}", requestDto.getReportId(), e);
            throw new ReportGenerationException("Error rendering medical PDF report: " + e.getMessage(), e);
        }
    }
}
