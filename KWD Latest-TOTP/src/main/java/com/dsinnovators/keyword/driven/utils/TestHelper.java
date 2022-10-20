package com.dsinnovators.keyword.driven.utils;

import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.TOTP;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Slf4j
public class TestHelper {
    static InputStream propIS = null;
    static Properties props = null;
    public static String getTestConfPropertyValue(String key, String defaultValue){
        try {
            //if(props==null){
                //initializePropertyFile(Constants.DEFAULT_PROJECT_PROPERTIES_CONFIG);
            //}
            return props.getProperty(key,defaultValue);
        //} catch (FileNotFoundException ex) {
            //log.info("TestHelper::TestHelper property file not found");
        //}  catch (IOException ex) {
            //log.info("TestHelper::TestHelper properties can not be loaded");
        } catch (Exception ex) {
            log.info("TestHelper::"+ex.getMessage());
        }
        return defaultValue;
    }
    public static Properties getProperty(){
        return props;
    }
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    public static String getTestConfPropertyValue(String key){
        String defaultValue = "";
        if(key.equals(Constants.REPORT_FILE_PATH_KEY)){
            defaultValue = Constants.DEFAULT_REPORT_FILE_PATH;
        }
        return getTestConfPropertyValue(key, defaultValue);
    }

    public static void initializePropertyFile(String propertyFile) throws Exception {
        propIS = new FileInputStream(propertyFile);
        props = new Properties();
        props.load(propIS);
    }

    public static boolean isEmpty(List list) {
        if (list == null) {

            return true;
        }
        if (list.size() == 0) {

            return true;
        }

        return false;
    }

    public static boolean isEmpty(String string) {
        if (string == null) {

            return true;
        }
        if ("".equals(string)) {

            return true;
        }

        return false;
    }


    // auto top generate

    public static String getTOTPCode(String otpUri) throws URISyntaxException {

        String code = null;
        // URI uri = new URI("otpauth://totp/Club%20Swan%20(US)%20DEV:imran786%40mailinator.com?secret="+secretKey+"&issuer=Club%20Swan%20(US)%20DEV");
        URI uri=new URI(otpUri);
        TOTP totp = TOTP.fromURI(uri);
        try {
            code = totp.now();
            log.info("Code print : " + "  " + code);
            // To verify a token:
            boolean isValid = totp.verify(code);
        } catch (IllegalStateException e) {
            // Handle error
        }
        return code;
    }

//----------------------------------------

    public static Date timeResetAndDayChange(Date date, boolean isIncrement, int changeType, int number, boolean clearTime){
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        int year = cal1.get(Calendar.YEAR);
        int month = cal1.get(Calendar.MONTH);
        int day = cal1.get(Calendar.DAY_OF_MONTH);
        int hour = cal1.get(Calendar.HOUR);
        int minute = cal1.get(Calendar.MINUTE);
        int second = cal1.get(Calendar.SECOND);
        int millisecond = cal1.get(Calendar.MILLISECOND);

        Calendar cal2 = Calendar.getInstance();
        cal2.clear();
        cal2.set(Calendar.YEAR, year);
        cal2.set(Calendar.MONTH, month);
        cal2.set(Calendar.DAY_OF_MONTH, day);
        if(clearTime){
            cal2.set(Calendar.HOUR, 0);
            cal2.set(Calendar.MINUTE, 0);
            cal2.set(Calendar.SECOND, 0);
            cal2.set(Calendar.MILLISECOND, 0);
        }else{
            cal2.set(Calendar.HOUR, hour);
            cal2.set(Calendar.MINUTE, minute);
            cal2.set(Calendar.SECOND, second);
            cal2.set(Calendar.MILLISECOND, millisecond);
        }
        if(!isIncrement){
            number = number*-1;
        }
        if(changeType==Calendar.YEAR) {
            cal2.add(Calendar.YEAR, number);
        }
        if(changeType==Calendar.MONTH) {
            cal2.add(Calendar.MONTH, number);
        }
        if(changeType==Calendar.DATE) {
            cal2.add(Calendar.DAY_OF_MONTH, number);
        }
        if(changeType==Calendar.MINUTE) {
            cal2.add(Calendar.MINUTE, number);
        }
        if(changeType==Calendar.SECOND) {
            cal2.add(Calendar.SECOND, number);
        }
        if(changeType==Calendar.MILLISECOND) {
            cal2.add(Calendar.MILLISECOND, number);
        }
        return cal2.getTime();

    }
}

