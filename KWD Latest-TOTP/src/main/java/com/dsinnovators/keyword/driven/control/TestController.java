package com.dsinnovators.keyword.driven.control;

import com.dsinnovators.keyword.driven.commons.CommonFunc;
import com.dsinnovators.keyword.driven.commons.ExcelUtils;
import com.dsinnovators.keyword.driven.exception.TestFailedException;
import com.dsinnovators.keyword.driven.meta.MetaFiles;
import com.dsinnovators.keyword.driven.report.Reporter;
import com.dsinnovators.keyword.driven.service.KeywordService;
import com.dsinnovators.keyword.driven.utils.Constants;
import com.dsinnovators.keyword.driven.utils.ReportEntity;
import com.dsinnovators.keyword.driven.utils.ReportUtils;
import com.dsinnovators.keyword.driven.utils.TestHelper;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.dsinnovators.keyword.driven.properties.CliArgs.*;

@Controller
@Slf4j
public class TestController {
    private List<String> allowedBrowsersList = Arrays.asList(Constants.ALLOWED_BROWSERS.split("\\s*,\\s*"));
    private WebDriver driver;
    private List<ReportEntity> reporterList;

    private Reporter reporter;
    private KeywordService keywordService;

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    @Autowired
    public void setKeywordService(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    private List<String> initBrowsers() {
        List<String> browsers = new ArrayList<>();
        log.info("New Testcase-> Application Started");
        String browsersProp = TestHelper.getTestConfPropertyValue("allowed.browsers");
        for (String browser : browsersProp.split(",")) {
            if (allowedBrowsersList.indexOf(browser.toLowerCase()) >= 0) {
                browsers.add(browser);
            }
        }
        return browsers;
    }

    private void initialize() {
        reporterList = new ArrayList<>();
        CommonFunc.paperlessStatus = "";
        CommonFunc.CurrentPage = "";
        CommonFunc.Label = "";
        CommonFunc.labelChange = false;
        CommonFunc.conditionApply = false;
        CommonFunc.meetCondition = false;
        CommonFunc.isFinished = false;
        CommonFunc.jdbcUrl = "";
        CommonFunc.jdbcPass = "";
        CommonFunc.jdbcUser = "";
        CommonFunc.query = "";
        CommonFunc.dbResult = "";
        CommonFunc.selectionValue = "";
        CommonFunc.dirPath = "";
        Connection con = null;
        //CommonFunc.registryKeyMap = new HashMap<>();
        CommonFunc.totalRowHasChecked = 0;
        CommonFunc.totalRowFailed = 0;
        CommonFunc.totalRowPassed = 0;
    }



    public void runTest(String fileName) throws Exception {

        System.out.println("current file name: " + fileName);
        List<String> sheetNameList = new ArrayList<String>();
        ExcelUtils.setExcelFile(excelPath + "/" + fileName, TestHelper.getTestConfPropertyValue(Constants.SHEET_SEQUENCE));
        int numberOfRows = ExcelUtils.getNumberOfRows();
        for (int rowNum = 1; rowNum < numberOfRows; rowNum++) {
            String cellData = ExcelUtils.getCellStringData(rowNum, 0);
            if (cellData == null || cellData.length() == 0)
                break;
            else
                sheetNameList.add(cellData);
        }


        String currentFilePath = excelPath + "/" + fileName;
        if (sheetNameList.size() == 0) {
            throw new IllegalArgumentException("Sheet name can not be empty.");
        }

        List<String> browsers = initBrowsers();
        for (String currentBrowser : browsers) {


            this.initialize();
            CommonFunc.dirPath = dirPath;
            CommonFunc.browser = currentBrowser;
            driver = CommonFunc.setUp();
            String reportAndMetaFileName = reportAndMetaFileName(fileName);
            try {
                for (String sheetName : sheetNameList) {
                    System.out.println("currentFilePath: "+currentFilePath+" filename: "+fileName+" sheetName:"+sheetName+" reportAndMetaFileName: "+reportAndMetaFileName);
                    runTestPerSheet(currentFilePath, fileName, sheetName, reportAndMetaFileName);
                }
            } catch (Exception ex) {

                log.info("Exception in runTestPerSheet " + ex);
                boolean isCommonException=false;
                for(String commonException: CommonFunc.commonExceptions){
                    if(commonException.equals(ex.getMessage())){
                        isCommonException=true;
                    }
                }
                if(!isCommonException){
                    //ex.printStackTrace();
                    Constants.IS_ABNORMAL_TERMINATION = true;
                }
            } finally {
                reporterList.addAll(getReportSummary());
                reporter.generateReport(reporterList, reportAndMetaFileName);
            }
            if (getTestPassPercentage() < expectedResult) {
                throw new TestFailedException();
            }
            driver.close();
        }
    }

    private void runTestPerSheet(String path, String excelFileName, String sheetName, String reportAndMetaFileName) throws Exception {
        ExcelUtils.setExcelFile(path, sheetName.trim());
        log.debug("Look for excel file " + path);
        CommonFunc.isFinished = false;
        CommonFunc.Label = "";
        CommonFunc.labelChange = false;
        log.info("Current sheet name::" + sheetName);
        int numberOfRows = ExcelUtils.getNumberOfRows();
        log.info("Total rows in this sheet ::" + numberOfRows);
        //reads each row
        MetaFiles metaFiles = new MetaFiles();
        String metaFilePath = metaFiles.createMetaFile(excelFileName, sheetName, reportAndMetaFileName);
        for (int rowNum = 1; rowNum < numberOfRows; rowNum++) {
            // writes metafiles row by row

            String regKeyValue = "";
            Map<String, String> map = new HashMap<>();
            String keyName = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.REGISTRY_KEY);
            if (!TestHelper.isEmpty(keyName)) {
                String value = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
                regKeyValue = keyName + "=" + value;
                map.put("regKeyValue", regKeyValue);
            }
            map.put("lineNo", Integer.toString(rowNum + 1));
            map.put("dateTime", new SimpleDateFormat(ReportUtils.getReportConfigurationValue(Constants.REPORT_DATE_FORMAT_KEY)).format(new Date()));
            String keyWord = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.KEYWORD_COLUMN);
            if (keyWord.equalsIgnoreCase("exit")) {
                map.put("status", "Successful");
            }
            metaFiles.appendToMetaFile(metaFilePath, map);


            if (CommonFunc.isFinished) {
                log.info("Sheet name:: " + sheetName + " finished.");
                break;
            }

            if (CommonFunc.labelChange) {
                log.info("Need to change label to :: " + CommonFunc.Label);
                CommonFunc.labelChange = false;
                rowNum = 1;
                continue;
            }

            if (!TestHelper.isEmpty(CommonFunc.Label)) {
                String label = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.LABEL_COLUMN);
                if (!TestHelper.isEmpty(label) && label.equalsIgnoreCase(CommonFunc.Label)) {
                    log.info("Label named :: {} is found.", CommonFunc.Label);
                    CommonFunc.Label = "";
                    goForTheAction(rowNum);
                    continue;
                }
                String keyword = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.KEYWORD_COLUMN);
                if (!TestHelper.isEmpty(keyword) && keyword.equalsIgnoreCase("exit")) {
                    CommonFunc.isFinished = true;
                }
            } else {
                goForTheAction(rowNum);
            }
        }
    }

    private int getTestPassPercentage() {
        int percentage = 0;
        if (CommonFunc.totalRowHasChecked > 0) {
            log.info("Total Row checked :: {}", CommonFunc.totalRowHasChecked);
            log.info("Total PASSED :: {}", CommonFunc.totalRowPassed);
            log.info("Total FAILED :: {}", CommonFunc.totalRowFailed);
            percentage = (CommonFunc.totalRowPassed * 100) / CommonFunc.totalRowHasChecked;
            log.info("Pass percentage :: {}", percentage);
            log.info("Expected percentage :: {}", expectedResult);
        }
        return percentage;
    }

    private List<ReportEntity> getReportSummary() {
        List<ReportEntity> reports = new ArrayList<>();

        if (!TestHelper.isEmpty(CommonFunc.currentReason)) {

            ReportEntity reportEntity = ReportEntity.builder()
                    .testCaseName("Termination Reason")
                    .lineNo(CommonFunc.totalRowHasChecked + 1)
                    .reason(CommonFunc.currentReason)
                    .build();
            reports.add(reportEntity);

        }
        ReportEntity reportEntity = ReportEntity.builder()
                .testCaseName("Total Checked:")
                .lineNo(CommonFunc.totalRowHasChecked)
                .build();

        reports.add(reportEntity);

        reportEntity = ReportEntity.builder()
                .testCaseName("Total Passed:")
                .lineNo(CommonFunc.totalRowPassed)
                .build();

        reports.add(reportEntity);

        reportEntity = ReportEntity.builder()
                .testCaseName("Total Failed:")
                .lineNo(CommonFunc.totalRowFailed)
                .build();

        reports.add(reportEntity);
        return reports;
    }

    private void goForTheAction(int rowNum) throws Exception {
        String currentTestCaseName = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.TEST_CASE_NAME);

        if (!TestHelper.isEmpty(currentTestCaseName)) {
            CommonFunc.currentTestCaseName = currentTestCaseName;
        } else {
            CommonFunc.currentTestCaseName = "";
        }

        String keyword = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.KEYWORD_COLUMN);
        ReportEntity reportEntity = keywordService.runByKeyword(keyword, driver, rowNum);
        if (!reportEntity.getExpectedResult().equalsIgnoreCase("Ignored")) {
            reporterList.add(reportEntity);
        }
    }

    private String reportAndMetaFileName(String excelFileName) {
        String fileNameDateTime = new SimpleDateFormat(Constants.DEFAULT_REPORT_FILE_NAME_DATE_FORMAT).format(new Date());
        return excelFileName + "-" + CommonFunc.browser + "-" +
                fileNameDateTime;
    }
}
