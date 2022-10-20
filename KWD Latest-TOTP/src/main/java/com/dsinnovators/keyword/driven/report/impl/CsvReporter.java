package com.dsinnovators.keyword.driven.report.impl;

import com.dsinnovators.keyword.driven.report.Reporter;
import com.dsinnovators.keyword.driven.utils.Constants;
import com.dsinnovators.keyword.driven.utils.ReportEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CsvReporter extends Reporter {

    @Override
    public String getExtension() {
        return  ".csv";
    }

    @Override
    public String generateReport(List<ReportEntity> reportEntityList,String reportAndMetaFileName) throws IOException {
        String reportFilePath = createReportFile(reportAndMetaFileName);

        try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(reportFilePath))) {
            fileWriter.append(getLine(getReportHeaderArray()));
            for (ReportEntity reportLine : reportEntityList) {
                fileWriter.append(getReportLine(reportLine));
            }
            fileWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return reportFilePath;
        }
    }

    private String getLine(List<String> values){
        StringBuilder line = new StringBuilder();
        int i = 1;
        for(String value: values) {
            // replace null value with empty string
            if(value == null) {
                value = "";
            }

            /* add comma delimiter after each value but
              add new line only after the last value */
            if(i != values.size()) {
                line.append(value).append(Constants.DEFAULT_DELIMITER);
            } else {
                line.append(value).append(Constants.DEFAULT_NEW_LINE_SEPARATOR);
            }
            i++;
        }

        return line.toString();
    }

    private String getReportLine(ReportEntity reportLine) {
        List<String> line = Arrays.asList(reportLine.getTime(),
                reportLine.getTestCaseName(),
                reportLine.getLineNo().toString(),
                reportLine.getResult(),
                reportLine.getReason(),
                reportLine.getComment());

        return getLine(line);
    }
}
