package com.cosmos.report.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportIdGenerator {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private static volatile String lastDateKey = "";

    /**
     * Generates report ID in exact format: CR(MONTH)(YEAR)(DATE)(3DIGITNUMBER)
     * Example: CR11202604001 (Month 11, Year 2026, Day 04, Sequence 001)
     */
    public static synchronized String generate() {
        LocalDate now = LocalDate.now();
        String month = String.format("%02d", now.getMonthValue());
        String year = String.valueOf(now.getYear());
        String day = String.format("%02d", now.getDayOfMonth());

        String dateKey = month + year + day;
        if (!dateKey.equals(lastDateKey)) {
            lastDateKey = dateKey;
            COUNTER.set(1);
        }

        int seq = COUNTER.getAndIncrement();
        if (seq > 999) {
            COUNTER.set(1);
            seq = 1;
        }

        return String.format("CR%s%s%s%03d", month, year, day, seq);
    }
}
