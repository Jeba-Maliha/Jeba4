package com.dsinnovators.keyword.driven.commons;

import com.dsinnovators.keyword.driven.factory.WebDriverFactory;
import com.dsinnovators.keyword.driven.utils.Constants;
import com.dsinnovators.keyword.driven.utils.TestHelper;
import com.google.common.base.Function;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class CommonFunc {

	//public static String excelPath = "";
	public static String baseUrl = "";
	public static String browser;
	public static String paperlessStatus = "";
	public static String CurrentPage = "";
	public static String Label="";
	public static String currentTestCaseName ="";
	public static Boolean labelChange = false;
	public static Boolean currentResult = false;
	public static String currentReason = "";
	public static Boolean checkResult = false;
	public static int totalRowHasChecked = 0;
	public static int totalRowFailed = 0;
	public static int totalRowPassed = 0;
	public static Boolean isFinished = false;
	public static String expectedValue="";
	public static Boolean conditionApply=false;
	public static Boolean meetCondition=false;
	public static String jdbcUrl="";
	public static String jdbcPass="";
	public static String jdbcUser="";
	public static String query="";
	public static String dbResult="";
	public static String selectionValue="";
	public static String dirPath="";
	public static Map<String,String> registryKeyMap = new HashMap<>();
	public static String [] commonExceptions={"No Such Element Found.","Can not separate location and selection type. Guessing format error.",
			"Increment value only except a number and valid date.","Increment by only except a number.",
			"Increment value only except a number or date.","Decrement by only except a number.","Gate date value only except a date.",
			"Does not have enough property to connect DB","Can not connect to db","Could not connect to the database",
			"given File path does not exist.","given File path is not file.","given File path is not readable.","unsupported browser name",
			"Driver not found.","Could not initialize driver.","Excel file not found.!!!","Sheet not found.!!!",
			"Does not have enough property to connect DB"};
	public static String getRequestResponse="";
	public static String putRequestResponse="";
	public static String deleteRequestResponse="";
	public static String postRequestResponse="";


	public static WebDriver setUp() throws Exception {
		try {
			return  new WebDriverFactory().getWebDriver(browser);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception("Could not initialize driver.");
		}
	}

	public static void gotoURL(WebDriver driver, String baseUrl) {
		driver.get(baseUrl);
		//driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	// Assert Value

	public static String getValueWithSelectorTypeAndLocator(int rowNum, final String selectorType, final String locator, String attributeName, WebDriver driver)
			throws Exception {
		try {
			log.info("selectorType :: " + selectorType
					+ " :: locator :: " + locator + " :: attributeName :: " + attributeName);

			if(!TestHelper.isEmpty(selectorType) && !selectorType.equalsIgnoreCase("regkey")) {
				System.out.println("driver = " + driver);
				WebElement selectElement = getPageElementWithWait(driver, rowNum, selectorType, locator);
				if(selectElement!=null){
					if (!TestHelper.isEmpty(attributeName)) {
						return selectElement.getAttribute(attributeName);
					}
					return selectElement.getText();
				} else {
					throw new Exception("No Such Element Found.");
				}
			} else if (!TestHelper.isEmpty(selectorType) && selectorType.equalsIgnoreCase("regkey")) {
				/*if(registryKeyMap.containsKey(locator)){
					return registryKeyMap.get(locator);
				}*/
				return locator;
			}
			return null;
		} catch(NoSuchElementException ex) {
			throw new Exception("No Such Element Found.");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	// Wait for Visibility of Elements
	private static void waitForVisibilityOf(WebDriver driver, By element){
		try {
			//log.info("imlog:: driver: " + driver);
//			  WebDriverWait wait = new WebDriverWait(driver, 25);

			JavascriptExecutor js=(JavascriptExecutor)driver;
			// Detect if page loaded completely
			String response = (String) js.executeScript("return document.readyState");


			// log.info("IMLOG::::>> "+ ExpectedConditions.visibilityOfElementLocated(element));

			//if load complete or not complete
			if(response.equals("complete")) {
				ExpectedConditions.visibilityOfElementLocated(element);
			}else {
				//waitForVisibilityOf(driver,element);
			}
//			  wait.until(ExpectedConditions.visibilityOfElementLocated(element));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static String getExcelDataVariableReplaced(int rowNum, int cellNumber) throws Exception {
		String excelData = ExcelUtils.getCellStringData(rowNum, cellNumber);
		if(!TestHelper.isEmpty(excelData)) {
			return replaceVariableWithValue(excelData.trim());
		}else{
			return excelData;
		}
	}

	public static String replaceVariableWithValue(String queryWithVar){
		List<String> variableKeyList = getVariableList(queryWithVar);
		for(String variableKey : variableKeyList){
			System.out.println("variableKey === " + variableKey);
			if(registryKeyMap.containsKey(variableKey)){
				queryWithVar = queryWithVar.replace("${"+variableKey+"}",registryKeyMap.get(variableKey));
			}
		}
		return queryWithVar;
	}

	public static List<String> getVariableList(String queryWithVar){
		Pattern REGEX = Pattern.compile("\\$\\{(.+?)\\}");
		List<String> variableList = new ArrayList<>();
		System.out.println("queryWithVar === " + queryWithVar);
		Matcher matcher = REGEX.matcher(queryWithVar);
		while (matcher.find()) {
			variableList.add(matcher.group(1));

		}
		return variableList;
	}

	public static void waitForPageLoad(WebDriver driver, int rowNum) throws Exception{
		long timeout = getTimeout(rowNum);
		Wait<WebDriver> wait = new WebDriverWait(driver, timeout, Constants.DEFAULT_WAIT_TIME_CHUNK_IN_MILLISECOND);
		wait.until(new Function<WebDriver, Boolean>() {
			public Boolean apply(WebDriver driver) {
				log.info("Current Window State : "
						+ String.valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState")));
				return String
						.valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
						.equals("complete");
			}
		});
	}

	public static long getTimeout(int rowNum) throws Exception {
		long timeout = Constants.DEFAULT_WAIT_TIME_IN_SECOND;
		if(!TestHelper.isEmpty(getExcelDataVariableReplaced(rowNum, ExcelUtils.TIMEOUT_COLUMN))){
			try {
				timeout = Long.parseLong(getExcelDataVariableReplaced(rowNum, ExcelUtils.TIMEOUT_COLUMN));
				log.info("Max waiting time::"+timeout);
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
		if(timeout<0){
			timeout=Constants.DEFAULT_WAIT_TIME_IN_SECOND;
		}
		return timeout;
	}


	public static WebElement getPageElementWithWait(final WebDriver driver, int rowNum, final String selectorType, final String locator) throws Exception{
		try {
			WebElement webElement = null;
			/*try {
				webElement = getPageElement(driver, selectorType, locator);
			}catch(Exception e){
				log.info("Did not get element at 1st try.");
			}
			if(webElement==null) {*/
				long timeout = getTimeout(rowNum) * 1000;
				/*for(int i = 0; i < timeout; i += Constants.DEFAULT_WAIT_TIME_CHUNK_IN_MILLISECOND ){
					Thread.sleep(Constants.DEFAULT_WAIT_TIME_CHUNK_IN_MILLISECOND);
					try {
						webElement = getPageElement(driver, selectorType, locator);
						if(webElement!=null){
							return webElement;
						}
					}catch(Exception e){
						log.info("Waited "+(i/1000)+" seconds, still did not get element.");
					}
				}*/

				Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
						.withTimeout(timeout, TimeUnit.MILLISECONDS)
						.pollingEvery(Constants.DEFAULT_WAIT_TIME_CHUNK_IN_MILLISECOND, TimeUnit.MILLISECONDS)
						.ignoring(NoSuchElementException.class);

				return wait.until(new Function<WebDriver, WebElement>() {
					public WebElement apply(WebDriver driver) {
						return getPageElement(driver, selectorType, locator);
					}
				});
			/*}else{
				return webElement;
			}*/
		}catch(Exception ex){
			//ex.printStackTrace();
		}
		return null;
	}
	public static WebElement getPageElement(WebDriver driver, String selectorType, String locator) {
		switch (selectorType) {
			case "id":
				return driver.findElement(By.id(locator));

			case "name":
				return driver.findElement(By.name(locator));

			case "css":
				return driver.findElement(By.cssSelector(locator));

			case "xpath":
				return driver.findElement(By.xpath(locator));

			case "link":
				return driver.findElement(By.linkText(locator));

			case "tag":
				return driver.findElement(By.tagName(locator));

//    	case "alert":
//			driver.switchTo().alert().accept();
//
		}
		return null;
	}

	public static void flowWait(WebDriver driver, int rowNum) throws Exception{
		long timeout = getTimeout(rowNum);
		Thread.sleep(timeout*1000);
	}

	public static void fixedWaitBeforeEachSteps() throws Exception{
		String timeoutStr = TestHelper.getTestConfPropertyValue(Constants.WAIT_BEFORE_EACH_STEP_KEY);
		int timeout = 0;
		if(!TestHelper.isEmpty(timeoutStr)){
			try{
				timeout = Integer.valueOf(timeoutStr);
			}catch(Exception ex){
				timeout = Constants.DEFAULT_WAIT_BEFORE_EACH_STEP;
			}
		}
		if(timeout>0) {
			Thread.sleep(timeout * 1000);
		}
	}

	public static boolean checkFileExecutable(File file) throws Exception{
		if (!file.exists()) {
			throw new Exception("given File path does not exist.");
		}
		if (!file.isFile()){
			throw new Exception("given File path is not file.");
		}

		if (!file.canRead()){
			throw new Exception("given File path is not readable.");
		}

		if (!file.canExecute()){
			return false;
		}
		return true;
	}
	public static boolean makeFileExecutable(File file) throws Exception{
		Boolean result = file.setReadable(true);
		result = file.setExecutable(true);
		return result;
	}
	public static boolean checkFileWritable(File file) throws Exception{
		boolean result = true;
		if (file.exists()) {
			throw new Exception("given File path does not exist.");
		}
		if (file.isFile()){
			throw new Exception("given File path is not file.");
		}
		if (file.canWrite()){
			return false;
		}
		return true;
	}
	public static boolean checkFileReadable(File file) throws Exception{
		if (file.exists()) {
			throw new Exception("given File path does not exist.");
		}
		if (file.isFile()){
			throw new Exception("given File path is not file.");
		}
		if (file.canRead()){
			return false;
		}
		return true;
	}

	public static String buildInMethod(String methodName,Map<String,String> operations) throws Exception{
		switch (methodName.toLowerCase()) {
			case Constants.REPLACE:
				return operations.get(Constants.VALUE).replace(operations.get(Constants.REPLACING),operations.get(Constants.REPLACEBY));
			case Constants.CONCAT:
				StringBuffer value = new StringBuffer();
				for(String key: operations.keySet()){
					value.append(operations.get(key));
				}
				return value.toString();
			case Constants.RANDOM:
				SecureRandom rnd = new SecureRandom();
				if(operations.get(Constants.TYPE).equalsIgnoreCase(Constants.STRING)){
					int length = Integer.parseInt(operations.get(Constants.LENGTH));
					StringBuffer sb = new StringBuffer( length );
					String supportedChar = Constants.DEFAULT_RANDOM_STRING_SUPPORTED_CHAR;
					for( int i = 0; i < length; i++ )
						sb.append( supportedChar.charAt( rnd.nextInt(supportedChar.length()) ) );
					return sb.toString();
				}else if(operations.get(Constants.TYPE).equalsIgnoreCase(Constants.NUMBER)){
					int fromNumber = Integer.parseInt(operations.get(Constants.FROM));
					int toNumber = Integer.parseInt(operations.get(Constants.TO));
					int randomNum = rnd.nextInt((toNumber - fromNumber) + 1) + fromNumber;
					return String.valueOf(randomNum);
				}
			case Constants.RANDOM_ALPHA_NUMARIC:
				SecureRandom rndAF = new SecureRandom();
				if(operations.get(Constants.TYPE).equalsIgnoreCase(Constants.STRING)){
					int length = Integer.parseInt(operations.get(Constants.LENGTH));
					StringBuffer sb = new StringBuffer( length );
					String supportedChar = Constants.DEFAULT_RANDOM_STRING_SUPPORTED_CHAR_WITH_NUMBER;
					for( int i = 0; i < length; i++ )
						sb.append( supportedChar.charAt( rndAF.nextInt(supportedChar.length()) ) );
					return sb.toString();
				}else if(operations.get(Constants.TYPE).equalsIgnoreCase(Constants.NUMBER)){
					int fromNumber = Integer.parseInt(operations.get(Constants.FROM));
					int toNumber = Integer.parseInt(operations.get(Constants.TO));
					int randomNum = rndAF.nextInt((toNumber - fromNumber) + 1) + fromNumber;
					return String.valueOf(randomNum);
				}
			case Constants.INCREMENT:
				int incValueType = Integer.valueOf(operations.get(Constants.VALUE_TYPE));
				double incrementBy = Double.valueOf(operations.get(Constants.INCREMENT_BY));
				if(incValueType == 0 ) {
					int valueIntegerInc = Integer.valueOf(operations.get(Constants.VALUE));
					valueIntegerInc = valueIntegerInc + Integer.valueOf(String.valueOf(incrementBy));
					return String.valueOf(valueIntegerInc);
				}else if ( incValueType == 1) {
					double valueDoubleInc = Double.valueOf(operations.get(Constants.VALUE));
					valueDoubleInc = valueDoubleInc + incrementBy;
					return String.valueOf(valueDoubleInc);
				}else if(incValueType == 2){
					String dateFormat = operations.get(Constants.FORMAT).trim();
					String dateString = operations.get(Constants.VALUE).trim();
					Date date;
					if(dateString.equalsIgnoreCase(Constants.CURRENT)){
						date = new Date();
					}else{
						date = new SimpleDateFormat(dateFormat).parse(dateString);
					}
					String incType = operations.get(Constants.INCREMENT_TYPE).trim();
					int incTypeInt = Calendar.DAY_OF_MONTH;
					if(incType.equalsIgnoreCase(Constants.DAY)){
						incTypeInt = Calendar.DAY_OF_MONTH;
					}else if(incType.equalsIgnoreCase(Constants.MONTH)){
						incTypeInt = Calendar.MONTH;
					}else if(incType.equalsIgnoreCase(Constants.YEAR)){
						incTypeInt = Calendar.YEAR;
					}else if(incType.equalsIgnoreCase(Constants.HOUR)){
						incTypeInt = Calendar.HOUR;
					}else if(incType.equalsIgnoreCase(Constants.MINUTE)){
						incTypeInt = Calendar.MINUTE;
					}else if(incType.equalsIgnoreCase(Constants.SECOND)){
						incTypeInt = Calendar.SECOND;
					}else if(incType.equalsIgnoreCase(Constants.MILLISECOND)){
						incTypeInt = Calendar.MILLISECOND;
					}
					date = TestHelper.timeResetAndDayChange(date,true,incTypeInt,(int)incrementBy, false);
					return new SimpleDateFormat(dateFormat).format(date);
					//timeResetAndDayChange
				}
			case Constants.DECREMENT:
				int decValueType = Integer.valueOf(operations.get(Constants.VALUE_TYPE));
				double decrementBy = Double.valueOf(operations.get(Constants.DECREMENT_BY));
				if(decValueType == 0  ) {
					int valueIntegerInc = Integer.valueOf(operations.get(Constants.VALUE));
					valueIntegerInc = valueIntegerInc - Integer.valueOf(String.valueOf(decrementBy));
					return String.valueOf(valueIntegerInc);
				}else if (  decValueType == 1) {
					double valueDoubleDec = Double.valueOf(operations.get(Constants.VALUE));
					valueDoubleDec = valueDoubleDec - decrementBy;
					return String.valueOf(valueDoubleDec);
				}else if(decValueType == 2){
					String dateFormat = operations.get(Constants.FORMAT).trim();
					String dateString = operations.get(Constants.VALUE).trim();
					Date date;
					if(dateString.equalsIgnoreCase(Constants.CURRENT)){
						date = new Date();
					}else{
						date = new SimpleDateFormat(dateFormat).parse(dateString);
					}
					String descType = operations.get(Constants.DECREMENT_TYPE).trim();
					int descTypeInt = Calendar.DAY_OF_MONTH;
					if(descType.equalsIgnoreCase(Constants.DAY)){
						descTypeInt = Calendar.DAY_OF_MONTH;
					}else if(descType.equalsIgnoreCase(Constants.MONTH)){
						descTypeInt = Calendar.MONTH;
					}else if(descType.equalsIgnoreCase(Constants.YEAR)){
						descTypeInt = Calendar.YEAR;
					}else if(descType.equalsIgnoreCase(Constants.HOUR)){
						descTypeInt = Calendar.HOUR;
					}else if(descType.equalsIgnoreCase(Constants.MINUTE)){
						descTypeInt = Calendar.MINUTE;
					}else if(descType.equalsIgnoreCase(Constants.SECOND)){
						descTypeInt = Calendar.SECOND;
					}else if(descType.equalsIgnoreCase(Constants.MILLISECOND)){
						descTypeInt = Calendar.MILLISECOND;
					}
					date = TestHelper.timeResetAndDayChange(date,true,descTypeInt,(int)decrementBy, false);
					return new SimpleDateFormat(dateFormat).format(date);
				}
			case Constants.COMPARE:
				String dateFormat = null;
				Date compareDate1 = null;
				Date compareDate2 = null;
				int compareInt1 = 0;
				int compareInt2 = 0;
				double compareDouble1 = 0.0;
				double compareDouble2 = 0.0;
				if(operations.containsKey(Constants.FORMAT)){
					dateFormat = operations.get(Constants.FORMAT).trim();
					operations.remove(Constants.FORMAT);
				}
				String compareType = operations.get(Constants.TYPE);
				operations.remove(Constants.TYPE);
				if(operations.size()==2){
					String firstValue = operations.get("0");
					String secondValue = operations.get("1");
					if(TestHelper.isEmpty(firstValue) && TestHelper.isEmpty(secondValue)){
						return Constants.TRUE;
					}
					try{
						if(TestHelper.isEmpty(dateFormat)){
							dateFormat = Constants.DEFAULT_REPORT_DATE_FORMAT;
						}
						compareDate1 = new SimpleDateFormat(dateFormat).parse(firstValue);
						compareDate2 = new SimpleDateFormat(dateFormat).parse(secondValue);
						if(compareType.equalsIgnoreCase(Constants.EQUAL)){
							if(compareDate1.equals(compareDate2)){
								return Constants.TRUE;
							}
						}else if(compareType.equalsIgnoreCase(Constants.GREATER_THEN)){
							if(compareDate1.after(compareDate2)){
								return Constants.TRUE;
							}
						}else if(compareType.equalsIgnoreCase(Constants.LESS_THEN)){
							if(compareDate1.before(compareDate2)){
								return Constants.TRUE;
							}
						}
						return Constants.FALSE;
					}catch(Exception ex) {
						try {
							compareInt1 = Integer.valueOf(firstValue);
							compareInt2 = Integer.valueOf(secondValue);
							if(compareType.equalsIgnoreCase(Constants.EQUAL)){
								if(compareInt1==compareInt2){
									return Constants.TRUE;
								}
							}else if(compareType.equalsIgnoreCase(Constants.GREATER_THEN)){
								if(compareInt1>compareInt2){
									return Constants.TRUE;
								}
							}else if(compareType.equalsIgnoreCase(Constants.LESS_THEN)){
								if(compareInt1<compareInt2){
									return Constants.TRUE;
								}
							}
							return Constants.FALSE;
						} catch (Exception ex2) {
							try {
								compareDouble1 = Double.valueOf(firstValue);
								compareDouble2 = Double.valueOf(secondValue);
								if(compareType.equalsIgnoreCase(Constants.EQUAL)){
									if(compareDouble1==compareDouble2){
										return Constants.TRUE;
									}
								}else if(compareType.equalsIgnoreCase(Constants.GREATER_THEN)){
									if(compareDouble1>compareDouble2){
										return Constants.TRUE;
									}
								}else if(compareType.equalsIgnoreCase(Constants.LESS_THEN)){
									if(compareDouble1<compareDouble2){
										return Constants.TRUE;
									}
								}
								return Constants.FALSE;
							} catch (Exception ex3) {
								if(compareType.equalsIgnoreCase(Constants.EQUAL)){
									if(firstValue.equals(firstValue)){
										return Constants.TRUE;
									}
								}else if(compareType.equalsIgnoreCase(Constants.GREATER_THEN)){
									if(firstValue.compareTo(firstValue)>0){
										return Constants.TRUE;
									}
								}else if(compareType.equalsIgnoreCase(Constants.LESS_THEN)){
									if(firstValue.compareTo(firstValue)<0){
										return Constants.TRUE;
									}
								}
								return Constants.FALSE;
							}
						}
					}

				}else {
					return Constants.FALSE;
				}
			case Constants.GET_DATE:
				String getDateFormat = operations.get(Constants.FORMAT).trim();
				String dateString = operations.get(Constants.VALUE).trim();
				Date date = new SimpleDateFormat(getDateFormat).parse(dateString);
				return new SimpleDateFormat(getDateFormat).format(date);
			case Constants.STRING_CHECK:
				String dataStringCheck = operations.get(Constants.VALUE);
				String checkType = operations.get(Constants.TYPE);
				String subStr = "";
				String regex = "";
				if(operations.containsKey(Constants.SUB_STRING)) {
					subStr = operations.get(Constants.SUB_STRING);
				}
				if(operations.containsKey(Constants.REGEX)) {
					regex = operations.get(Constants.REGEX);
				}
				if((checkType.equalsIgnoreCase(Constants.CONTAINS) && TestHelper.isEmpty(regex) ) || checkType.equalsIgnoreCase(Constants.START_WITH) || checkType.equalsIgnoreCase(Constants.END_WITH)) {
					if (TestHelper.isEmpty(dataStringCheck) && TestHelper.isEmpty(subStr)) {
						return Constants.TRUE;
					}
				}
				if(TestHelper.isEmpty(dataStringCheck)){
					return Constants.FALSE;
				}
				if((checkType.equalsIgnoreCase(Constants.CONTAINS) && !TestHelper.isEmpty(subStr) )){
					if(dataStringCheck.contains(subStr)){
						return Constants.TRUE;
					}
				}
				if((checkType.equalsIgnoreCase(Constants.CONTAINS) && TestHelper.isEmpty(subStr) && !TestHelper.isEmpty(subStr) )){
					if(TestHelper.isEmpty(dataStringCheck)){
						return Constants.FALSE;
					}
					if(dataStringCheck.matches(subStr)){
						return Constants.TRUE;
					}
				}
				if((checkType.equalsIgnoreCase(Constants.START_WITH) && !TestHelper.isEmpty(subStr))){
					if(dataStringCheck.startsWith(subStr)){
						return Constants.TRUE;
					}
				}
				if((checkType.equalsIgnoreCase(Constants.END_WITH) && !TestHelper.isEmpty(subStr))){
					if(dataStringCheck.endsWith(subStr)){
						return Constants.TRUE;
					}
				}
				return Constants.FALSE;
			case "add":

				break;
			case "subtract":

				break;
			case "divide":

				break;
			case "multiply":

				break;
			default:

				break;
		}
		return "";
	}

}
