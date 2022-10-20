package com.dsinnovators.keyword.driven.service;

import com.dsinnovators.keyword.driven.exception.TestFailedException;
import com.dsinnovators.keyword.driven.properties.CliArgs;
import com.dsinnovators.keyword.driven.utils.Constants;
import com.dsinnovators.keyword.driven.utils.TestHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;

@Slf4j
@Component
@Qualifier("CliParser")
public class CliParser {
    public void parse(String ...args) {
        String configFile = Constants.DEFAULT_PROJECT_PROPERTIES_CONFIG;
        try {
            Options options = new Options();
            options.addOption("c", Constants.CONFIG_FILE, true, "To define config file explicitly.");
            options.addOption("e", Constants.EXCEL_PATH, true, "To define excel file explicitly.");
            options.addOption("s", Constants.SHEET_NAME, true, "To define sheet name explicitly.");
            options.addOption("r", Constants.REPORT_PATH, true, "To define log name explicitly.");
            options.addOption("rf", Constants.REPORT_FORMAT, true, "To define log name explicitly.");
            options.addOption("d", Constants.DIR_PATH, true, "To define asset directory path explicitly.");
            options.addOption("er", Constants.EXPECTED_RESULT, true, "Set Expected Result parentage.");
            options.addOption("to", Constants.REPORT_TO_ID, true, "Set receiver email to get test report through email.");
            options.addOption("cc", Constants.REPORT_CC_IDS, true, "Set cc ids to get test report through email");
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args, true);

            int expectedResultInt = 0;

            if(cmd.hasOption("e")){
                log.info("Get argument from command line ::excelPath::"+ cmd.getOptionValue("e"));
                CliArgs.excelPath = cmd.getOptionValue("e");
            }
            if(cmd.hasOption("s")){
                log.info("Get argument from command line ::sheetNames::"+ cmd.getOptionValue("s"));
                CliArgs.sheetNames = cmd.getOptionValue("s");
            }
            if(cmd.hasOption("r")){
                log.info("Get argument from command line ::reportPath::"+ cmd.getOptionValue("r"));
                CliArgs.reportPath = cmd.getOptionValue("r");
            }
            if(cmd.hasOption("rf")){
                log.info("Get argument from command line ::reportPath::"+ cmd.getOptionValue("rf"));
                CliArgs.reportFormat = cmd.getOptionValue("rf");
            }
            if(cmd.hasOption("d")){
                log.info("Get argument from command line ::dirPath::"+ cmd.getOptionValue("d"));
                CliArgs.dirPath = cmd.getOptionValue("d");
            }
            if(cmd.hasOption("c")){
                log.info("Get argument from command line ::configFile::"+ cmd.getOptionValue("c"));
                configFile = cmd.getOptionValue("c",configFile);
            }

            if(cmd.hasOption("er")){
                log.info("Get argument from command line ::expectedResult::"+ cmd.getOptionValue("er"));
                CliArgs.expectedResult = Integer.parseInt(cmd.getOptionValue("er"));
            }

            if(cmd.hasOption("to")){
                log.info("Get argument from command line ::receiver's email ID::"+ cmd.getOptionValue("to"));
                CliArgs.to = cmd.getOptionValue("to");
            }
            if(cmd.hasOption("cc")){
                log.info("Get argument from command line ::cc IDs::"+ cmd.getOptionValue("cc"));
                CliArgs.cc = cmd.getOptionValue("cc");
            } else {
                CliArgs.expectedResult = 0;
            }

            TestHelper.initializePropertyFile(configFile);


            if(TestHelper.isEmpty(CliArgs.excelPath)){
                CliArgs.excelPath = TestHelper.getTestConfPropertyValue(Constants.EXCEL_PATH);
            }
            if(TestHelper.isEmpty(CliArgs.sheetNames)) {
                CliArgs.sheetNames = TestHelper.getTestConfPropertyValue(Constants.SHEET_NAME);
            }
            if(TestHelper.isEmpty(CliArgs.reportPath)){
                CliArgs.reportPath = TestHelper.getTestConfPropertyValue(Constants.REPORT_FILE_PATH_KEY);
            }
            if(TestHelper.isEmpty(CliArgs.reportFormat)){
                CliArgs.reportFormat = TestHelper.getTestConfPropertyValue(Constants.REPORT_FORMAT);
            }
            if(TestHelper.isEmpty(CliArgs.dirPath)){
                CliArgs.dirPath = TestHelper.getTestConfPropertyValue(Constants.ASSET_DIR_PATH_KEY);
            }

            if(TestHelper.isEmpty(CliArgs.cc)){
                CliArgs.cc = TestHelper.getTestConfPropertyValue(Constants.REPORT_CC_IDS);
            }

            if(TestHelper.isEmpty(CliArgs.to)){
                CliArgs.to = TestHelper.getTestConfPropertyValue(Constants.REPORT_TO_ID);
            }


            log.info("Excel File : "+ CliArgs.excelPath);
            log.info("Sheet Name : "+ CliArgs.sheetNames);
            log.info("Report Path : "+ CliArgs.reportPath);
            log.info("Asset Dir Path : "+ CliArgs.dirPath);
            log.info("CC IDS : "+ CliArgs.cc);
            log.info("Receiver ID : "+ CliArgs.to);
            log.info("Configuration file Path : "+ configFile);

        } catch (TestFailedException e) {
            e.printStackTrace();
            System.exit(1);
        }  catch(FileNotFoundException ex){
            System.out.println("Configuration file not found!!");
            System.out.println("Configuration file path:"+configFile);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}

