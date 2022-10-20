package com.dsinnovators.keyword.driven.report;

import com.dsinnovators.keyword.driven.commons.CommonFunc;
import com.dsinnovators.keyword.driven.properties.CliArgs;
import com.dsinnovators.keyword.driven.utils.Constants;
import com.dsinnovators.keyword.driven.utils.ReportEntity;
import com.dsinnovators.keyword.driven.utils.ReportUtils;
import com.dsinnovators.keyword.driven.utils.TestHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class Reporter {
    private String getReportFileName(String reportAndMetaFileName) {
        String reportPath = CliArgs.reportPath;
        if(reportPath.substring(reportPath.length() - 1).equalsIgnoreCase("/")||reportPath.substring(reportPath.length() - 1).equalsIgnoreCase("\\")){
            reportPath = reportPath.substring(0,reportPath.length()-1);
        }
        return reportPath + File.separator + "report-"+ reportAndMetaFileName +getExtension();
    }

    public String createReportFile(String reportAndMetaFileName) throws IOException {
        String reportFilePath = getReportFileName(reportAndMetaFileName);

        File file = new File(reportFilePath);
        log.debug("Try to create report file to :: " + file);

        if (!file.exists()) {
            String absolutePath = file.getAbsolutePath();
            String fileFolderPath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));

            if (!new File(fileFolderPath).mkdirs() && !file.createNewFile()) {
                throw new IOException("Couldn't create report file.");
            }
        }
        return reportFilePath;
    }

    public ArrayList<String> getReportHeaderArray() throws Exception {
        String rawHeader = ReportUtils.getReportConfigurationValue("report.file.header");
        ArrayList<String> finalHeaders = new ArrayList<>();
        if(TestHelper.isEmpty(rawHeader)){
            throw new Exception("report configuration file not found");
        } else {
            String[] headers = rawHeader.split("[|]");
            for (String header : headers) {
                if (!TestHelper.isEmpty(header)) {
                    finalHeaders.add(header.trim());
                }
            }
        }
        return finalHeaders;
    }
    public abstract String getExtension();
    public abstract String generateReport(List<ReportEntity> reportEntities,String reportAndMetaFileName) throws IOException;
}
