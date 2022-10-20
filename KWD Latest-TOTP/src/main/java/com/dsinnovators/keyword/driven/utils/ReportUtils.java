package com.dsinnovators.keyword.driven.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 */
@Slf4j
 public class ReportUtils {
    static InputStream propIS = null;
    static Properties props = null;
    public static String getReportConfigurationValue(String key, String defaultValue){
        try {
            if(props==null){
                initializePropertyFile(Constants.DEFAULT_REPORT_PROPERTIES_CONFIG);
            }
            return props.getProperty(key,defaultValue);
        } catch (FileNotFoundException ex) {
            log.info("ReportUtils::Report property file not found");
        }  catch (IOException ex) {
            log.info("ReportUtils::Report properties can not be loaded");
        } catch (Exception ex) {
            log.info("ReportUtils::"+ex.getMessage());
        }
        return defaultValue;
    }

    public static String getReportConfigurationValue(String key){
        String defaultValue = "";
        if(key.equals(Constants.REPORT_FILE_NAME_DATE_FORMAT_KEY)){
            defaultValue = Constants.DEFAULT_REPORT_FILE_NAME_DATE_FORMAT;
        }else if(key.equals(Constants.REPORT_DATE_FORMAT_KEY)){
            defaultValue = Constants.DEFAULT_REPORT_DATE_FORMAT;
        }else if(key.equals(Constants.DELIMITER_KEY)){
            defaultValue = Constants.DEFAULT_DELIMITER;
        }else if(key.equals(Constants.NEW_LINE_SEPARATOR_KEY)){
            defaultValue = Constants.DEFAULT_NEW_LINE_SEPARATOR;
        }
        return getReportConfigurationValue(key,defaultValue);
    }

    private static void initializePropertyFile(String propertyFile) throws Exception {
        propIS = new FileInputStream(propertyFile);
        props = new Properties();
        props.load(propIS);
    }

}
