package com.dsinnovators.keyword.driven.service;

import com.dsinnovators.keyword.driven.commons.CommonFunc;
import com.dsinnovators.keyword.driven.commons.ExcelUtils;
import com.dsinnovators.keyword.driven.engine.KeywordEngine;
import com.dsinnovators.keyword.driven.utils.Constants;
import com.dsinnovators.keyword.driven.utils.ReportEntity;
import com.dsinnovators.keyword.driven.utils.ReportUtils;
import com.dsinnovators.keyword.driven.utils.TestHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class KeywordService {
    public ReportEntity runByKeyword(String keyword, WebDriver driver, int rowNum) throws Exception {
        ReportEntity reportEntity = null;
        Date currentDate = new Date();
        CommonFunc.checkResult = false;
        CommonFunc.currentResult = true;
        CommonFunc.currentReason = "";


        try {
            CommonFunc.fixedWaitBeforeEachSteps();
            KeywordEngine keywordEngine = new KeywordEngine(driver);
            if (!TestHelper.isEmpty(keyword)) {
                log.info("keyWord:: " + " " + keyword + " ::step:: " + (rowNum + 1));
                log.info("time::" + new SimpleDateFormat(ReportUtils.getReportConfigurationValue(Constants.REPORT_DATE_FORMAT_KEY)).format(new Date()));
                String keywordLowerCase = keyword.toLowerCase();

                List<String> checkableKeyword =  Arrays.asList("settext", "settextjs", "gettext","select","unselect","click","clear",
                        "hasattribute","hassamevalue","gotolabel","gettitle","assertvalue","assertlikematch","assertcss",
                        "assertselection", "assertpresence","dbhasresult","haselement","setlabelbasedassertion","gotourl",
                        "reload","rightclick","clear","close","dynamicclick","dynamicselect","dropdown","dropdownoptioncount",
                        "hover","scrolldown","pagination","paginationcamp","clicks","switchalert","switchwindow","switchframe",
                        "popupalert","injectelement","dburl","dbusername","dbpassword","dbquery","dbgetcolumn","dbgetrowcount",
                        "dbresult","addregkey","getregkey","wait","function","modify","date","exit","executekeyevent","incnum",
                        "restapiget","restapipost","restapiput","restapidelete","rest","reverse_rest");

                if (checkableKeyword.contains(keywordLowerCase)) {
                    CommonFunc.checkResult = true;
                }

                switch (keywordLowerCase) {
                    case "gotourl":
                        log.info("check first url " + rowNum);
                        keywordEngine.gotoURL(rowNum);
                        break
                                ;
                    case "settext":
                        keywordEngine.setText(rowNum);
                        break;
                    case "settextjs":
                        keywordEngine.settextjs(rowNum);
                        break
                                ;
                    case "scrolldown":
                        keywordEngine.scrollDown(rowNum);
                        break;
                    case "reload":
                        keywordEngine.reload(rowNum);
                        break;
                    case "browserback":
                        keywordEngine.goBrowserBack(rowNum);
                        break;
                    case "gettext":
                        keywordEngine.getText(rowNum);
                        break;
                    case "setauthotp":
                        keywordEngine.setAuthOtp(rowNum);
                        break;

                    case "getinputtext":
                        keywordEngine.getInputText(rowNum);
                        break;
                    case "select":
                        keywordEngine.select(rowNum, true);
                        break;
                    case "unselect":
                        keywordEngine.select(rowNum, false);
                        break;
                    case "click":
                        keywordEngine.click(rowNum);
                        break;
                    case "rightclick":
                        keywordEngine.rightClick(rowNum);
                        break;
                    case "draganddrop":
                        keywordEngine.dragAndDrop(rowNum);
                        break;
                    case "clear":
                        keywordEngine.clear(rowNum);
                        break;
                    case "dynamicclick":
                        keywordEngine.dynamicClick(rowNum);
                        break;
                    case "close":
                        keywordEngine.close(rowNum);
                        break;
                    case "dynamicselect":
                        keywordEngine.dynamicSelect(rowNum);
                        break;
                    case "dropdown":
                        keywordEngine.option(rowNum);
                        break;
                    case "dropdownoptioncount":
                        keywordEngine.dropdownOptionCount(rowNum);
                        break;
                    case "hover":
                        keywordEngine.hover(rowNum);
                        break;
                    case "clickmultiplelinks":
                        keywordEngine.clickMultiplelinks(rowNum);
                        break;
                    case "hasattribute":
                        CommonFunc.currentResult = keywordEngine.hasAttribute(rowNum);
                        break;
                    case "pagination":
                        keywordEngine.Pagination(rowNum);
                        break;
                    case "paginationcamp":
                        keywordEngine.PaginationCamp(rowNum);
                        break;
                    case "clicks":
                        keywordEngine.clicks(rowNum);
                        break;
                    case "setfaker":
                        keywordEngine.setFaker(rowNum);
                        break;
                    case "switchalert":
                        keywordEngine.switchAlert(rowNum);
                        break;
                    case "switchwindow":
                        keywordEngine.switchWindow(rowNum);
                        break;
                    case "switchframeportal":
                        keywordEngine.switchFramePortal(rowNum);
                        break;
                    case "switchframe":
                        keywordEngine.switchFrame(rowNum);
                        break;
                    case "popupalert":
                        keywordEngine.popUpAlert(rowNum);
                        break;
                    case "injectelement":
                        keywordEngine.ElementExcecutor(rowNum);
                        break;
                    case "setdatesettext":
                        keywordEngine.SetDateSetText(rowNum);
                        break;
                    case "dropdownvaluecount":
                        keywordEngine.dropdownValueCount(rowNum);
                        break;
                    case "commondropdownoptioncount":
                        keywordEngine.commonDropdownOptionCount(rowNum);
                        break;
                    case "jsclick":
                        keywordEngine.JsClick(rowNum);
                        break;
                    case "gotolabel":
                        if (CommonFunc.conditionApply) {
                            if (CommonFunc.meetCondition) {
                                CommonFunc.labelChange = true;
                                CommonFunc.Label = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
                                CommonFunc.meetCondition = false;
                                CommonFunc.conditionApply = false;
                            }
                        } else {
                            CommonFunc.labelChange = true;
                            CommonFunc.Label = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
                            log.info("Next label ::  " + CommonFunc.Label);
                        }
                        break;
                    case "gettitle":
                        keywordEngine.getTitle(rowNum);
                        break;
                    case "assertvalue":
                        CommonFunc.currentResult = keywordEngine.assertValue(rowNum);
                        break;
                    case "assertlikematch":
                        CommonFunc.currentResult = keywordEngine.assertLikeMatch(rowNum);
                        break;
                    case "assertcss":
                        CommonFunc.currentResult = keywordEngine.assertCss(rowNum);
                        break;
                    case "assertselection":
                        CommonFunc.currentResult = keywordEngine.assertSelection(rowNum);
                        break;
                    case "assertpresence":
                        CommonFunc.currentResult = keywordEngine.assertPresence(rowNum);
                        break;
                    case "dburl":
                        CommonFunc.jdbcUrl = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
                        break;
                    case "dbusername":
                        CommonFunc.jdbcUser = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
                        break;
                    case "dbpassword":
                        CommonFunc.jdbcPass = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
                        break;
                    case "dbquery":
                        CommonFunc.query = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
                        break;
                    case "dbupdate":
                        keywordEngine.executeQuery();
                        break;
                    case "dbdelete":
                        keywordEngine.executeQuery();
                        break;
                    case "dbgetcolumn":
                        keywordEngine.getDBColumnData(rowNum);
                        break;
                    case "dbhasresult":
                        keywordEngine.checkDBHasData(rowNum);
                        break;
                    case "dbgetrowcount":
                        keywordEngine.getDBRowCount(rowNum);
                        break;
                    case "dbresult":
                        CommonFunc.currentResult = keywordEngine.dbColumnValueAssert(rowNum);
                        break;
                    case "addregkey":
                        keywordEngine.setRegistryKey(rowNum, CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN));
                        break;
                    case "getregkey":
                        keywordEngine.setStoredTextFromRegKey(CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.REGISTRY_KEY));
                        break;
                    case "wait":
                        keywordEngine.flowWait(rowNum);
                        break;
                    case "function":
                        keywordEngine.prepareForBuildinMethord(rowNum);
                        break;
                    case "modify":
                        keywordEngine.prepareForBuildinMethord(rowNum);
                        break;
                    case "date":
                        keywordEngine.modifyDate(rowNum);
                        break;
                    case "exit":
                        CommonFunc.isFinished = true;
                        break;
                    case "executekeyevent":
                        keywordEngine.executeKeyEvent(rowNum);
                        break;
                    case "haselement":
                        CommonFunc.currentResult = keywordEngine.hasElement(rowNum);
                        break;
                    case "hassamevalue":
                        CommonFunc.currentResult = keywordEngine.hasSameValue(rowNum);
                        break;
                    case "incnum":
                        keywordEngine.incNum(rowNum);
                        break;
                    case "removeattribute":
                        keywordEngine.removeAttribute(rowNum);
                        break;
                    case "fatal_error":
                        log.info("which keyword is it? " + keyword);
                        CommonFunc.isFinished = true;
                        Exception ex = new Exception("FATAL_ERROR KEYWORD");
                        throw ex;
                    case "rest":
                        CommonFunc.currentResult = keywordEngine.callRestApiGET(rowNum,keywordLowerCase);
                        break;
                    case "reverse_rest":
                        CommonFunc.currentResult = keywordEngine.callRestApiGET(rowNum,keywordLowerCase);
                        break;

                    default:
                        log.info("Wrong Keyword!!!" + keyword);
                        CommonFunc.currentResult = false;
                        CommonFunc.currentReason = "Wrong Keyword!!!";
                        break;

                }
            } else {
                CommonFunc.currentResult = false;
                CommonFunc.currentReason = "No Keyword!!!";
            }
        } catch (Exception ex) {
            CommonFunc.currentResult = false;
            CommonFunc.currentReason = ex.getMessage();
            System.out.println("--------------- common exception(3)------------------:: "+ex.getMessage());
            boolean isCommonException=false;
            for(String commonException: CommonFunc.commonExceptions){
                if(commonException.equals(ex.getMessage())){
                    isCommonException=true;
                }
            }
            if(!isCommonException){
                Constants.IS_ABNORMAL_TERMINATION = true;
                throw ex;
            }
        }
        try {
            CommonFunc.expectedValue = "";

            if (!TestHelper.isEmpty(CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.EXPECTED_RESULT_COLUMN))) {
                CommonFunc.expectedValue = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.EXPECTED_RESULT_COLUMN);
            }
            if (!CommonFunc.expectedValue.equalsIgnoreCase("Passed")
                    && !CommonFunc.expectedValue.equalsIgnoreCase("Failed")) {
                CommonFunc.expectedValue = "Ignored";
            }
            if (CommonFunc.expectedValue.equalsIgnoreCase("Ignored")) {
                CommonFunc.checkResult = false;
            }
            if (CommonFunc.checkResult) {
                CommonFunc.totalRowHasChecked++;
            }
            reportEntity = ReportEntity.builder()
                    .lineNo(rowNum + 1)
                    .testCaseName(CommonFunc.currentTestCaseName)
                    .expectedResult(CommonFunc.expectedValue)
                    .time(new SimpleDateFormat(ReportUtils.getReportConfigurationValue(Constants.REPORT_DATE_FORMAT_KEY)).format(currentDate))
                    .build();

            if (CommonFunc.expectedValue.equalsIgnoreCase("Ignored") ||
                    (CommonFunc.currentResult && CommonFunc.expectedValue.equalsIgnoreCase("Passed")) ||
                    (!CommonFunc.currentResult && CommonFunc.expectedValue.equalsIgnoreCase("Failed"))) {

                reportEntity.setResult("Passed");

                if (CommonFunc.checkResult) {
                    CommonFunc.totalRowPassed++;
                }
            } else {
                reportEntity.setResult("Failed");

                //___________Screenshot______________
                File tempFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
                String date=new SimpleDateFormat("yyyy-MM-dd HH-mm-ss.SSS").format(new Date());
                try {
                    File src = new File(CommonFunc.dirPath+"/screenshots/Screenshot "+date+" Step-"+(rowNum+1)+".png");
                    FileUtils.copyFile(tempFile, src);
                    log.info("Screenshot Path For Failed Test Step : " + src);
                }catch (IOException e) {
                    throw new Error(e);
                }
                //_____________________________________

                if (CommonFunc.checkResult) {
                    CommonFunc.totalRowFailed++;
                }
            }


            if (!TestHelper.isEmpty(CommonFunc.currentReason)) {
                System.out.println("keyword service current reason:: " + CommonFunc.currentReason);
                reportEntity.setReason(CommonFunc.currentReason);
            } else {
                reportEntity.setReason("");
            }
            if(!TestHelper.isEmpty(CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.COMMENTS_COLUMN))){
                reportEntity.setComment(CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.COMMENTS_COLUMN));
            }

        } catch (Exception ex) {
            log.info("Can not write report!!");
            System.out.println("--------------- common exception(2) ------------------:: "+ex.getMessage());
            boolean isCommonException=false;
            for(String commonException: CommonFunc.commonExceptions){
                if(commonException.equals(ex.getMessage())){

                    isCommonException=true;
                }
            }
            if(!isCommonException){
                Constants.IS_ABNORMAL_TERMINATION = true;
                throw ex;
            }
        }
        return reportEntity;
    }
}
