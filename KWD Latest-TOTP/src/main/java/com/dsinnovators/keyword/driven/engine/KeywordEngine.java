package com.dsinnovators.keyword.driven.engine;

import com.dsinnovators.keyword.driven.commons.CommonFunc;
import com.dsinnovators.keyword.driven.commons.DAO;
import com.dsinnovators.keyword.driven.commons.ExcelUtils;
import com.dsinnovators.keyword.driven.rest.RestApiCall;
import com.dsinnovators.keyword.driven.utils.Constants;
import com.dsinnovators.keyword.driven.utils.TestHelper;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



@Slf4j
public class KeywordEngine {
	private WebDriver driver;
	private static String storedText = "";

	RestApiCall restApiCall = new RestApiCall();

	public KeywordEngine(WebDriver driver) throws Exception {
		this.driver = driver;
	}


	public Boolean callRestApiGET(int rowNum,String keyword) throws Exception{
		String apiInfo = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ELEMENT_LOCATOR);
		String expectedResponse = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_WITH);
		String payload = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);

		Pattern pattern = Pattern.compile("url\\s*=\\s*(.*)\\s*,\\s*method\\s*=\\s*", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(apiInfo);
		String apiUrl, method, headers = apiUrl = method  = "";
		if (matcher.find()){
			log.info("rest url :"+ matcher.group(1));
			apiUrl = matcher.group(1);
		}
		pattern = pattern.compile("\\s*method\\s*=\\s*(.*)\\s*,\\s*header\\s*=\\s*", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(apiInfo);
		if ( matcher.find()){
			log.info("rest method :"+ matcher.group(1));
			method = matcher.group(1);
		}

		pattern = pattern.compile("\\s*header\\s*=\\s*.\\s*(.*)\\s*}", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(apiInfo);

		if ( matcher.find()){
			log.info("rest headers :"+ matcher.group(1).replaceAll("\\s",""));
			headers = matcher.group(1).replaceAll("\\s","");
		}
        MultivaluedMap headerMap = new MultivaluedHashMap();

		String headersArray[] = headers.split(",");
		for(String header: headersArray){
			String eachHeader [] = header.split(":");
			headerMap.add(eachHeader[0],eachHeader[1]);
		}
		Map<String,String> map = new HashMap<>();
		if (method.equalsIgnoreCase("get")) {
			restApiCall.init();
			map = restApiCall.getRequest(apiUrl, expectedResponse, headerMap, keyword);
			String responseBody = map.get("responseBody");
			Boolean responseValidated = map.get("responseValidated").equalsIgnoreCase("true");
			setRegistryKey(rowNum, responseBody);
			if(responseValidated==true){
				setLabelBasedOnAssertion(rowNum, true);
			}
			else {
				setLabelBasedOnAssertion(rowNum,false);
			}
			return responseValidated;
		} else if (method.equalsIgnoreCase("post")) {
			return restApiCall.postRequest(apiUrl, payload, headerMap);
		} else if (method.equalsIgnoreCase("put")) {
			return restApiCall.putRequest(apiUrl, payload, headerMap);
		} else {
			return restApiCall.deleteRequest(apiUrl, headerMap);
		}
	}
	// SetText

	public void setText(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String text = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		if(text.contains("=")) {
			String element[] = text.split("=", 2);
			if(element[0].equalsIgnoreCase("file")){
				text = CommonFunc.dirPath + File.separator + element[1];
				log.info("File Path::"+text);
			}
		}

		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		if (selectElement != null) {
			selectElement.clear();
			if(!TestHelper.isEmpty(text)) {
				selectElement.sendKeys(text);
			}
			log.info("Set text value::" + text);
			setRegistryKey(rowNum, text.trim());
		} else {
			throw new Exception("No Such Element Found.");
		}
	}
 // AUTH OTP GENERATE



	//SettextJs
	public void settextjs(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String text = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);

		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		if (selectElement != null) {
			selectElement.clear();
			if (!TestHelper.isEmpty(text)) {
				selectElement.click();
				String jsQuery = "arguments[0].value='"+ text +"'";

				JavascriptExecutor js = (JavascriptExecutor)driver;
				js.executeScript(jsQuery, selectElement);

				selectElement.sendKeys(Keys.TAB);
			}
			log.info("Set text value::" + text);
			setRegistryKey(rowNum, text.trim());
		} else {
			throw new Exception("No Such Element Found.");
		}

	}

	public void reload(int rowNum)throws Exception{
		log.info("Performing Page Reload");
		CommonFunc.waitForPageLoad(driver, rowNum);
		this.driver.navigate().refresh();

	}

	public void goBrowserBack(int rowNum) throws Exception {
		log.info("Performing browser back.");
		CommonFunc.waitForPageLoad(driver, rowNum);
		this.driver.navigate().back();
	}


	// Absolute File upload Path

//	public void uploadFile(int rowNum) throws Exception {
//		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
//		CommonFunc.waitForPageLoad(driver, rowNum);
//		String filePath = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
//
//		File file = new File(filePath);
//		String absolutePath = file.getAbsolutePath();
//		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
//		if (selectElement != null) {
//			selectElement.clear();
//			if (!TestHelper.isEmpty(absolutePath)) {
//				selectElement.sendKeys(absolutePath);
//			}
//			log.info("Set text value::" + absolutePath);
//			setRegistryKey(rowNum, absolutePath.trim());
//		} else {
//			throw new Exception("No Such Element Found.");
//		}
//	}
//
	// Gettext

	public String getText(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String text = CommonFunc.getValueWithSelectorTypeAndLocator(rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"), CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_TYPE), driver);
		log.info("Get the text ::" + text);
		storedText = text;
		setRegistryKey(rowNum, text);
		return text;
	}

	// GetInputText

	public String getInputText(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String text = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator")).getAttribute("value");
		log.info("Get the input text :: " + text);
		storedText = text;
		setRegistryKey(rowNum, text);
		return text;
	}
//auth otp

	public void setAuthOtp(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		log.info("Before settext");
		String text = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);

		log.info("After settext");
		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		log.info("MIDDLE settext");
		if (selectElement != null) {
			selectElement.clear();
			if(!TestHelper.isEmpty(text)) {
				log.info("ajira print " + text);
				String otp = TestHelper.getTOTPCode(text);
				log.info("Generated otp " + otp);
				selectElement.sendKeys(otp);
			}
			log.info("Set text value::" + text);
			setRegistryKey(rowNum, text.trim());
		} else {
			throw new Exception("No Such Element Found.");
		}
	}

//set faker method
	public void setFaker(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String text = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		String fakeValue = getFakeValue(text);
		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		if (selectElement != null) {
			selectElement.clear();
			if(!TestHelper.isEmpty(fakeValue)) {

				selectElement.sendKeys(fakeValue);
			}
			log.info("Set text value::" + fakeValue);
			setRegistryKey(rowNum, fakeValue.trim());
		} else {
			throw new Exception("No Such Element Found.");
		}
	}
	// JavaScript Executor Click
	public void JsClick(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		WebElement jsElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("arguments[0].click();", jsElement);

	}
	private String getFakeValue(String text) {
		Faker faker = new Faker();
		if(text.equals("FIRST_NAME"))
			return faker.name().firstName(); // Emory
		if(text.equals("LAST_NAME"))
			return faker.name().lastName();

		if(text.equals("ADDRESS"))
			return faker.address().streetAddress(); // Emory

		if(text.equals("CITY"))
			return faker.address().city(); // Emory
		if(text.equals("PHONE"))
			return faker.phoneNumber().phoneNumber(); // Emory

		return  null;
	}
	// GetTitle

	public String getTitle(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String title = CommonFunc.getValueWithSelectorTypeAndLocator(rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"), CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_TYPE), driver);
		log.info("Get the title ::" + title);
		storedText = title;
		setRegistryKey(rowNum, title);
		return title;
	}

	//setDateValue
//	public void SetDateValue(int rowNum) throws Exception {
//		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
//		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
//		Actions builder=new Actions(driver);
//		Action date= builder.sendKeys(selectElement, "10-10-1990").build();
//		date.perform();
//	}
	public void SetDateSetText(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String text = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		//new add
		if(text.contains("=")) {
			String element[] = text.split("=", 2);
			if(element[0].equalsIgnoreCase("file")){
				text = CommonFunc.dirPath + File.separator + element[1];
				log.info("File Path::"+text);
			}
		}

		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		Actions builder=new Actions(driver);
		//Action date= builder.sendKeys(selectElement, "10-10-1990").build();
		Action date= builder.sendKeys(selectElement, (text)).build();
		date.perform();
	}

	// Click

	public void click(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		if (selectElement != null) {
			log.info("Element clicked.");
			selectElement.click();
		} else {
			throw new Exception("No Such Element Found.");
		}

	}


	// Right Click

	public void rightClick(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		Actions action = new Actions(driver);
		if (selectElement != null) {
			log.info("Element clicked.");
			action.contextClick(selectElement).perform();
		} else {
			throw new Exception("No Such Element Found.");
		}
	}

    //Drag And Drop
    public void dragAndDrop(int rowNum) throws Exception {
        List <Map<String, String>> locatorSelectionType = multipleElementLocatorSelectionType(rowNum);
        WebElement element[]= new WebElement[locatorSelectionType.size()];
        //log.info("\n\ndest: "+locatorSelectionType);
        for(int i=0; i<locatorSelectionType.size(); i++) {
            Map<String, String> sourceLocatorSelectionType = locatorSelectionType.get(i);
            element[i] = CommonFunc.getPageElementWithWait(driver, rowNum, sourceLocatorSelectionType.get("selectorType"),sourceLocatorSelectionType.get("locator")  );
        }
        Actions action = new Actions(driver);
        action.dragAndDrop(element[0], element[1]).build().perform();
        //action.clickAndHold(element[0]).moveToElement(element[1]).release(element[1]).build().perform();

    }

////	 //upload file
//	 public void upload(int rowNum) throws Exception {
////		 Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
////		 WebElement uploading = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
////		 uploading.sendKeys("C:\\Users\\Imran\\Desktop\\Desktop\\Bat.jpg");
//		 driver.findElement(By.id("imageFile")).sendKeys("C:\\Users\\Imran\\Desktop\\Desktop\\Blue\\wats.jpg");
////		 }
//	 }


	//Mouse hover
	public void hover(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String hover = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		log.info("HOVER ---> Starting ");
		WebElement element = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));

		//WebElement element = driver.findElement(By.linkText("Marketing"));

		// hover Operation
		Actions action = new Actions(driver);
		action.moveToElement(element).perform();

		log.info("HOVER --------------> FInished ");


	}

	// Select ( need check if select or not)


	public void select(int rowNum, boolean checked) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);

		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		if (selectElement != null) {
			if ((checked && !selectElement.isSelected()) || (!checked && selectElement.isSelected())) {
				selectElement.click();
				log.info("Element selected.");
			}
		} else {
			throw new Exception("No Such Element Found.");
		}

	}


	public void setStoredText(String storedText) {
		this.storedText = storedText;
	}

	public String getStoredText() {
		return this.storedText;
	}

	// Clear

	public void clear(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		if (selectElement != null) {
			log.info("Perform clearing.");
			selectElement.clear();
		} else {
			throw new Exception("No Such Element Found.");
		}

	}

	//executeKeyEvent

	public void executeKeyEvent(int rowNum) throws Exception{
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String keyValue = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		WebElement webElement = CommonFunc.getPageElementWithWait(driver,rowNum,locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		if(Keys.valueOf(keyValue)!=null){
			webElement.sendKeys(Keys.valueOf(keyValue));
		}else {
			webElement.sendKeys(keyValue);
		}
	}


	//hasElement

	public boolean hasElement(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		boolean hasData = false;
		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));

		if (selectElement == null) {
			hasData = false;
		} else {
			hasData = true;
		}
		if (hasData) {
			log.info("Attribute found");
			setLabelBasedOnAssertion(rowNum, true);
			setRegistryKey(rowNum, String.valueOf(hasData));
		} else {
			log.info("Attribute not found");
			setLabelBasedOnAssertion(rowNum, false);
		}

		return hasData;
	}

	//Increment

	public String incNum(int rowNum) throws Exception {
		int incVal = 0;
		String num = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ELEMENT_LOCATOR);
		String value = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		if(num!=null){
			incVal = Integer.parseInt(value)+Integer.parseInt(num);
		}
		else{
			incVal = Integer.parseInt(value)+1;
		}
		String idNum = String.valueOf(incVal);
		//storedText = value;
		log.info("After incrementing or decrementing id number : " + idNum);
		//log.info("Stored Number : " + storedText);
		setRegistryKey(rowNum, String.valueOf(idNum));

		return idNum;
	}

	//hasSameElement

	public boolean hasSameValue(int rowNum) throws Exception {
		//Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		//CommonFunc.waitForPageLoad(driver, rowNum);
		try{
			String value = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
			log.info("Get the value ::" + value);
			String assertVal = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_WITH);
			log.info("Get the assertion value ::" + assertVal);
			if(!TestHelper.isEmpty(assertVal) && assertVal.equalsIgnoreCase("null")){
				assertVal = "";
			}
			log.info("Expected value ::"+assertVal);
			log.info("Value found::"+value);
			if (value != null && value.equalsIgnoreCase(assertVal)) {
				log.info("Expected value has found.");
				setLabelBasedOnAssertion(rowNum,true);
				setRegistryKey(rowNum,value);
				return true;
			} else {
				log.info("Expected value has not found.");
				setLabelBasedOnAssertion(rowNum,false);
			}
		}catch(Exception e){
			log.info("Element not found.");
			setLabelBasedOnAssertion(rowNum,false);
		}
		return false;

	}


	//removeAttribute

	public void removeAttribute(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		try{
			String value = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
			WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
			((JavascriptExecutor)driver).executeScript("arguments[0].removeAttribute('"+value+"')", selectElement);
			log.info("Successfully removed the readonly property");
		}catch(Exception e){
			log.info("Element not found.");
		}
	}



	// Element Edit function


	public void ElementExcecutor(int rowNum) throws Exception {
		log.info("Inject Element is entering");
		//for MM2 page size drop down
		String d = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.SEED_DATA);
	int injectElem = 1;
		if(d != null && !"".equals(d)) {
			try {
				injectElem = Integer.parseInt(d);
			}catch (Exception ex) {

			}
		}
		log.info("Inject Element ? "+ injectElem);
		if(injectElem == 1) {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			//js.executeScript("document.getElementsByName('pageSize')[0].innerHTML = '<option value=\"2000\">20000</option>';");
			//js.executeScript("document.getElementsByName('pageSize')[1].innerHTML = '<option value=\"2000\">2000</option>'<option value=\"20\">20</option>';");
			js.executeScript("document.getElementsByName('pageSize')[0].innerHTML = '<option value=\"10\">10</option><option value=\"2000\">20000</option>';");
			//js.executeScript("document.getElementByName('pageSize').appendChild(document.createTextNode(' 2000'))");
			//js.executeScript("document.getElementByName('//pageSize').setAttribute('option value', '2000')");
			log.info("Inject Element is entering");

		}


//		WebElement element = driver.findElement(By.name("pageSize']"));
//		JavascriptExecutor js= (JavascriptExecutor)driver;
//		js.executeScript("arguments[0].innerText = '2000'", element);
//		//js.executeScript("document.getElementById('//id of element').setAttribute('attr', '10')");
//		log.info("JS executor is entering");
//		if (selectElement != null) {
//			log.info("Perform clearing.");
//			selectElement.clear();
//		} else {
//			throw new Exception("No Such Element Found.");
//		}

	}


// Switch Alert

	public void switchWindow(int rowNum) throws Exception {

		log.info("Window Alert is working again");
		//String myWindowHandle = driver.getWindowHandle();
		//log.info("Parent window id : " + myWindowHandle);
		Set<String> windows = driver.getWindowHandles();
		//int count =windows.size();
		//log.info("Total window count :"+ count);
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		if (TestHelper.isEmpty(locatorSelectionType.get("locator"))) {
			log.info("Switching to main Window");
			driver.switchTo().defaultContent();
		} else {
			int windowCount = Integer.parseInt(locatorSelectionType.get("locator"));
			int counter = 1;
			for (String childwindow : windows) {
				//if(!myWindowHandle.equalsIgnoreCase(childwindow)){
				if (windowCount == counter) {
					driver.switchTo().window(childwindow);
					break;
				}
				counter++;
			}
		}
		/*for(String childwindow: windows){
			if(!myWindowHandle.equalsIgnoreCase(childwindow)){
				driver.switchTo().window(childwindow);
				break;
			}

		}*/

		/*Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		if(TestHelper.isEmpty(locatorSelectionType.get("locator"))){
			log.info("Switching to main Alert.");
			//Switching back to Parent Window
			String Parent_Window = driver.getWindowHandle();
			driver.switchTo().window(Parent_Window);
		}else {
			WebElement switchframe = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
			log.info("Switching to Alert");


			for (String Child_Window : driver.getWindowHandles())
			{
				driver.switchTo().window(Child_Window);
				//Perform operation on child window
				//driver.close();
			}


		}
*/


	}

	// Switch Frame

	public void switchFrame(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		if (TestHelper.isEmpty(locatorSelectionType.get("locator"))) {
			log.info("Switching to main.");
			driver.switchTo().parentFrame();
		} else {
			WebElement switchframe = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
			log.info("Switching to iframe");
			driver.switchTo().frame(switchframe);
		}
		//driver.switchTo().alert().accept();
		//log.info("Switch to Alert Button");

	}

// Switch Alert

	public void switchAlert(int rowNum) throws Exception {

		log.info("In alert is working again");
		//String myWindowHandle = driver.getWindowHandle();
		//log.info("Parent window id : " + myWindowHandle);
		Set<String> windows=driver.getWindowHandles();
		//int count =windows.size();
		//log.info("Total window count :"+ count);
		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		if(TestHelper.isEmpty(locatorSelectionType.get("locator"))){
			log.info("Switching to main.");
			driver.switchTo().defaultContent();
		}else {
			int windowCount = Integer.parseInt(locatorSelectionType.get("locator"));
			int counter = 1;
			for(String childwindow: windows){
				//if(!myWindowHandle.equalsIgnoreCase(childwindow)){
				if(windowCount==counter){
					driver.switchTo().window(childwindow);
					break;
				}
				counter++;
			}
		}
		/*for(String childwindow: windows){
			if(!myWindowHandle.equalsIgnoreCase(childwindow)){
				driver.switchTo().window(childwindow);
				break;
			}

		}*/

		/*Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		if(TestHelper.isEmpty(locatorSelectionType.get("locator"))){
			log.info("Switching to main Alert.");
			//Switching back to Parent Window
			String Parent_Window = driver.getWindowHandle();
			driver.switchTo().window(Parent_Window);
		}else {
			WebElement switchframe = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
			log.info("Switching to Alert");


			for (String Child_Window : driver.getWindowHandles())
			{
				driver.switchTo().window(Child_Window);
				//Perform operation on child window
				//driver.close();
			}


		}
*/


	}
	// Java Script Pop up Alert
	public void popUpAlert(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		if (TestHelper.isEmpty(locatorSelectionType.get("locator"))) {
			log.info("JavaScript Accept ......");
			Alert alert = driver.switchTo().alert();
			String alertMessage = driver.switchTo().alert().getText();
			// Display Alert Message
			System.out.println(alertMessage);
			alert.accept();
			log.info("Javascript Clicked  successfully");
//	} else {
//		WebElement popUpAlert = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
//		log.info("Switching to Alert");
//		if(popUpAlert==null){
//			Alert alert=driver.switchTo().alert();
//			String alertMessage= driver.switchTo().alert().getText();
//			// Display Alert Message
//			System.out.println(alertMessage);
//			alert.dismiss();
//			popUpAlert.click();
//			log.info("Javascript Dismissed successfully");
//		}
//		//driver.switchTo().alert().accept();
		}
	}
	// JavaScript Pop UP Alert

//	public void popUpAlert(int rowNum) throws Exception {
//		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
//		WebElement popUpAlert = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
//		if(popUpAlert!=null){
//			Alert alert=driver.switchTo().alert();
//			log.info("Javascript Alert clicked.");
//			String alertMessage= driver.switchTo().alert().getText();
//			// Display Alert Message
//		System.out.println(alertMessage);
//			alert.accept();
//			popUpAlert.click();
//			log.info("Javascript Clicked Successfully");
//		}else{
//			throw new Exception("Alert not Clicked somehow");
//		}
//
//	}
//



//	// Dynamic Click
//
//	public void dynamicClick(int rowNum) throws Exception {
//		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
//		CommonFunc.waitForPageLoad(driver, rowNum);
//		// User Static Variable for finding
//		String selectedValue = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_WITH);
//
//		List<WebElement> drop = this.driver.findElements(By.tagName("span"));
//
//		for (int i = 0; i < drop.size(); i++) {
//			String value = drop.get(i).getText();
//			if (value.equalsIgnoreCase(selectedValue)) {
//				drop.get(i).click();
//				//Thread.sleep(2000);
//				break;
//			}
//
//		}
//
//	}
	public void dynamicClick(int rowNum) throws Exception {
		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		// User Static Variable for finding

		String selectedValue = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_WITH);
		String cssSelectorStr = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ELEMENT_LOCATOR);


		String cssSelector = cssSelectorStr.split("=")[1];

		List<WebElement> drop = this.driver.findElements(By.cssSelector(cssSelector));

		for (int i = 0; i < drop.size(); i++) {
			String value = drop.get(i).getText();
			if (value.equalsIgnoreCase(selectedValue)) {
				drop.get(i).click();
				break;
			}

		}

	}

	public boolean hasAttribute(int rowNum) throws Exception {

		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		boolean hasData = false;
		if(!TestHelper.isEmpty(CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_TYPE))) {
			String data = CommonFunc.getValueWithSelectorTypeAndLocator(rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"), CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_TYPE), driver);
			if (data == null) {
				hasData = false;
			} else {
				hasData = true;
			}
			if (hasData) {
				log.info("Attribute found");
				setLabelBasedOnAssertion(rowNum, true);
				setRegistryKey(rowNum, String.valueOf(hasData));
			} else {
				log.info("Attribute not found");
				setLabelBasedOnAssertion(rowNum, false);
			}
		} else {
			log.info("Assertion type not given.");
		}
		return hasData;

	}


	// Pagination

	public void clickp(int rowNum) throws Exception {

		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String tableId = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		WebElement nextButton =driver.findElement(By.id(tableId+"_next"));
		log.info(nextButton.getAttribute("class")+" : class");

		List<WebElement> pagination =
				driver.findElements(By.cssSelector("#"+tableId+"_paginate > ul > li"));
		log.info(pagination.size()+"****");
		for(int i = 0; i < pagination.size(); i++){
			String elementClassVal = pagination.get(i).getAttribute("class");
			log.info(elementClassVal+" : val");
			if(nextButton.getAttribute("class").contains("disabled") == false && elementClassVal.contains("paginate_button")){pagination.get(i).click();
			}
			nextButton = driver.findElement(By.id(tableId+"_next"));
			pagination = driver.findElements(By.cssSelector("#"+tableId+"_paginate > ul > li"));
		}
	}

	// Pagination  Infodocs report

	public void Pagination(int rowNum) throws Exception {

		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String tableId = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		log.info("PAGINATION");
//		WebElement nextButton =driver.findElement(By.id(tableId+"_next"));
//		//WebElement nextButton =driver.findElement(By.tagName("a"));
//		log.info("Nextbutton : " + nextButton);
//		log.info(nextButton.getAttribute("class")+" : class");
//
//	List<WebElement> pagination =driver.findElements(By.cssSelector("#"+tableId+"_paginate > ul > li > a"));
//		//List<WebElement> pagination =driver.findElements(By.tagName("a"));
//		log.info(pagination.size()+"****");
//		for(int i = 0; i < pagination.size(); i++){
//			//String elementClassVal = pagination.get(i).getAttribute("class");
//			String elementClassVal = pagination.get(i).getAttribute("class");
//			log.info(elementClassVal+" : val");
//			if(nextButton.getAttribute("class").contains("disabled") == false && elementClassVal.contains("paginate_button")){pagination.get(i).click();
//				break;
//			}
//			nextButton = driver.findElement(By.id(tableId+"_next"));
//			//nextButton =driver.findElement(By.tagName("a"));
//			pagination = driver.findElements(By.cssSelector("#"+tableId+"_paginate > ul > li > a"));
//			//pagination = driver.findElements(By.cssSelector("#detailsTable_paginate > ul > li.paginate_button.active > a"));


		List<WebElement> activePages = driver.findElements(By.cssSelector("#detailsTable_paginate li[class='paginate_button ']"));
		//CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		WebElement lastActivePageElem = activePages.get(activePages.size() - 1);
		int totalPageCount = Integer.parseInt(lastActivePageElem.getText().trim());

		for (int i = 1; i <= totalPageCount; i++) {
			driver.findElement(By.cssSelector(String.format("#detailsTable_paginate li a[data-dt-idx='%s']", i))).click();
			Thread.sleep(5000);
			log.info(String.format("Page %s loaded", i));
		}


	}




		/*List<WebElement> pagination =driver.findElements(By.cssSelector("#detailsTable_paginate > ul > li"));



		int totalPages = pagination.size();
		log.info("GETTING ALL link");
		for(int i = 0; i < totalPages; i++){
			boolean isLastPage =driver.findElement(By.id("detailsTable_next")).getAttribute("class").contains("disabled");
			log.info("Check Last page and disabled");
			if(isLastPage){
				break;

			}

			//pagination.get(i).click();

            //wait for new page to load.
			Thread.sleep(5000);
          log.info("each link clicking one by one ");
			pagination =driver.findElements(By.cssSelector("#detailsTable_paginate > ul > li"));
		}*/




   // Pagination Campaign
   public void PaginationCamp(int rowNum) throws Exception {

	   Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
	   CommonFunc.waitForPageLoad(driver, rowNum);
	   String tableId = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
	   log.info("PAGINATION-- >......... Campaign ---> Started ");
	   List<WebElement> activePages = driver.findElements(By.cssSelector("#emailCampaignDT_paginate a[class='paginate_button ']"));

	   WebElement lastActivePageElem = activePages.get(activePages.size() - 1);
	   int totalPageCount = Integer.parseInt(lastActivePageElem.getText().trim());

	   for (int i = 1; i <= totalPageCount; i++) {
		   driver.findElement(By.cssSelector(String.format("#emailCampaignDT_paginate a[data-dt-idx='%s']", i))).click();
		   Thread.sleep(5000);
		   log.info(String.format("Page %s loaded", i));
		   log.info("PAGINATION ---->... Campaign ---> Finished ");

	   }

   }







	// Sorting

	public void clicks(int rowNum) throws Exception {

		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String tableId = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		List<WebElement> sortingPage =driver.findElements(By.cssSelector("#"+tableId+" > thead > tr > th" ));

		log.info(sortingPage.size()+"****");
		for(int i = 0; i < sortingPage.size(); i++){
			String elementClassVal = sortingPage.get(i).getAttribute("class");
			log.info(elementClassVal+" : val");
			sortingPage.get(i).click();
			sortingPage =driver.findElements(By.cssSelector("#"+tableId+" > thead > tr > th"));
		}
	}


	// Dynamic Select 
	public void dynamicSelect(int rowNum) throws Exception {

		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String elementName = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		WebElement divclass=driver.findElement(By.cssSelector("table[aria-describedby='"+elementName+"_info']"));
		log.info("Print div: " + divclass.getTagName());
		WebElement tbodyTag = divclass.findElement(By.tagName("tbody"));
		List<WebElement> trTags = tbodyTag.findElements(By.tagName("tr"));
		log.info("Size of tr : " + trTags.size());
		List<WebElement> tdTags = trTags.get(0).findElements(By.tagName(("td")));

		log.info("Size of td : " + tdTags.size());
		WebElement addtag=tdTags.get(6).findElement(By.cssSelector("img[class='extend-btn pointer']"));

	}


	public void setLabelBasedOnAssertion(int rowNum, boolean assertResult) throws Exception {
		String assertionAction = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_ACTION);
		String nextLabelForTrue = "";
		String nextLabelForFalse = "";
		if(!TestHelper.isEmpty(assertionAction)){
			String actions[] = assertionAction.split("\\|\\|");
			if(actions.length>1){
				if(!TestHelper.isEmpty(actions[1])){
					String actionFor[] = actions[1].split("=", 2);
					if(actionFor.length==2){
						if(actionFor[0].trim().equalsIgnoreCase("true")){
							nextLabelForTrue = actionFor[1].trim();
						}else if(actionFor[0].trim().equalsIgnoreCase("false")){
							nextLabelForFalse = actionFor[1].trim();
						}
					}
				}
			}
			if(!TestHelper.isEmpty(actions[0])){
				String actionFor[] = actions[0].split("=", 2);
				if(actionFor.length==2){
					if(actionFor[0].trim().equalsIgnoreCase("true")){
						nextLabelForTrue = actionFor[1].trim();
					}else if(actionFor[0].trim().equalsIgnoreCase("false")){
						nextLabelForFalse = actionFor[1].trim();
					}
				}
			}
		}
		if(assertResult) {
			if(!TestHelper.isEmpty(nextLabelForTrue)) {
				log.info("As assertion is true, so next label is::"+nextLabelForTrue);
				CommonFunc.labelChange = true;
				CommonFunc.Label = nextLabelForTrue;
			}
		}else{
			if(!TestHelper.isEmpty(nextLabelForFalse)) {
				log.info("As assertion is false, so next label is::"+nextLabelForFalse);
				CommonFunc.labelChange = true;
				CommonFunc.Label = nextLabelForFalse;
			}
		}
	}

	// Assert Value Like Match

	public boolean assertLikeMatch(int rowNum) throws Exception {
		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		try{
			String value = CommonFunc.getValueWithSelectorTypeAndLocator(rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"), CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_TYPE), driver);
			String assertVal = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_WITH);
			if(!TestHelper.isEmpty(assertVal) && assertVal.equalsIgnoreCase("null")){
				assertVal = "";
			}
			log.info("Expected value ::"+assertVal);
			log.info("Value found::"+value);
			if (value != null && value.toLowerCase().contains(assertVal.toLowerCase())) {
				log.info("Expected value has found.");
				setLabelBasedOnAssertion(rowNum,true);
				setRegistryKey(rowNum,value);
				return true;
			} else {
				log.info("Expected value has not found.");
				setLabelBasedOnAssertion(rowNum,false);
			}
		}catch(Exception e){
			log.info("Element not found.");
			setLabelBasedOnAssertion(rowNum,false);
		}
		return false;

	}


	// Assert Value

	public boolean assertValue(int rowNum) throws Exception {
		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		try{
			String value = CommonFunc.getValueWithSelectorTypeAndLocator(rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"), CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_TYPE), driver);
			String assertVal = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_WITH);
			if(!TestHelper.isEmpty(assertVal) && assertVal.equalsIgnoreCase("null")){
				assertVal = "";
			}
			log.info("Expected value ::"+assertVal);
			log.info("Value found::"+value);
			if (value != null && value.equalsIgnoreCase(assertVal)) {
				log.info("Expected value has found.");
				setLabelBasedOnAssertion(rowNum,true);
				setRegistryKey(rowNum,value);
				return true;
			} else {
				log.info("Expected value has not found.");
				setLabelBasedOnAssertion(rowNum,false);
			}
		}catch(Exception e){
			log.info("Element not found.");
			setLabelBasedOnAssertion(rowNum,false);
		}
		return false;

	}

	// Assert Presence

	public boolean assertPresence(int rowNum) throws Exception {
		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		System.out.println("locatorSelectionType.get(\"selectorType\") = " + locatorSelectionType.get("selectorType"));
		System.out.println("locatorSelectionType.get(\"locator\") = " + locatorSelectionType.get("locator"));
		String element = CommonFunc.getValueWithSelectorTypeAndLocator(rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"), CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_TYPE), driver);
		String assertVal = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_WITH);
		if(!TestHelper.isEmpty(assertVal) && assertVal.equalsIgnoreCase("null")){
			log.info("Expected value ::"+assertVal);
			assertVal = "";
		}
		if (element != null && element.equalsIgnoreCase(assertVal)) {
			log.info("Expected value has found.");
			setRegistryKey(rowNum,element);
			setLabelBasedOnAssertion(rowNum,true);
			return true;
		} else {
			log.info("Expected value has not found.");
			setLabelBasedOnAssertion(rowNum,false);
		}
		return false;
	}


	// Drop Down

	public void option(int rowNum) throws Exception {
		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		Select select = new Select(selectElement);
		String value = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		/////////////////To support old sheet. will be remover after all sheet modified.
		String assertValue = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_WITH);
		if(TestHelper.isEmpty(value) && !TestHelper.isEmpty(assertValue) ){
			value = assertValue;
		}
		/////////////////To support old sheet. will be remover after all sheet modified.
		String valuePart[] = value.split("=", 2);
		if(valuePart.length == 2 && ( valuePart[0].trim().toLowerCase().startsWith("value") || valuePart[0].trim().toLowerCase().startsWith("index") )) {
			if(valuePart[0].trim().toLowerCase().startsWith("value")){
				log.info("Select dropdown with value :: "+valuePart[1].trim());
				select.selectByVisibleText(valuePart[1].trim());
				return;
			}else if(valuePart[0].trim().toLowerCase().startsWith("index")){
				try {
					log.info("Select dropdown with index :: "+valuePart[1].trim());
					int index = Integer.parseInt(valuePart[1].trim());
					select.selectByIndex(index);
				}catch (Exception ex){
					ex.printStackTrace();
				}
				return;
			}
		}
		log.info("Select dropdown with visible text :: "+value);
		select.selectByVisibleText(value);

	}

	public void commonDropdownOptionCount(int rowNum) throws Exception {
		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		//get the data from cxcel cell
		String dpdwnlists = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ELEMENT_LOCATOR);
		//remove any white space in the cell data
		String str=dpdwnlists.replaceAll("\\s","");
		// remove "xpath=" keyword from cell data
		String[] trimed=str.split("xpath=");
		List<WebElement> links=driver.findElements(By.xpath(trimed[1]));
		int optionCount = 0;
		if(links!=null && links.size()>0){
			optionCount = links.size();
			log.info("List of size : " + optionCount);
		}
		setRegistryKey(rowNum,String.valueOf(optionCount));
	}
	public void close(int rowNum)throws Exception{
		driver.close();
	}

	//ScrollDown
	public void scrollDown(int rowNum) throws Exception {

		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String keyValue = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		WebElement Element = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		log.info("Scrooliiing entry");
		JavascriptExecutor js = ((JavascriptExecutor) driver);
		js.executeScript("arguments[0].scrollIntoView();", Element);
		js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
		js.executeScript("window.scrollBy(0,250)");
		log.info("Javascript executor for scroll down");

	}

	public void dropdownOptionCount(int rowNum) throws Exception {
		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		Select select = new Select(selectElement);
		List<WebElement> options = select.getOptions();
		int optionCount = 0;
		if(options!=null && options.size()>0){
			optionCount = options.size();
			log.info("List of size : " + optionCount);
		}
		setRegistryKey(rowNum,String.valueOf(optionCount));
	}

	public void dropdownValueCount(int rowNum) throws Exception {
		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		//get the data from cxcel cell
		String dpdwnlists = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ELEMENT_LOCATOR);
		//remove any white space in the cell data
		String str=dpdwnlists.replaceAll("\\s","");
		// remove "xpath=" keyword from cell data
		String[] trimed=str.split("xpath=");
		//if xpath contains select keyword
		if(trimed[1].contains("select"))
		{
			WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
			Select select = new Select(selectElement);
			List<WebElement> options = select.getOptions();
			int valueCount = 0;
			if (options != null && options.size() > 0) {
				valueCount = options.size();
				log.info("List of size : " + valueCount);
			}
			setRegistryKey(rowNum, String.valueOf(valueCount));
			System.out.println("I am in Select");
		}
		// for general dropdowns
		else
		{
			List<WebElement> links=driver.findElements(By.xpath(trimed[1]));
			int valueCount = 0;
			valueCount=links.size();
			System.out.println("total option:"+links.size());
			setRegistryKey(rowNum,String.valueOf(valueCount));
			System.out.println("I am in Xpath");
		}

	}

	public void clickMultiplelinks(int rowNum) throws Exception {

		Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		WebElement clickmultiple = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		String alllinks = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ELEMENT_LOCATOR);
		System.out.println("All links Count:" +alllinks);

		List<WebElement> links=driver.findElements(By.xpath(alllinks));
		System.out.println("no of links:" +links.size());
		//WebElement q;
		for(int i=1;i<=links.size();i++)
		{
			driver.findElement(By.xpath(alllinks+"["+i+"]")).click();
			Thread.sleep(3000);

			driver.findElement(By.xpath("(//span[text()='Accounts'])[1]")).click();
			Thread.sleep(3000);

		}

	}
	/*
	public void dropdownOptionCount(int rowNum) throws Exception {
		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		Select select = new Select(selectElement);
		List<WebElement> options = select.getOptions();
		int optionCount = 0;
		if(options!=null && options.size()>0){
			optionCount = options.size();
			log.info("List of size : " + optionCount);
		}
		setRegistryKey(rowNum,String.valueOf(optionCount));
	}
	*/

	// AssertSelection
	public boolean assertSelection(int rowNum) throws Exception {
		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);

		WebElement webElement = CommonFunc.getPageElementWithWait(driver,rowNum,locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));

		if ( webElement != null ) {
			if (webElement.isSelected()) {
				log.info("Element is selected.");
				setRegistryKey(rowNum, "true");
				setLabelBasedOnAssertion(rowNum, true);
				return true;
			} else {
				log.info("Element is not selected.");
				setLabelBasedOnAssertion(rowNum,false);
				return false;
			}
		}else {
			setLabelBasedOnAssertion(rowNum,false);
			return false;
		}
	}

	public Map<String,String> separateLocatorSelectionType(int rowNum)throws Exception{
		String elementPath = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ELEMENT_LOCATOR);
		String element[] = elementPath.split("=", 2);
		Map<String,String> locatorSelectionType = new HashMap<>();
		if(element.length == 2) {
			locatorSelectionType.put("selectorType", element[0]);
			locatorSelectionType.put("locator", element[1]);
			return locatorSelectionType;
		}else {
			throw new Exception("Can not separate location and selection type. Guessing format error.");
		}
	}

    // Added separateDropLocatorSelectionType for testing

    public List<Map<String,String>> multipleElementLocatorSelectionType(int rowNum)throws Exception{
        String elementPath = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ELEMENT_LOCATOR);
        String multipleElement[] = elementPath.split(",", 2);
        List<Map<String,String>> locatorSelectionTypeArray = new ArrayList<Map<String,String>>();
        for(int i=0; i<multipleElement.length; i++) {
            String element[] = multipleElement[i].split("=", 2);
            Map<String,String> locatorSelectionType = new HashMap<>();
            if(element.length == 2) {
                locatorSelectionType.put("selectorType", element[0]);
                locatorSelectionType.put("locator", element[1]);
                locatorSelectionTypeArray.add( locatorSelectionType);
            }else {
                throw new Exception("Can not separate location and selection type. Guessing format error.");
            }
        }
        return  locatorSelectionTypeArray;

    }

	// AssertCSS

	public boolean assertCss(int rowNum) throws Exception {

		String color = "";
		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		WebElement a3 = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		String assertion = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_WITH);
		String values[] = assertion.split(";");
		for (String bg : values) {
			if (bg != null && bg.length() > 1) {
				String[] splits = bg.split(":");
				log.info(splits[0] + "->" + splits[1]);
				String cssValue = getValueFromCss(splits[0].trim(), a3);
				if (cssValue == null)
					break;
				if (splits[1] != null
						&& (splits[1].trim()).equalsIgnoreCase(cssValue)) {
					log.info(splits[0] + " Matched!!! ");
					setLabelBasedOnAssertion(rowNum,true);
					return true;
				} else {
					log.info(splits[0] + " Not Matched!!!!!!! ");
					setLabelBasedOnAssertion(rowNum,false);
					return false;
				}
			}else{
				setLabelBasedOnAssertion(rowNum,false);
				return false;
			}
		}
		log.info(" Assert Finished!!!!!!! ");
		return false;
	}

	// gotoURL

	public void gotoURL(int rowNum) throws Exception {
		CommonFunc.baseUrl = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		CommonFunc.gotoURL(driver, CommonFunc.baseUrl);
		setRegistryKey(rowNum, CommonFunc.baseUrl);
		log.info("URL Set::"+CommonFunc.baseUrl);
	}

	public void setRegistryKey(int rowNum, String value) throws Exception {
		String keyName = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.REGISTRY_KEY);
		if(!TestHelper.isEmpty(keyName)){
			if(!TestHelper.isEmpty(value)) {
				CommonFunc.registryKeyMap.put(keyName, value);
				log.info("RegKey has been added. keyName::"+keyName+"::value::"+value);
			}else{
				log.info("Empty RegKey has been added. keyName::"+keyName+"::value::");
				CommonFunc.registryKeyMap.put(keyName, "");
			}
		}
	}

	public String getRegistryKey(String keyName) {
		if(CommonFunc.registryKeyMap.containsKey(keyName)){
			return CommonFunc.registryKeyMap.get(keyName);
		}
		return null;
	}

	public void setStoredTextFromRegKey(String keyName){
		String regKeyValue = getRegistryKey(keyName);
		setStoredText(regKeyValue);
	}

	// get css value
	public String getValueFromCss(String key, WebElement webElement) {
		if (key == null || key == "")
			return null;
		String value = webElement.getCssValue(key);
		if (value.indexOf("rgb") != -1) {
			return convertRgbToHex(value);
		}
		return value;
	}

	public String convertRgbToHex(String RGB) {
		String s1 = RGB.substring(6);
		StringTokenizer st = new StringTokenizer(s1);
		int r = Integer.parseInt(st.nextToken(",").trim());
		int g = Integer.parseInt(st.nextToken(",").trim());
		int b = Integer.parseInt(st.nextToken(",").trim());
		Color c = new Color(r, g, b);
		String hex = "#" + Integer.toHexString(c.getRGB()).substring(2);
		log.info("HEX VALUE:" + hex);
		return hex;
	}

	public void executeQuery()throws Exception {
		DAO dao = new DAO(CommonFunc.jdbcUrl, CommonFunc.jdbcUser, CommonFunc.jdbcPass);
		dao.executeSqlQuery(CommonFunc.query);
		dao.closeConnection();
	}

	public void getDBColumnData(int rowNum) throws Exception {
		DAO dao=new DAO(CommonFunc.jdbcUrl, CommonFunc.jdbcUser, CommonFunc.jdbcPass);
		CommonFunc.dbResult=dao.getData(CommonFunc.query, CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN));
		dao.closeConnection();
		if(!TestHelper.isEmpty(CommonFunc.dbResult)){
			log.info("Get column data from DB ::"+CommonFunc.dbResult);
			setLabelBasedOnAssertion(rowNum,true);
			setRegistryKey(rowNum,CommonFunc.dbResult);
		}else{
			log.info("Get column data from DB ::"+CommonFunc.dbResult);
			setLabelBasedOnAssertion(rowNum,false);
		}
	}

	public boolean checkDBHasData(int rowNum) throws Exception {
		DAO dao=new DAO(CommonFunc.jdbcUrl, CommonFunc.jdbcUser, CommonFunc.jdbcPass);
		boolean hasData = dao.hasData(CommonFunc.query);
		if(hasData){
			log.info("DB has data.");
			setLabelBasedOnAssertion(rowNum,true);
			setRegistryKey(rowNum,String.valueOf(hasData));
		}else{
			log.info("DB has no data.");
			setLabelBasedOnAssertion(rowNum,false);
		}
		dao.closeConnection();
		return hasData;
	}

	public void getDBRowCount(int rowNum) throws Exception {
		DAO dao=new DAO(CommonFunc.jdbcUrl, CommonFunc.jdbcUser, CommonFunc.jdbcPass);
		int count = dao.getRowCount(CommonFunc.query);
		log.info("Total row got from DB ::"+count);
		setRegistryKey(rowNum,String.valueOf(count));
		dao.closeConnection();
	}

	public boolean dbColumnValueAssert(int rowNum) throws Exception {

		if(CommonFunc.dbResult!=null) {
			log.info("DBresult:: "+CommonFunc.dbResult);
			CommonFunc.selectionValue = CommonFunc.dbResult;
			CommonFunc.conditionApply = true;
			String assertVal = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_WITH);
			if(!TestHelper.isEmpty(assertVal) && assertVal.equalsIgnoreCase("null")){
				log.info("Expected value ::"+assertVal);
				assertVal = "";
			}
			if (!TestHelper.isEmpty(assertVal) && !TestHelper.isEmpty(CommonFunc.dbResult)) {
				if (assertVal.equalsIgnoreCase(CommonFunc.dbResult)) {
					log.info("Expected value has found.");
					CommonFunc.meetCondition = true;
					setRegistryKey(rowNum, CommonFunc.dbResult);
					return true;
				}
			}
		}
		log.info("Expected value has not found.");
		return false;
	}

	public void flowWait(int rowNum) throws Exception {
		CommonFunc.flowWait(driver, rowNum);
	}

	public void checkThenSetBuildingMethodRandomLengthString(Map<String, String> operations) throws Exception {
		int length = Constants.DEFAULT_RANDOM_STRING_LENGTH;
		if(operations.containsKey(Constants.LENGTH)){
			try{
				length = Integer.parseInt(operations.get(Constants.LENGTH));
			}catch(Exception ex){
				log.info("Random string LENGTH only except number.");
			}
		}
		if(length>0){
			operations.put(Constants.LENGTH, String.valueOf(length));
		}else {
			log.info("Random string LENGTH can not be less then 1. Set the default LENGTH::"+Constants.DEFAULT_RANDOM_STRING_LENGTH);
			operations.put(Constants.LENGTH, String.valueOf(Constants.DEFAULT_RANDOM_STRING_LENGTH));
		}
	}

	public void checkThenSetBuildingMethodRandomRangeNumber(Map<String, String> operations) throws Exception {
		if(!operations.containsKey(Constants.FROM)){
			log.info("Set the default FROM::"+Constants.DEFAULT_RANDOM_NUMBER_FROM);
			operations.put(Constants.FROM, String.valueOf(Constants.DEFAULT_RANDOM_NUMBER_FROM));
		}else{
			String from = operations.get(Constants.FROM);
			int fromNumber = 0;
			try{
				fromNumber = Integer.valueOf(from.trim());
			}catch(Exception ex){
				log.info("Random number FROM only except number.");
				fromNumber = Constants.DEFAULT_RANDOM_NUMBER_FROM;
			}
			if(fromNumber<=0){
				log.info("Set random number FROM::"+String.valueOf(fromNumber));
				operations.put(Constants.FROM, String.valueOf(fromNumber));
			}else{
				log.info("Random number FROM can not be less then 0. Set the default FROM::"+Constants.DEFAULT_RANDOM_NUMBER_FROM);
				operations.put(Constants.FROM, String.valueOf(Constants.DEFAULT_RANDOM_NUMBER_FROM));
			}
		}
		if(!operations.containsKey(Constants.TO)){
			log.info("Set the default TO::"+Constants.DEFAULT_RANDOM_NUMBER_TO);
			operations.put(Constants.TO, String.valueOf(Constants.DEFAULT_RANDOM_NUMBER_TO));
		}else{
			String to = operations.get(Constants.TO);
			int toNumber = 0;
			try{
				toNumber = Integer.valueOf(to.trim());
			}catch(Exception ex){
				log.info("Random number TO only except number.");
				toNumber= Constants.DEFAULT_RANDOM_NUMBER_TO;
			}
			if(toNumber<0){
				log.info("Set random number TO::"+String.valueOf(toNumber));
				operations.put(Constants.TO, String.valueOf(toNumber));
			}else{
				log.info("Random number TO can not be less then 0. Set the default TO::"+Constants.DEFAULT_RANDOM_NUMBER_TO);
				operations.put(Constants.TO, String.valueOf(Constants.DEFAULT_RANDOM_NUMBER_TO));
			}
		}
		if(Integer.parseInt(operations.get(Constants.TO))<Integer.parseInt(operations.get(Constants.FROM))){
			log.info("Random number FROM is greater then TO. So interchange");
			String interchange = operations.get(Constants.TO);
			operations.put(Constants.TO, operations.get(Constants.FROM));
			operations.put(Constants.FROM, interchange);
		}
	}
//switch frame portal
public void switchFramePortal(int rowNum) throws Exception {
	Map<String, String> locatorSelectionType = separateLocatorSelectionType(rowNum);
	WebElement switchframe = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
	driver.switchTo().frame("apex-frame");
	driver.findElement(By.xpath("//*[@id=\"card_number\"]")).sendKeys("“5200000000001005”\n");
	Thread.sleep(5000);
	driver.findElement(By.xpath("//*[@id=\"expiry_month\"]")).sendKeys("12");
	Thread.sleep(5000);
	driver.findElement(By.xpath("//*[@id=\"expiry_year\"]")).sendKeys("28");
	Thread.sleep(5000);
	driver.findElement(By.xpath("//*[@id=\"cvv\"]")).sendKeys("123");
	Thread.sleep(5000);
	driver.findElement(By.xpath("//*[@id=\"hostedPaymentsubmitBtn\"]")).click();
	Thread.sleep(30000);
	String windowHandle = driver.getWindowHandle(); // save the original window handle
	driver.switchTo().window(windowHandle); // handle the pop up
	log.info("switching to default");
//	WebElement getokText=driver.findElement(By.xpath("//button[@class='ui primary button okButton']"));
//	log.info(getokText.getText());
//	getokText.click();
	Thread.sleep(10000);
	//log.info("click button successfully");
}


	//Modify Date

	public void modifyDate(int rowNum) throws Exception {
		String actionString = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ELEMENT_LOCATOR);
		String value = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		log.info("\nGet the value ::" + value + "\n");
		Calendar cal = Calendar.getInstance();

		String[] dateFormatSymbol = new String[]{"yyyy-MM-dd HH:mm:ss.SSS","dd-MMM-yyyy HH:MM:SS", "dd-MMM-yyyy", "MM/dd/yy", "MMMM", "MMM", "MM", "MM/dd", "d", "dd", "ddd", "yyyy", "yy", "dd/MM", "dd/MM/yy", "MMM.dd,yyyy HH:MM:SS", "MMM.dd,yyyy", "MM/dd/yyyy", "dd/MM/yyyy", "yy/MM/dd", "yyyy/MM/dd", "MMMyyyy", "yyyy-MM-dd", "yyyy-MM-dd HH:MM:SS"};
		List<String> list = Arrays.asList(dateFormatSymbol);

		if(list.contains(actionString)){
			if (value != null) {
				if (value.contains("m")) {
					String numberOfMonths[] = value.split("=");

					cal.add(Calendar.MONTH, Integer.parseInt(numberOfMonths[1]));
				}
				else {
					cal.add(Calendar.DATE, Integer.parseInt(value));
				}
				value = new SimpleDateFormat(actionString).format(cal.getTime());
			} else {
				value = new SimpleDateFormat(actionString).format(cal.getTime());
			}
		}
		else{
			log.info("Invalid Date Format");
		}
		log.info("Date: "+value);
		setRegistryKey(rowNum, value);
	}


	public void prepareForBuildinMethord(int rowNum) throws Exception {
		String actionString = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ELEMENT_LOCATOR);
		String value = "";
		String operators[] = actionString.split("\\|\\|");

		Map<String,String> operations = null;
		for(String operatorStr: operators){
			operations = new HashMap<>();
			String operatorName[] = operatorStr.split(",");
			if(operatorName.length<0){
				continue;
			}
			if(operatorName[0].trim().equalsIgnoreCase(Constants.REPLACE)) {
				value = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
				if(!TestHelper.isEmpty(value)) {
					String options[] = operatorStr.split(",", 3);
					for (String option : options) {
						String optionSplit[] = option.split("=", 2);
						if (optionSplit.length == 2 && !TestHelper.isEmpty(optionSplit[0])) {
							operations.put(optionSplit[0].toLowerCase().trim(), optionSplit[1]);
						}
					}
					if (operations.containsKey(Constants.REPLACING) && operations.containsKey(Constants.REPLACEBY)) {
						operations.put(Constants.VALUE, value);
						value = CommonFunc.buildInMethod(Constants.REPLACE, operations);
					}
				}
			}else if(operatorName[0].trim().equalsIgnoreCase(Constants.CONCAT)) {
				String options[] = operatorStr.split(",");
				int count = 0;
				boolean skippedFirst = false;
				for(String option: options) {
					if(!skippedFirst){
						skippedFirst = true;
						continue;
					}
					String optionSplit[] = option.split("=", 2);
					if (optionSplit.length == 2 && !TestHelper.isEmpty(optionSplit[0])) {
						operations.put(optionSplit[0].toLowerCase().trim(), optionSplit[1]);
					}else if(optionSplit.length==1){
						operations.put(String.valueOf(count), optionSplit[0]);
						count++;
					}
				}
				if(operations.size()>1){
					value = CommonFunc.buildInMethod(Constants.CONCAT,operations);
				}else if(operations.size()==1){
					value = operations.entrySet().iterator().next().getValue();
				}
				break;
			}else if(operatorName[0].trim().equalsIgnoreCase(Constants.RANDOM)) {
				String options[] = operatorStr.split(",",4);
				for(String option: options) {
					String optionSplit[] = option.split("=", 2);
					if (optionSplit.length == 2 && !TestHelper.isEmpty(optionSplit[0])) {
						operations.put(optionSplit[0].toLowerCase().trim(), optionSplit[1]);
					}
				}
				if(operations.containsKey(Constants.TYPE)){
					if(operations.get(Constants.TYPE).equalsIgnoreCase(Constants.STRING)||operations.get(Constants.TYPE).equalsIgnoreCase(Constants.TEXT)){
						operations.put(Constants.TYPE, Constants.STRING);
						checkThenSetBuildingMethodRandomLengthString(operations);
					}
					if(operations.get(Constants.TYPE).equalsIgnoreCase(Constants.NUMBER)||operations.get(Constants.TYPE).equalsIgnoreCase(Constants.INT)||operations.get(Constants.TYPE).equalsIgnoreCase(Constants.INTEGER)){
						operations.put(Constants.TYPE, Constants.NUMBER);
						checkThenSetBuildingMethodRandomRangeNumber(operations);
					}
				}else {
					operations.put(Constants.TYPE, Constants.NUMBER);
					checkThenSetBuildingMethodRandomRangeNumber(operations);
				}
				value = CommonFunc.buildInMethod(Constants.RANDOM,operations);
				break;
			}else if(operatorName[0].trim().equalsIgnoreCase(Constants.INCREMENT)) {
				value = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN).trim();
				int valueType = 0;
				int valueInt = 0;
				double valueDouble = 0.0;
				String dateFormat = "";
				Date valueDate = null;
				try{
					valueInt = Integer.valueOf(value);
					valueType = 0;
				}catch(Exception ex){
					try{
						valueDouble = Double.valueOf(value);
						valueType = 1;
					}catch(Exception ex2){
						try {

							if (operations.containsKey(Constants.FORMAT)) {
								dateFormat = operations.get(Constants.FORMAT).trim();
							}
							if (TestHelper.isEmpty(dateFormat)) {
								dateFormat = Constants.DEFAULT_REPORT_DATE_FORMAT;
							}
							valueDate = new SimpleDateFormat(dateFormat).parse(value);

							operations.put(Constants.FORMAT,dateFormat);

							if (!operations.containsKey(Constants.INCREMENT_TYPE)) {
								operations.put(Constants.INCREMENT_TYPE,Constants.DAY);
							}
							valueType = 2;
						}catch(Exception ex3) {
							throw new Exception("Increment value only except a number and valid date.");
						}
					}
				}
				operations.put(Constants.VALUE_TYPE,String.valueOf(valueType));
				if(valueType == 0){
					operations.put(Constants.VALUE, String.valueOf(valueInt));
				}else if(valueType == 1){
					operations.put(Constants.VALUE, String.valueOf(valueDouble));
				}else if(valueType == 2){
					operations.put(Constants.VALUE, new SimpleDateFormat(dateFormat).format(valueDate));
				}

				String options[] = operatorStr.split(",",3);
				for(String option: options) {
					String optionSplit[] = option.split("=", 2);
					if (optionSplit.length == 2 && !TestHelper.isEmpty(optionSplit[0])) {
						operations.put(optionSplit[0].toLowerCase().trim(), optionSplit[1]);
					}
				}
				if(operations.containsKey(Constants.INCREMENT_BY)){
					boolean isIntForBy = false;
					valueInt = 0;
					valueDouble = 0.0;
					String incrementBy = operations.get(Constants.INCREMENT_BY).trim();
					try{
						valueInt = Integer.valueOf(incrementBy);
						isIntForBy = true;
					}catch(Exception ex){
						try{
							valueDouble = Double.valueOf(incrementBy);
							isIntForBy = false;
						}catch(Exception ex2){
							throw new Exception("Increment by only except a number.");
						}
					}
					if(isIntForBy){
						operations.put(Constants.INCREMENT_BY, String.valueOf(valueInt));
					}else{
						operations.put(Constants.INCREMENT_BY, String.valueOf(valueDouble));
					}
				}else {
					operations.put(Constants.INCREMENT_BY, String.valueOf(Constants.DEFAULT_INCREMENT_BY));
				}
				value = CommonFunc.buildInMethod(Constants.INCREMENT,operations);
				valueDouble = Double.valueOf(value);
				if(valueType==0) {
					valueInt = (int) valueDouble;
					value = String.valueOf(valueInt);
				}
				log.info("Incremented value::"+value);
				break;
			}else if(operatorName[0].trim().equalsIgnoreCase(Constants.DECREMENT)) {
				value = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN).trim();
				int valueType = 0;
				int valueInt = 0;
				double valueDouble = 0.0;
				String dateFormat = "";
				Date valueDate = null;

				String options[] = operatorStr.split(",",3);
				for(String option: options) {
					String optionSplit[] = option.split("=", 2);
					if (optionSplit.length == 2 && !TestHelper.isEmpty(optionSplit[0])) {
						operations.put(optionSplit[0].toLowerCase().trim(), optionSplit[1]);
					}
				}

				try{
					valueInt = Integer.valueOf(value);
					valueType = 0;
				}catch(Exception ex){
					try{
						valueDouble = Double.valueOf(value);
						valueType = 1;
					}catch(Exception ex2){
						try {

							if (operations.containsKey(Constants.FORMAT)) {
								dateFormat = operations.get(Constants.FORMAT).trim();
							}
							if (TestHelper.isEmpty(dateFormat)) {
								dateFormat = Constants.DEFAULT_REPORT_DATE_FORMAT;
							}
							valueDate = new SimpleDateFormat(dateFormat).parse(value);

							operations.put(Constants.FORMAT,dateFormat);

							if (!operations.containsKey(Constants.DECREMENT_TYPE)) {
								operations.put(Constants.DECREMENT_TYPE,Constants.DAY);
							}
							valueType = 2;
						}catch(Exception ex3) {
							throw new Exception("Increment value only except a number or date.");
						}
					}
				}

				if(valueType == 0){
					operations.put(Constants.VALUE, String.valueOf(valueInt));
				}else if(valueType == 1){
					operations.put(Constants.VALUE, String.valueOf(valueDouble));
				}else if(valueType == 2){
					operations.put(Constants.VALUE, new SimpleDateFormat(dateFormat).format(valueDate));
				}

				if(operations.containsKey(Constants.DECREMENT_BY)){
					boolean isIntForBy = false;
					valueInt = 0;
					valueDouble = 0.0;
					String decrementBy = operations.get(Constants.DECREMENT_BY).trim();
					try{
						valueInt = Integer.valueOf(decrementBy);
						isIntForBy = true;
					}catch(Exception ex){
						try{
							valueDouble = Double.valueOf(decrementBy);
							isIntForBy = false;
						}catch(Exception ex2){
							throw new Exception("Decrement by only except a number.");
						}
					}
					if(isIntForBy){
						operations.put(Constants.DECREMENT_BY, String.valueOf(valueInt));
					}else{
						operations.put(Constants.DECREMENT_BY, String.valueOf(valueDouble));
					}
				}else {
					operations.put(Constants.DECREMENT_BY, String.valueOf(Constants.DEFAULT_DECREMENT_BY));
				}
				value = CommonFunc.buildInMethod(Constants.DECREMENT,operations);
				valueDouble = Double.valueOf(value);
				if(valueType==0) {
					valueInt = (int) valueDouble;
					value = String.valueOf(valueInt);
				}
				log.info("Decremented value::"+value);
				break;
			}else if(operatorName[0].trim().equalsIgnoreCase(Constants.COMPARE)) {
				String options[] = operatorStr.split(",",4);
				int count = 0;
				boolean skippedFirst = false;
				for(String option: options) {
					if(!skippedFirst){
						skippedFirst = true;
						continue;
					}
					String optionSplit[] = option.split("=", 2);
					if (optionSplit.length == 2 && !TestHelper.isEmpty(optionSplit[0])) {
						if(!optionSplit[0].trim().equalsIgnoreCase(Constants.TYPE)&&!optionSplit[0].trim().equalsIgnoreCase(Constants.FORMAT)) {
							operations.put(String.valueOf(count), optionSplit[1]);
							count++;
						}else if(optionSplit[0].trim().equalsIgnoreCase(Constants.TYPE)) {
							operations.put(Constants.TYPE, optionSplit[1]);
						}else if(optionSplit[0].trim().equalsIgnoreCase(Constants.FORMAT)) {
							operations.put(Constants.FORMAT, optionSplit[1]);
						}
					}else if(optionSplit.length==1){
						operations.put(String.valueOf(count), optionSplit[0]);
						count++;
					}
				}
				if(!operations.containsKey(Constants.TYPE)){
					log.info("Compare type is not defined. So set compare type as equal.");
					operations.put(Constants.TYPE, Constants.EQUAL);
				}
				value = CommonFunc.buildInMethod(Constants.COMPARE,operations);
				if(TestHelper.isEmpty(value)){
					value = Constants.FALSE;
				}
				log.info("Compare Result::"+value);
				break;
			}else if(operatorName[0].trim().equalsIgnoreCase(Constants.GET_DATE)) {
				value = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN).trim();
				String options[] = operatorStr.split(",",3);
				String dateFormat = "";
				Date valueDate = null;

				for(String option: options) {
					String optionSplit[] = option.split("=", 2);
					if (optionSplit.length == 2 && !TestHelper.isEmpty(optionSplit[0])) {
						operations.put(optionSplit[0].toLowerCase().trim(), optionSplit[1]);
					}
				}
				if (operations.containsKey(Constants.FORMAT)) {
					dateFormat = operations.get(Constants.FORMAT).trim();
				}
				if (TestHelper.isEmpty(dateFormat)) {
					dateFormat = Constants.DEFAULT_REPORT_DATE_FORMAT;
				}
				operations.put(Constants.FORMAT, dateFormat);

				if (!operations.containsKey(Constants.DECREMENT_TYPE)) {
					operations.put(Constants.DECREMENT_TYPE, Constants.DAY);
				}
				try {
					if(!TestHelper.isEmpty(value)) {
						valueDate = new SimpleDateFormat(dateFormat).parse(value);
					}else{
						valueDate = new Date();
					}
				}catch(Exception ex3) {
					throw new Exception("Gate date value only except a date.");
				}
				operations.put(Constants.VALUE, new SimpleDateFormat(dateFormat).format(valueDate));
				value = CommonFunc.buildInMethod(Constants.GET_DATE,operations);
				log.info("Got the date::"+value);
				break;
			}else if(operatorName[0].trim().equalsIgnoreCase(Constants.STRING_CHECK)) {
				value = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN).trim();
				String options[] = operatorStr.split(",",3);

				for(String option: options) {
					String optionSplit[] = option.split("=", 2);
					if (optionSplit.length == 2 && !TestHelper.isEmpty(optionSplit[0])) {
						operations.put(optionSplit[0].toLowerCase().trim(), optionSplit[1]);
					}
				}
				if (!operations.containsKey(Constants.TYPE)) {
					log.info("String check no type given so set default check type as::"+Constants.CONTAINS);
					operations.put(Constants.TYPE, Constants.CONTAINS);
				}

				if(!operations.containsKey(Constants.SUB_STRING) || (operations.get(Constants.TYPE).equalsIgnoreCase(Constants.CONTAINS) && !operations.containsKey(Constants.REGEX))){
					value = Constants.FALSE;
					break;
				}
				operations.put(Constants.VALUE, value);
				value = CommonFunc.buildInMethod(Constants.STRING_CHECK,operations);
				log.info("String check result::"+value);
				break;
			}else if(operatorName[0].trim().equalsIgnoreCase(Constants.JSON_CONVERT)){
				value = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN).trim();
				System.out.println("------------------ >>>>> value ::"+value);
				String assertWith = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.ASSERTION_WITH);
				String options[] = operatorStr.split(",");

				if(options[1].trim().equalsIgnoreCase(Constants.ASSERTION)){
					if(assertWith != null && !TestHelper.isEmpty(value) ){
						JSONObject assertWihJSON= new JSONObject(assertWith);
						JSONObject convertedJSON = new JSONObject(value);
						restApiCall.init();
						value = restApiCall.jsonCompare(convertedJSON, assertWihJSON) ? "true" : "false";
					}else {
						value = "false";
					}
				}
				else if(options[1].trim().contains("key")){
					String jsonKey  = options[1].trim().split("=")[1];
					String keys[] = jsonKey.split("\\.");
					for (String key: keys) {
						int index = -111;
						if (key.contains("[") && key.contains("]")) {
							index = keyIndex(key);
							key = getKey(key);
						}
						value = getProperty(new JSONObject(value), key, index);

						log.info("returned json string:: " + value);
					}
				}
			}
		}
		setRegistryKey(rowNum,value);

	}
	public int keyIndex(String key){

		Pattern pattern = Pattern.compile("\\[(.*)\\]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(key);
		if (matcher.find()){
			log.info("index::  "+ matcher.group(1));
			return Integer.parseInt(matcher.group(1));
		}
		return -111;
	}
	public String getKey(String key){

		Pattern pattern = Pattern.compile("(.*)\\[(.*)\\]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(key);
		if (matcher.find()){
			log.info("jsonKey ::  "+ matcher.group(1));
			return matcher.group(1);
		}
		return null;
	}

	public  String getProperty(JSONObject js1, String jsonKey, int index) throws JSONException {
		Iterator iterator = js1.keys();
		String key = null;
		while (iterator.hasNext()) {
			key = (String) iterator.next();
			if ((key.equals(jsonKey)) && index == -111) {
				return js1.getString(key);
			}
			else  if((key.equals(jsonKey)) && index != -111){
				return js1.getJSONArray(key).get(index).toString();

			}
		}

		return null;
	}

	/*public void setFile(int rowNum) throws Exception {
		Map<String,String> locatorSelectionType = separateLocatorSelectionType(rowNum);
		CommonFunc.waitForPageLoad(driver, rowNum);
		String text = CommonFunc.getExcelDataVariableReplaced(rowNum, ExcelUtils.VALUE_COLUMN);
		WebElement selectElement = CommonFunc.getPageElementWithWait(driver, rowNum, locatorSelectionType.get("selectorType"), locatorSelectionType.get("locator"));
		if(selectElement!=null){
			selectElement.clear();
			if(!TestHelper.isEmpty(text)) {
				selectElement.sendKeys(text);
			}
			log.info("Set text value::"+text);
			setRegistryKey(rowNum,text.trim());
		} else {
			throw new Exception("No Such Element Found.");
		}
	}*/
}
