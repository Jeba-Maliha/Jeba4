package com.dsinnovators.keyword.driven.meta;

import com.dsinnovators.keyword.driven.utils.Constants;
import com.dsinnovators.keyword.driven.utils.TestHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaFiles {
    public MetaFiles(){
        String metaFilePath = TestHelper.getTestConfPropertyValue(Constants.META_FILE_PATH) ;
        File directory = new File(String.valueOf(metaFilePath));
        if (!directory.exists()) {
            directory.mkdir();
        }
    }
    public String getMetaFileName(String reportAndMetaFileName) {
        String metaFilePath = TestHelper.getTestConfPropertyValue(Constants.META_FILE_PATH) ;
        if(metaFilePath.substring(metaFilePath.length() - 1).equalsIgnoreCase("/")||metaFilePath.substring(metaFilePath.length() - 1).equalsIgnoreCase("\\")){
            metaFilePath = metaFilePath.substring(0,metaFilePath.length()-1);
        }
        return metaFilePath + File.separator + "meta-"+ reportAndMetaFileName+ ".txt";
    }

    public String createMetaFile(String excelFileName,String sheetName, String reportAndMetaFileName) throws IOException {
        String metaFilePath = getMetaFileName(reportAndMetaFileName);
        File f = new File(metaFilePath);
        BufferedWriter fileWriter=null ;
        if(f.exists() && f.isFile()) {
            try{
                fileWriter = new BufferedWriter(new FileWriter(metaFilePath,true));
                fileWriter.write("SheetName="+sheetName+"\n");

            } catch (Exception e) {
                System.out.println("Error appending meta file (1):: "+e.getMessage());
            }
        }
        else {
            try{
                fileWriter = new BufferedWriter(new FileWriter(metaFilePath));
                fileWriter.append("ExcelFileName="+excelFileName+"\n");
                fileWriter.append("SheetName="+sheetName+"\n");

            } catch (Exception e) {
                System.out.println("Error creating meta file:: " + e.getMessage());
            }
        }
        fileWriter.newLine();
        fileWriter.flush();
        return metaFilePath;
    }

    public String appendToMetaFile(String metaFilePath , Map<String, String> map) throws IOException  {
        try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(metaFilePath,true))) {
           if(map.containsKey("regKeyValue")) {
               fileWriter.write(map.get("regKeyValue") + "\t(Time=" + map.get("dateTime") + " LineNo=" + map.get("lineNo") + ")\n");
           }
            if(map.containsKey("status")){
                fileWriter.write("Status="+map.get("status")+"\n");
                fileWriter.newLine();
            }

            fileWriter.flush();

        } catch (Exception e) {
            System.out.println("Error appending to meta file:: "+e.getMessage());
        }
        return metaFilePath;
    }
    public List<String> metaTextFiles() {
        System.out.println("metaTextFiles directory:: "+TestHelper.getTestConfPropertyValue(Constants.META_FILE_PATH));
        File directory = new File(TestHelper.getTestConfPropertyValue(Constants.META_FILE_PATH));
        List<String>metaFilesList = new ArrayList<String>();

        System.out.println("Directory error" + directory.listFiles());
        for (File file : directory.listFiles()) {
            String fileName = file.getName();
            Pattern pattern = Pattern.compile(".xlsx-(.*).txt");
            Matcher matcher = pattern.matcher(fileName);
            if(matcher.find()) {
                metaFilesList.add(matcher.group(1));
            }
        }
        return metaFilesList;
    }
    public void deleteFiles() {
        System.out.println("deleteMetaFiles directory:: "+TestHelper.getTestConfPropertyValue(Constants.META_FILE_PATH));
        File dir = new File(TestHelper.getTestConfPropertyValue(Constants.META_FILE_PATH));
        if (!dir.exists())
            return;
        File filesList[] = dir.listFiles();
        for (File file : filesList) {
            if (file.getName().toLowerCase().endsWith(".txt")) {
                file.delete();
            }
        }

    }
}
