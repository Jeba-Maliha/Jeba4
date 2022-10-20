package com.dsinnovators.keyword.driven.report.impl;

import com.dsinnovators.keyword.driven.report.Reporter;
import com.dsinnovators.keyword.driven.utils.Constants;
import com.dsinnovators.keyword.driven.utils.ReportEntity;
import com.dsinnovators.keyword.driven.utils.ReportUtils;
import com.dsinnovators.keyword.driven.utils.TestHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HtmlReporter extends Reporter {
    @Override
    public String getExtension() {
        return ".html";
    }

    @Override
    public String generateReport(List<ReportEntity> reportEntityList,String reportAndMetaFileName) throws IOException {
        String reportFilePath = createReportFile(reportAndMetaFileName);

        String htmlTemplateTop = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "table {\n" +
                "  font-family: arial, sans-serif;\n" +
                "  font-size: 13px;" +
                "  border-collapse: collapse;\n" +
                "  width: 100%;\n" +
                "}\n" +
                "\n" +
                "td, th {\n" +
                "  border: 1px solid #c5c5c5;\n" +
                "  text-align: left;\n" +
                "  padding: 8px;\n" +
                "}\n" +
                "\n" +
                "tr:nth-child(even) {\n" +
                "  background-color: #dddddd;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<h2>Test Report</h2>";

        try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(reportFilePath))) {
            fileWriter
                    .append(htmlTemplateTop)
                    .append("<table>");
            fileWriter.append(getLine(getReportHeaderArray()));
            for (ReportEntity reportLine : reportEntityList) {
                fileWriter.append(getReportLine(reportLine));
            }

            fileWriter
                    .append("</table>")
                    .append("</body></html>");
            fileWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            return reportFilePath;
        }
    }

    private String getLine(List<String> values){
        StringBuilder line = new StringBuilder();

        line.append("<tr>");
        for(String value: values) {
            // replace null value with empty string
            if(value == null) {
                value = "";
            }
            line.append("<td>")
                .append(value)
                .append("</td>");
        }
        line.append("</tr>");

        return line.toString();
    }


    private String emtifyNull(String s) {
        return s == null ? "" : s;
    }

    private String getReportLine(ReportEntity reportLine) {
        StringBuilder line = new StringBuilder();

        String time = emtifyNull(reportLine.getTime());
        String testCaseName = emtifyNull(reportLine.getTestCaseName());
        String lineNumber = emtifyNull(reportLine.getLineNo().toString());
        String result = emtifyNull(reportLine.getResult());
        String expectedResult = emtifyNull(reportLine.getExpectedResult());
        String reason = emtifyNull(reportLine.getReason());
        String comment = emtifyNull(reportLine.getComment());

        line.append("<tr>")
                .append("<td>").append(time).append("</td>")
                .append("<td>").append(testCaseName).append("</td>")
                .append("<td>").append(lineNumber).append("</td>");


        if( ("failed").equalsIgnoreCase(result) ) {
            line.append("<td style=\"background:#fdb9b9\">");
        } else if( ("passed").equalsIgnoreCase(result) ) {
            line.append("<td style=\"background:#b6d8ca\">");
        } else {
            line.append("<td>");
        }

        line.append(result).append("</td>");

        line.append("<td style=\"width:20%\">").append(reason).append("</td>")
                .append("<td>").append(comment).append("</td>")
                .append("</tr>");

        return line.toString();
    }
}
