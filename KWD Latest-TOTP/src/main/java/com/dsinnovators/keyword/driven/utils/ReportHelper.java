//
//
//import org.apache.commons.io.FileUtils;
//import org.apache.log4j.Logger;
//
//import java.io.*;
//import java.lang.reflect.Field;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//
///**
// *
// */
//public class ReportHelper {
//    private static Logger log = Logger.getLogger(ReportHelper.class.getName());
//    String reportFilePath = "";
//    BufferedWriter fileWriter = null;
//    public ReportHelper(String browserName, String reportPath) throws Exception{
//        try {
//            String fileNameDateTime = new SimpleDateFormat(ReportUtils.getReportConfigurationValue(Constants.REPORT_FILE_NAME_DATE_FORMAT_KEY)).format(new Date());
//            if(reportPath.substring(reportPath.length() - 1, reportPath.length()).equalsIgnoreCase("/")||reportPath.substring(reportPath.length() - 1, reportPath.length()).equalsIgnoreCase("\\")){
//                reportPath = reportPath.substring(0,reportPath.length()-1);
//            }
//            reportFilePath = reportPath+File.separator+"report-"+browserName+"-"+fileNameDateTime+".csv";
//
//            boolean newReportFile=false;
//            File aLocalFile = new File(reportFilePath);
//            log.info("Try to create report file to :: "+ aLocalFile);
//            if (!aLocalFile.exists()) {
//                String absolutePath = aLocalFile.getAbsolutePath();
//                String fileFolderPath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
//                new File(fileFolderPath).mkdirs();
//                aLocalFile.createNewFile();
//                newReportFile=true;
//            }else {
//                if (aLocalFile.exists()) {
//                    try {
//                        File newFile = new File(reportFilePath+"."+Long.toString(System.currentTimeMillis()));
//                        FileUtils.moveFile(aLocalFile, newFile);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    newReportFile=true;
//                }
//            }
//            if(newReportFile==true){
//                fileWriter = new BufferedWriter(new FileWriter(reportFilePath));
//                fileWriter.append(getReportHeader());
//                fileWriter.append(ReportUtils.getReportConfigurationValue(Constants.NEW_LINE_SEPARATOR_KEY));
//                fileWriter.flush();
//            }else{
//                startWriter();
//            }
//        }catch (Exception ex){
//            log.info("Error while creating report file!!!");
//            ex.printStackTrace();
//            throw ex;
//        }finally {
//            /*try {
//                fileReader.close();
//            }catch (Exception e){
//                log.info("Error while closing fileReader !!!");
//                e.printStackTrace();
//            }*/
//        }
//    }
//    public String getReportFilePath(){
//        return reportFilePath;
//    }
//    public void startWriter() throws Exception{
//        if(fileWriter==null){
//            fileWriter = new BufferedWriter(new FileWriter(reportFilePath,true));
//        }
//    }
//    public void closeWriter() throws Exception{
//        if(fileWriter!=null){
//            fileWriter.flush();
//            fileWriter.close();
//            fileWriter = null;
//        }
//    }
//    public void appendReportLine(ReportEntity reportEntity) throws Exception{
//        if(fileWriter!=null){
//            Field[] fields = reportEntity.getClass().getDeclaredFields();
//            ArrayList<String> headers = getReportHeaderArray();
//            ArrayList<String> finalValues = new ArrayList<String>();
//            String value = "";
//            boolean found = false;
//            for(String header: headers){
//                found = false;
//                for(Field field: fields){
//                    if(header.equals(field.getName())){
//                        found = true;
//                        value = String.valueOf(field.get(reportEntity));
//                        if(!TestHelper.isEmpty(value)) {
//                            finalValues.add(value);
//                        }else {
//                            finalValues.add("");
//                        }
//                    }
//                }
//                if(!found){
//                    finalValues.add("");
//                }
//            }
//            fileWriter.append(getReportLine(finalValues));
//            fileWriter.append(ReportUtils.getReportConfigurationValue(Constants.NEW_LINE_SEPARATOR_KEY));
//            fileWriter.flush();
//        }else{
//            log.info("File writer is not initialized. Please open writer first.");
//        }
//    }
//    public static String getReportHeader(){
//        return getReportLine(getReportHeaderArray()).toString();
//    }
//    public static StringBuffer getReportLine(ArrayList<String> values){
//        StringBuffer finalValue = new StringBuffer();
//        for(String value: values){
//            if(finalValue.length()!=0){
//                finalValue.append(ReportUtils.getReportConfigurationValue(Constants.DELIMITER_KEY));
//            }
//            finalValue.append("\""+value.replaceAll("\"", "\\\"").replaceAll("[\\t\\n\\r]"," ")+"\"");
//        }
//        return finalValue;
//    }
//    public static ArrayList<String> getReportHeaderArray(){
//        String rawHeader = ReportUtils.getReportConfigurationValue("csv.file.header");
//        ArrayList<String> finalHeaders = new ArrayList<String>();
//        if(TestHelper.isEmpty(rawHeader)){
//            Field[] fields = ReportEntity.class.getDeclaredFields();
//            for (Field field : fields) {
//                finalHeaders.add(field.getName());
//            }
//        }else {
//            String[] headers = rawHeader.split("[|]");
//            for (String header : headers) {
//                if (!TestHelper.isEmpty(header)) {
//                    finalHeaders.add(header.trim());
//                }
//            }
//        }
//        return finalHeaders;
//    }
//
//    public static boolean checkIfSameOldReportHeader(String olderHeader){
//        if(getReportHeader().equals(olderHeader)){
//            return true;
//        }
//        return false;
//    }
//
//
//}
