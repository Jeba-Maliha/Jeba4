package com.dsinnovators.keyword.driven.factory;

import com.dsinnovators.keyword.driven.report.Reporter;
import com.dsinnovators.keyword.driven.report.impl.CsvReporter;
import com.dsinnovators.keyword.driven.report.impl.HtmlReporter;

public class ReporterFactory {
    private Reporter reporter;

    public Reporter getReporter(String type) {
        if(type.equalsIgnoreCase("html")) {
            reporter = new HtmlReporter();
        } else if(type.equalsIgnoreCase("csv")) {
            reporter = new CsvReporter();
        } else {
            throw new IllegalArgumentException("Invalid Report Output Format.");
        }
        return reporter;
    }
}
