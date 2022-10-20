package com.dsinnovators.keyword.driven;

import com.dsinnovators.keyword.driven.commons.CommonFunc;
import com.dsinnovators.keyword.driven.control.TestController;
import com.dsinnovators.keyword.driven.email.SendTestReport;
import com.dsinnovators.keyword.driven.exception.TestFailedException;
import com.dsinnovators.keyword.driven.factory.ReporterFactory;
import com.dsinnovators.keyword.driven.meta.MetaFiles;
import com.dsinnovators.keyword.driven.properties.CliArgs;
import com.dsinnovators.keyword.driven.report.Reporter;
import com.dsinnovators.keyword.driven.service.CliParser;
import com.dsinnovators.keyword.driven.utils.Constants;
import com.dsinnovators.keyword.driven.utils.ExcelFileFilter;
import com.dsinnovators.keyword.driven.utils.TestHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootApplication
@Slf4j
public class Main implements CommandLineRunner {
    private final CliParser parser;

    private final TestController testController;


    @Autowired
    public Main(TestController testController, CliParser parser) {
        this.testController = testController;
        this.parser = parser;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        parser.parse(args);

        SendTestReport sendTestReport = new SendTestReport();
        MetaFiles metaFiles = new MetaFiles();
        List<String> metaFilesList = metaFiles.metaTextFiles();
        if(metaFilesList.size()>0){
            sendTestReport.sendReport(TestHelper.getTestConfPropertyValue(Constants.REPORT_FAILED_MESSAGE),metaFilesList);
            metaFiles.deleteFiles();
        }

        FileFilter filter = new ExcelFileFilter();
        File directory = new File(TestHelper.getTestConfPropertyValue(Constants.EXCEL_PATH));
        File[] files = directory.listFiles(filter);
        Arrays.sort(files);

        for (File file : files) {
            Reporter reporter = new ReporterFactory().getReporter(CliArgs.reportFormat);
            try {
                testController.setReporter(reporter);
                testController.runTest(file.getName());
            } catch (TestFailedException e) {
                //log.info("Expected test result didn't met. TEST FAILED!");
            }
        }
        System.out.println("is abnormal? "+Constants.IS_ABNORMAL_TERMINATION);

        metaFilesList = metaFiles.metaTextFiles();
        if(!Constants.IS_ABNORMAL_TERMINATION) {
            sendTestReport.sendReport(TestHelper.getTestConfPropertyValue(Constants.REPORT_SUCCESS_MESSAGE), metaFilesList);
            metaFiles.deleteFiles();
        }

    }

}
