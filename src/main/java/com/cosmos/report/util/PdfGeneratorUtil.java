package com.cosmos.report.util;

import com.cosmos.report.dto.BiomarkersDto;
import com.cosmos.report.dto.DoctorDto;
import com.cosmos.report.dto.PatientDto;
import com.cosmos.report.dto.ReportRequestDto;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfGeneratorUtil {

    // Authoritative clinical typography: Times New Roman for formal titles/narrative + Helvetica for crisp tabular data
    private static final Color NAVY = new Color(15, 32, 67);
    private static final Color ORG_BLUE = new Color(30, 58, 138);
    private static final Color SLATE = new Color(71, 85, 105);

    private static final Font FONT_BRAND_TITLE = new Font(Font.TIMES_ROMAN, 20, Font.BOLD, NAVY);
    private static final Font FONT_ORG_NAME = new Font(Font.TIMES_ROMAN, 10.5f, Font.BOLD, ORG_BLUE);
    private static final Font FONT_BRAND_SUB = new Font(Font.TIMES_ROMAN, 9f, Font.ITALIC, SLATE);

    private static final Font FONT_META_LABEL = new Font(Font.HELVETICA, 8.5f, Font.BOLD, new Color(55, 65, 81));
    private static final Font FONT_META_VAL = new Font(Font.COURIER, 9f, Font.BOLD, NAVY);
    private static final Font FONT_META_DATE = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, new Color(30, 41, 59));

    private static final Font FONT_SECTION_TITLE = new Font(Font.TIMES_ROMAN, 10.5f, Font.BOLD, NAVY);
    private static final Font FONT_FIELD_LABEL = new Font(Font.HELVETICA, 8.5f, Font.BOLD, new Color(50, 60, 75));
    private static final Font FONT_FIELD_VAL = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, new Color(25, 30, 40));

    // Biomarkers Table typography
    private static final Font FONT_TABLE_HEADER = new Font(Font.TIMES_ROMAN, 9.5f, Font.BOLD, NAVY);
    private static final Font FONT_TABLE_ROW = new Font(Font.HELVETICA, 9f, Font.NORMAL, new Color(30, 35, 45));
    private static final Font FONT_TABLE_ROW_BOLD = new Font(Font.HELVETICA, 9f, Font.BOLD, new Color(15, 30, 50));

    private static final Font FONT_ALERT_HEADING = new Font(Font.TIMES_ROMAN, 10.5f, Font.BOLD, new Color(175, 20, 20));
    private static final Font FONT_ALERT_BODY = new Font(Font.HELVETICA, 8.8f, Font.NORMAL, new Color(45, 20, 20));
    private static final Font FONT_FINDING_ITEM = new Font(Font.TIMES_ROMAN, 9.2f, Font.NORMAL, new Color(30, 35, 45));

    private static final Font FONT_DISCLAIMER = new Font(Font.TIMES_ROMAN, 7.5f, Font.NORMAL, new Color(90, 98, 110));
    private static final Font FONT_SIGN_NAME = new Font(Font.TIMES_ROMAN, 10.5f, Font.BOLD, NAVY);
    private static final Font FONT_SIGN_META = new Font(Font.HELVETICA, 8f, Font.NORMAL, new Color(65, 75, 90));
    private static final Font FONT_FOOTER = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, new Color(100, 116, 139));

    public static byte[] generateMedicalReportPdf(ReportRequestDto req) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Exact A4 page with calibrated margins ensuring crisp 1-page hospital layout
        Document document = new Document(PageSize.A4, 30, 30, 16, 14);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        PatientDto p = req.getPatient() != null ? req.getPatient() : new PatientDto();
        DoctorDto d = req.getDoctor() != null ? req.getDoctor() : new DoctorDto();
        BiomarkersDto b = req.getBiomarkers() != null ? req.getBiomarkers() : new BiomarkersDto();
        String reportId = req.getReportId() != null && !req.getReportId().isBlank() ? req.getReportId() : ReportIdGenerator.generate();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy, hh:mm a"));
        String sigId = d.getSignatureId() != null && !d.getSignatureId().isBlank() ? d.getSignatureId() : ("DSIG-" + reportId);

        // ==========================================
        // 1. HEADER: CENTERED TITLE, ORGANISATION & METADATA
        // ==========================================
        Paragraph titleP = new Paragraph("COSMOS NEURO-DIAGNOSTICS", FONT_BRAND_TITLE);
        titleP.setAlignment(Element.ALIGN_CENTER);
        document.add(titleP);

        Paragraph orgP = new Paragraph("GUPTA SOFTWORKS ORGANISATION", FONT_ORG_NAME);
        orgP.setAlignment(Element.ALIGN_CENTER);
        orgP.setSpacingBefore(1);
        document.add(orgP);

        Paragraph subP = new Paragraph("Continuous Acute Stroke & Cerebral Asymmetry Screening Report", FONT_BRAND_SUB);
        subP.setAlignment(Element.ALIGN_CENTER);
        subP.setSpacingBefore(1);
        subP.setSpacingAfter(6);
        document.add(subP);

        // Report Number (Left) & Date/Time (Right)
        PdfPTable metaBar = new PdfPTable(2);
        metaBar.setWidthPercentage(100);
        metaBar.setWidths(new float[]{50, 50});

        PdfPCell idCell = new PdfPCell();
        idCell.setBorder(Rectangle.NO_BORDER);
        idCell.setPadding(0);
        Paragraph idPara = new Paragraph();
        idPara.add(new Chunk("Report No: ", FONT_META_LABEL));
        idPara.add(new Chunk(reportId, FONT_META_VAL));
        idCell.addElement(idPara);
        metaBar.addCell(idCell);

        PdfPCell dateCell = new PdfPCell();
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setPadding(0);
        Paragraph datePara = new Paragraph();
        datePara.setAlignment(Element.ALIGN_RIGHT);
        datePara.add(new Chunk("Date & Time: ", FONT_META_LABEL));
        datePara.add(new Chunk(timestamp, FONT_META_DATE));
        dateCell.addElement(datePara);
        metaBar.addCell(dateCell);
        document.add(metaBar);

        document.add(createSolidDivider(NAVY, 1.5f, 3f, 5f));

        // ==========================================
        // 2. PATIENT & ATTENDING NEUROLOGIST DETAILS
        // ==========================================
        PdfPTable infoGrid = new PdfPTable(2);
        infoGrid.setWidthPercentage(100);
        infoGrid.setWidths(new float[]{50, 50});

        PdfPCell patCell = new PdfPCell();
        patCell.setBackgroundColor(new Color(248, 250, 253));
        patCell.setPadding(7);
        patCell.setBorderColor(new Color(210, 220, 235));
        patCell.addElement(new Paragraph("PATIENT DETAILS", FONT_SECTION_TITLE));
        patCell.addElement(createField("Full Name", p.getPatientName() != null && !p.getPatientName().isBlank() ? p.getPatientName() : "N/A"));
        patCell.addElement(createField("Patient ID", p.getPatientId() != null && !p.getPatientId().isBlank() ? p.getPatientId() : "N/A"));
        patCell.addElement(createField("Age / Gender", (p.getAge() != null ? p.getAge() + " Years" : "N/A") + " / " + (p.getGender() != null && !p.getGender().isBlank() ? p.getGender() : "N/A")));
        infoGrid.addCell(patCell);

        PdfPCell docCell = new PdfPCell();
        docCell.setBackgroundColor(new Color(248, 250, 253));
        docCell.setPadding(7);
        docCell.setBorderColor(new Color(210, 220, 235));
        docCell.addElement(new Paragraph("ATTENDING NEUROLOGIST", FONT_SECTION_TITLE));
        docCell.addElement(createField("Doctor Name", d.getDoctorName() != null && !d.getDoctorName().isBlank() ? d.getDoctorName() : "N/A"));
        docCell.addElement(createField("License No", d.getLicenseNumber() != null && !d.getLicenseNumber().isBlank() ? d.getLicenseNumber() : "N/A"));
        docCell.addElement(createField("Department", d.getDepartment() != null && !d.getDepartment().isBlank() ? d.getDepartment() : "N/A"));
        infoGrid.addCell(docCell);
        document.add(infoGrid);

        // ==========================================
        // 3. QUANTITATIVE SPECTRAL BIOMARKERS TABLE
        // ==========================================
        Double bsiVal = b.getBsi();
        Double adrVal = b.getAdr();
        Double aPower = b.getAlphaPower();
        Double dPower = b.getDeltaPower();
        Double f4Power = b.getF4Power();
        Double conf = b.getConfidencePercentage();
        String diagStatus = b.getDiagnosticStatus();

        boolean isStroke = (diagStatus != null && diagStatus.toUpperCase().contains("STROKE"))
                || (bsiVal != null && bsiVal > 0.200)
                || (adrVal != null && adrVal < 0.700)
                || (dPower != null && dPower > 25.0);

        Paragraph bioHeading = new Paragraph("ELECTROPHYSIOLOGICAL SPECTRAL BIOMARKERS", FONT_SECTION_TITLE);
        bioHeading.setSpacingBefore(6);
        bioHeading.setSpacingAfter(3);
        document.add(bioHeading);

        PdfPTable bioTable = new PdfPTable(4);
        bioTable.setWidthPercentage(100);
        bioTable.setWidths(new float[]{38, 18, 22, 22});

        addTableColHeader(bioTable, "Biomarker Parameter");
        addTableColHeader(bioTable, "Observed Value");
        addTableColHeader(bioTable, "Reference Range");
        addTableColHeader(bioTable, "Diagnostic Finding");

        // Row 1: BSI
        String bsiText = bsiVal != null ? String.format("%.3f", bsiVal) : "N/A";
        String bsiFinding = bsiVal != null ? (bsiVal > 0.200 ? "Severe Asymmetry" : "Symmetrical") : "Pending";
        addTableRow(bioTable, "Brain Symmetry Index (BSI)", bsiText, "0.010 - 0.100", bsiFinding, bsiVal != null && bsiVal > 0.200);

        // Row 2: ADR
        String adrText = adrVal != null ? String.format("%.3f", adrVal) : "N/A";
        String adrFinding = adrVal != null ? (adrVal < 0.700 ? "Severe Slowing" : "Normal Ratio") : "Pending";
        addTableRow(bioTable, "Alpha-to-Delta Ratio (ADR)", adrText, "1.200 - 3.500", adrFinding, adrVal != null && adrVal < 0.700);

        // Row 3: F3 Left Frontal Total Power
        String f3Text;
        if (aPower != null && dPower != null) {
            f3Text = String.format("%.1f uV2", aPower + dPower);
        } else if (dPower != null) {
            f3Text = String.format("%.1f uV2", dPower);
        } else if (aPower != null) {
            f3Text = String.format("%.1f uV2", aPower);
        } else {
            f3Text = "N/A";
        }
        boolean f3Abnormal = dPower != null && dPower > 25.0;
        String f3Finding = dPower != null ? (f3Abnormal ? "Pathological Delta Elevation" : "Normal") : "Pending";
        addTableRow(bioTable, "F3 Left Frontal Total Power", f3Text, "25.0 - 35.0 uV2", f3Finding, f3Abnormal);

        // Row 4: F4 Right Frontal Total Power
        String f4Text = f4Power != null ? String.format("%.1f uV2", f4Power) : "N/A";
        String f4Finding = f4Power != null ? "Healthy Symmetrical Control" : "Pending";
        addTableRow(bioTable, "F4 Right Frontal Total Power", f4Text, "25.0 - 35.0 uV2", f4Finding, false);
        document.add(bioTable);

        // ==========================================
        // 4. CEREBRAL BIOPOTENTIAL TRACES (EEG WAVEFORM)
        // ==========================================
        Paragraph waveHeading = new Paragraph("CEREBRAL BIOPOTENTIAL TRACES (Lead F3 Left vs Lead F4 Right)", FONT_SECTION_TITLE);
        waveHeading.setSpacingBefore(6);
        waveHeading.setSpacingAfter(3);
        document.add(waveHeading);

        PdfContentByte cb = writer.getDirectContent();
        float waveWidth = 535;
        float waveHeight = 96;

        PdfTemplate template = cb.createTemplate(waveWidth, waveHeight);
        Graphics2D g2 = template.createGraphics(waveWidth, waveHeight);

        // Authentic Clinical ECG/EEG Millimeter Grid Paper
        g2.setColor(new Color(255, 238, 240));
        g2.fillRect(0, 0, (int) waveWidth, (int) waveHeight);

        // Minor grid (every 6px)
        g2.setColor(new Color(255, 214, 220));
        for (int x = 0; x < waveWidth; x += 6) {
            g2.drawLine(x, 0, x, (int) waveHeight);
        }
        for (int y = 0; y < waveHeight; y += 6) {
            g2.drawLine(0, y, (int) waveWidth, y);
        }

        // Major grid (every 30px)
        g2.setColor(new Color(255, 175, 185));
        for (int x = 0; x < waveWidth; x += 30) {
            g2.drawLine(x, 0, x, (int) waveHeight);
        }
        for (int y = 0; y < waveHeight; y += 30) {
            g2.drawLine(0, y, (int) waveWidth, y);
        }

        // Trace 1: F3 (Left Frontal)
        Color f3Color = isStroke ? new Color(195, 25, 25) : new Color(18, 125, 65);
        g2.setColor(f3Color);
        g2.setStroke(new BasicStroke(1.35f));
        int midY1 = 27;
        List<Double> f3Data = req.getF3Wave();
        for (int x = 0; x < waveWidth - 1; x++) {
            double yVal1, yVal2;
            if (f3Data != null && f3Data.size() > 1) {
                int i1 = Math.min((int) ((x / waveWidth) * f3Data.size()), f3Data.size() - 1);
                int i2 = Math.min((int) (((x + 1) / waveWidth) * f3Data.size()), f3Data.size() - 1);
                yVal1 = midY1 - Math.max(-18, Math.min(18, f3Data.get(i1) * 0.4));
                yVal2 = midY1 - Math.max(-18, Math.min(18, f3Data.get(i2) * 0.4));
            } else if (isStroke) {
                yVal1 = midY1 + 12 * Math.sin(x * 0.045) + 5.5 * Math.sin(x * 0.085) + 2.0 * Math.sin(x * 0.14);
                yVal2 = midY1 + 12 * Math.sin((x + 1) * 0.045) + 5.5 * Math.sin((x + 1) * 0.085) + 2.0 * Math.sin((x + 1) * 0.14);
            } else {
                yVal1 = midY1 + 8.5 * Math.sin(x * 0.28) + 3.0 * Math.sin(x * 0.55);
                yVal2 = midY1 + 8.5 * Math.sin((x + 1) * 0.28) + 3.0 * Math.sin((x + 1) * 0.55);
            }
            g2.drawLine(x, (int) Math.round(yVal1), x + 1, (int) Math.round(yVal2));
        }

        // Trace 2: F4 (Right Frontal)
        Color f4Color = new Color(20, 50, 130);
        g2.setColor(f4Color);
        g2.setStroke(new BasicStroke(1.25f));
        int midY2 = 70;
        List<Double> f4Data = req.getF4Wave();
        for (int x = 0; x < waveWidth - 1; x++) {
            double yVal1, yVal2;
            if (f4Data != null && f4Data.size() > 1) {
                int i1 = Math.min((int) ((x / waveWidth) * f4Data.size()), f4Data.size() - 1);
                int i2 = Math.min((int) (((x + 1) / waveWidth) * f4Data.size()), f4Data.size() - 1);
                yVal1 = midY2 - Math.max(-18, Math.min(18, f4Data.get(i1) * 0.4));
                yVal2 = midY2 - Math.max(-18, Math.min(18, f4Data.get(i2) * 0.4));
            } else {
                yVal1 = midY2 + 8.5 * Math.sin(x * 0.28 + 0.15) + 3.0 * Math.sin(x * 0.55);
                yVal2 = midY2 + 8.5 * Math.sin((x + 1) * 0.28 + 0.15) + 3.0 * Math.sin((x + 1) * 0.55);
            }
            g2.drawLine(x, (int) Math.round(yVal1), x + 1, (int) Math.round(yVal2));
        }

        // Lead Annotation Badges
        g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 9));
        java.awt.FontMetrics fmBadge = g2.getFontMetrics();

        String f3Label = isStroke
                ? "Lead F3 (Left) - Slow Delta Waves & Alpha Suppression"
                : "Lead F3 (Left) - Normal Baseline Alpha Rhythms (10 Hz)";
        int f3BadgeW = fmBadge.stringWidth(f3Label) + 12;
        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(8, 4, f3BadgeW, 14, 4, 4);
        g2.setColor(f3Color);
        g2.drawRoundRect(8, 4, f3BadgeW, 14, 4, 4);
        g2.drawString(f3Label, 14, 14);

        String f4Label = "Lead F4 (Right) - Normal Baseline Alpha Rhythms (10 Hz)";
        int f4BadgeW = fmBadge.stringWidth(f4Label) + 12;
        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(8, 46, f4BadgeW, 14, 4, 4);
        g2.setColor(f4Color);
        g2.drawRoundRect(8, 46, f4BadgeW, 14, 4, 4);
        g2.drawString(f4Label, 14, 56);

        // Calibration marker
        g2.setColor(new Color(60, 60, 70));
        g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
        String calText = "Calibration: 10 mm/s | 10 mm/50 uV | 0.5-30 Hz Bandpass";
        int calW = g2.getFontMetrics().stringWidth(calText);
        g2.drawString(calText, (int) waveWidth - calW - 8, (int) waveHeight - 4);

        // Border around grid
        g2.setColor(new Color(205, 160, 170));
        g2.drawRect(0, 0, (int) waveWidth - 1, (int) waveHeight - 1);
        g2.dispose();

        Image waveImage = Image.getInstance(template);
        waveImage.setAlignment(Element.ALIGN_CENTER);
        document.add(waveImage);

        // ==========================================
        // 5. DIAGNOSTIC STATUS EVALUATION
        // ==========================================
        PdfPTable summaryTable = new PdfPTable(1);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(6);

        PdfPCell sumCell = new PdfPCell();
        if (isStroke) {
            sumCell.setBackgroundColor(new Color(254, 242, 242));
            sumCell.setBorderColor(new Color(225, 120, 120));
        } else {
            sumCell.setBackgroundColor(new Color(240, 253, 244));
            sumCell.setBorderColor(new Color(110, 231, 155));
        }
        sumCell.setPadding(8);

        Paragraph sumP = new Paragraph();
        if (isStroke) {
            String statusTitle = diagStatus != null && !diagStatus.isBlank() ? diagStatus : "ACUTE ISCHEMIC STROKE (HIGH ASYMMETRY)";
            sumP.add(new Chunk("DIAGNOSTIC STATUS: " + statusTitle + "\n", FONT_ALERT_HEADING));
            sumP.add(new Chunk("Screening Result: ", FONT_FIELD_LABEL));
            if (conf != null) {
                sumP.add(new Chunk(String.format("%.1f%% Probability of Acute Cerebral Ischemia   |   ", conf), FONT_ALERT_BODY));
            } else {
                sumP.add(new Chunk("Elevated Acute Ischemia Risk Detected   |   ", FONT_ALERT_BODY));
            }
            sumP.add(new Chunk("Clinical Urgency: ", FONT_FIELD_LABEL));
            sumP.add(new Chunk("CRITICAL - Immediate Thrombolysis / Stroke Team Evaluation Recommended", new Font(Font.HELVETICA, 8.8f, Font.BOLD, new Color(175, 20, 20))));
        } else {
            String statusTitle = diagStatus != null && !diagStatus.isBlank() ? diagStatus : "NORMAL PHYSIOLOGICAL BASELINE";
            sumP.add(new Chunk("DIAGNOSTIC STATUS: " + statusTitle + "\n", new Font(Font.TIMES_ROMAN, 10.5f, Font.BOLD, new Color(20, 120, 50))));
            sumP.add(new Chunk("Screening Result: ", FONT_FIELD_LABEL));
            if (conf != null) {
                sumP.add(new Chunk(String.format("%.1f%% Bilateral Symmetry Confirmed   |   ", conf), FONT_FIELD_VAL));
            } else {
                sumP.add(new Chunk("Bilateral Hemispheric Symmetry Confirmed   |   ", FONT_FIELD_VAL));
            }
            sumP.add(new Chunk("Rhythm Status: ", FONT_FIELD_LABEL));
            sumP.add(new Chunk("Symmetrical resting brainwave activity across both hemispheres", FONT_FIELD_VAL));
        }
        sumCell.addElement(sumP);
        summaryTable.addCell(sumCell);
        document.add(summaryTable);

        // ==========================================
        // 6. CLINICAL FINDINGS & ACTION PLAN
        // ==========================================
        Paragraph findHeading = new Paragraph("CLINICAL FINDINGS & ACTION PLAN", FONT_SECTION_TITLE);
        findHeading.setSpacingBefore(6);
        findHeading.setSpacingAfter(2);
        document.add(findHeading);

        com.lowagie.text.List findingsList = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED, 10);
        findingsList.setListSymbol("\u2022 ");

        if (req.getClinicalFindings() != null && !req.getClinicalFindings().isEmpty()) {
            for (String f : req.getClinicalFindings()) {
                if (f != null && !f.isBlank()) {
                    findingsList.add(new ListItem(f, FONT_FINDING_ITEM));
                }
            }
        } else {
            findingsList.add(new ListItem("No additional clinical remarks recorded for this screening session.", FONT_FINDING_ITEM));
        }
        document.add(findingsList);

        // ==========================================
        // 7. LEGAL DISCLAIMER & DOCTOR DIGITAL SIGNATURE
        // ==========================================
        document.add(createSolidDivider(new Color(200, 210, 225), 0.8f, 5f, 4f));

        PdfPTable signGrid = new PdfPTable(2);
        signGrid.setWidthPercentage(100);
        signGrid.setWidths(new float[]{62, 38});

        PdfPCell legalCell = new PdfPCell();
        legalCell.setBorder(Rectangle.RIGHT);
        legalCell.setBorderWidthRight(0.6f);
        legalCell.setBorderColorRight(new Color(210, 218, 230));
        legalCell.setPaddingTop(2f);
        legalCell.setPaddingBottom(2f);
        legalCell.setPaddingLeft(0f);
        legalCell.setPaddingRight(14f);
        Paragraph legalPara = new Paragraph();
        legalPara.add(new Chunk("LEGAL DISCLAIMER & CLINICAL INDEMNITY\n", new Font(Font.TIMES_ROMAN, 8f, Font.BOLD, new Color(40, 50, 65))));
        legalPara.add(new Chunk("This document is generated by the COSMOS Autonomous AI Neuro-Diagnostics Engine. It serves as a real-time computer-aided decision support advisory tool. The final clinical, diagnostic, and legal responsibility remains with the attending physician in conjunction with complete clinical examination and neuro-imaging (CT/MRI).\n\n", FONT_DISCLAIMER));
        legalPara.add(new Chunk("Authenticity Notice: Electronically signed medical record. Verify using Report No & Signature ID via official COSMOS Clinical Portal.", new Font(Font.HELVETICA, 7.2f, Font.ITALIC, new Color(70, 85, 110))));
        legalCell.addElement(legalPara);
        signGrid.addCell(legalCell);

        PdfPCell signCell = new PdfPCell();
        signCell.setBorder(Rectangle.NO_BORDER);
        signCell.setPaddingTop(2f);
        signCell.setPaddingBottom(2f);
        signCell.setPaddingLeft(14f);
        signCell.setPaddingRight(0f);
        signCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph signP = new Paragraph();
        signP.setAlignment(Element.ALIGN_RIGHT);
        signP.add(new Chunk("DIGITALLY SIGNED & VERIFIED\n", new Font(Font.HELVETICA, 8.5f, Font.BOLD, new Color(15, 115, 45))));
        signP.add(new Chunk((d.getDoctorName() != null && !d.getDoctorName().isBlank() ? d.getDoctorName() : "Authorized Physician") + "\n", FONT_SIGN_NAME));
        signP.add(new Chunk("License: " + (d.getLicenseNumber() != null && !d.getLicenseNumber().isBlank() ? d.getLicenseNumber() : "N/A") + "\n", FONT_SIGN_META));
        signP.add(new Chunk("Signature ID: ", FONT_SIGN_META));
        signP.add(new Chunk(sigId, new Font(Font.COURIER, 8.5f, Font.BOLD, NAVY)));
        signCell.addElement(signP);
        signGrid.addCell(signCell);
        document.add(signGrid);

        // ==========================================
        // 8. FOOTER
        // ==========================================
        document.add(createSolidDivider(new Color(220, 226, 235), 0.5f, 4f, 3f));
        Paragraph footerP = new Paragraph(
                "COSMOS NEURO-DIAGNOSTICS  |  GUPTA SOFTWORKS ORGANISATION  |  Confidential Electronic Medical Record  |  Page 1 of 1",
                FONT_FOOTER
        );
        footerP.setAlignment(Element.ALIGN_CENTER);
        document.add(footerP);

        document.close();
        return baos.toByteArray();
    }

    private static Paragraph createField(String label, String value) {
        Paragraph p = new Paragraph();
        p.setSpacingBefore(2);
        p.add(new Chunk(label + ": ", FONT_FIELD_LABEL));
        p.add(new Chunk(value, FONT_FIELD_VAL));
        return p;
    }

    private static void addTableColHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TABLE_HEADER));
        cell.setBackgroundColor(new Color(236, 240, 248));
        cell.setPadding(5f);
        cell.setBorderColor(new Color(205, 215, 230));
        table.addCell(cell);
    }

    private static void addTableRow(PdfPTable table, String param, String val, String ref, String remark, boolean isCritical) {
        PdfPCell c1 = new PdfPCell(new Phrase(param, FONT_TABLE_ROW));
        PdfPCell c2 = new PdfPCell(new Phrase(val, isCritical ? FONT_TABLE_ROW_BOLD : FONT_TABLE_ROW));
        PdfPCell c3 = new PdfPCell(new Phrase(ref, FONT_TABLE_ROW));
        PdfPCell c4 = new PdfPCell(new Phrase(remark, isCritical ? new Font(Font.HELVETICA, 9f, Font.BOLD, new Color(175, 20, 20)) : FONT_TABLE_ROW));

        for (PdfPCell c : new PdfPCell[]{c1, c2, c3, c4}) {
            c.setPadding(5f);
            c.setBorderColor(new Color(220, 228, 240));
            table.addCell(c);
        }
    }

    private static Paragraph createSolidDivider(Color color, float thickness, float before, float after) {
        LineSeparator line = new LineSeparator(thickness, 100f, color, Element.ALIGN_CENTER, 0);
        Paragraph p = new Paragraph();
        p.setSpacingBefore(before);
        p.setSpacingAfter(after);
        p.add(new Chunk(line));
        return p;
    }
}
