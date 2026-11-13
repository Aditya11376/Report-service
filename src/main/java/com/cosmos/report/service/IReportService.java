package com.cosmos.report.service;

import com.cosmos.report.dto.ReportRequestDto;

public interface IReportService {
    byte[] generateReportPdf(ReportRequestDto requestDto);
}
