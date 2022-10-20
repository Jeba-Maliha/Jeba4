package com.dsinnovators.keyword.driven.factory;

import com.dsinnovators.keyword.driven.helper.PrefixedProperties;
import com.dsinnovators.keyword.driven.utils.FileUtil;
import com.dsinnovators.keyword.driven.utils.OsUtil;
import com.dsinnovators.keyword.driven.utils.TestHelper;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.io.File;
import java.util.Properties;


public class WebDriverFactory {

    public WebDriver getWebDriver(String browser) throws Exception {
        WebDriver webDriver;
        switch (browser.toLowerCase()) {
            case "chrome":
                webDriver = chromeDriver();
                break;
            case "chrome-portable":
                webDriver = chromePortableDriver();
                break;
            case "firefox":
                webDriver = firefoxWebDriver();
                break;
            case "ie":
                webDriver = ieWebDriver();
                break;
            case "firefox-portable":
                webDriver = firefoxPortableWebDriver();
                break;
            case "firefoxwithgecko":
                webDriver = firefoxWithGeckoWebDriver();
                break;
            case "htmlunit":
                webDriver = new HtmlUnitDriver(true);
                break;
            case "phantomjs":
                webDriver = phantomJsWebDriver();
                break;
            default:
                throw new IllegalArgumentException("unsupported browser name");
        }
        return webDriver;
    }

    private WebDriver phantomJsWebDriver() throws Exception {
        File file = OsUtil.isWindows() ?
            new File(TestHelper.getTestConfPropertyValue("phantomdriver")) :
            new File(TestHelper.getTestConfPropertyValue("phantomdriver_linux"));

        FileUtil.makeFileExecutable(file);
        System.setProperty("phantomjs.binary.path", file.getAbsolutePath());
        return new PhantomJSDriver();
    }

    private WebDriver firefoxWithGeckoWebDriver() throws Exception {
        FirefoxProfile profile = defaultFirefoxProfiler();

        File file = OsUtil.isWindows() ?
            new File(TestHelper.getTestConfPropertyValue("geckodriver")):
            new File(TestHelper.getTestConfPropertyValue("geckodriver_linux"));

        FileUtil.makeFileExecutable(file);

        System.setProperty("webdriver.gecko.driver", file.getAbsolutePath());

        File fireFoxPortable = OsUtil.isWindows() ?
            new File(TestHelper.getTestConfPropertyValue("firefox.portable.windows")) :
            new File(TestHelper.getTestConfPropertyValue("firefox.portable.linux"));

        FileUtil.makeFileExecutable(fireFoxPortable);

        return new FirefoxDriver(new FirefoxBinary(fireFoxPortable), profile);
    }

    private WebDriver firefoxPortableWebDriver() throws Exception {
        FirefoxProfile profile = defaultFirefoxProfiler();
        File fireFoxPortable = OsUtil.isWindows() ?
            new File(TestHelper.getTestConfPropertyValue("firefox.portable.windows")) :
            new File(TestHelper.getTestConfPropertyValue("firefox.portable.linux"));

        FileUtil.makeFileExecutable(fireFoxPortable);

        return new FirefoxDriver(new FirefoxBinary(fireFoxPortable), profile);
    }

    private WebDriver ieWebDriver() throws Exception {
        if(OsUtil.isWindows()) {
            File file = new File(TestHelper.getTestConfPropertyValue("chromedriver"));
            FileUtil.makeFileExecutable(file);
            System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
        } else {
            throw new Exception("Driver not found.");
        }
        return new InternetExplorerDriver();
    }

    private WebDriver firefoxWebDriver() throws Exception {
        FirefoxDriverManager.getInstance().setup();
        FirefoxProfile profile = defaultFirefoxProfiler();
        return new FirefoxDriver(profile);
    }

    private FirefoxProfile defaultFirefoxProfiler() {
        FirefoxProfile profile = new FirefoxProfile();
        Properties properties = new PrefixedProperties(TestHelper.getProperty(),"firefox");
        for(String property : properties.stringPropertyNames()){
            System.out.println("Firefox Profile Property Name: "+property);
            System.out.println("Firefox Profile Property Value: "+properties.stringPropertyNames());
            if(TestHelper.isNumeric(properties.getProperty(property))){
                profile.setPreference(property,Integer.parseInt(properties.getProperty(property)));
            } else if(properties.getProperty(property).equalsIgnoreCase("true")){
                profile.setPreference(property,true);
            } else if(properties.getProperty(property).equalsIgnoreCase("false")){
                profile.setPreference(property,false);
            }else{
                profile.setPreference(property,properties.getProperty(property));
            }
        }
        return profile;
    }

    private WebDriver chromePortableDriver() throws Exception {
        ChromeOptions options = defaultChromeOptions();
        File chromePortable = OsUtil.isWindows() ?
            new File(TestHelper.getTestConfPropertyValue("chrome.portable.windows")) :
            new File(TestHelper.getTestConfPropertyValue("chrome.portable.linux"));

        FileUtil.makeFileExecutable(chromePortable);

        options.setBinary(chromePortable.getAbsolutePath());
        return new ChromeDriver(options);
    }

    private WebDriver chromeDriver() throws Exception {
        ChromeOptions options = defaultChromeOptions();

        return new ChromeDriver(options);
    }

    private ChromeOptions defaultChromeOptions() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        File file = OsUtil.isWindows() ?
            new File(TestHelper.getTestConfPropertyValue("chromedriver")) :
            new File(TestHelper.getTestConfPropertyValue("chromedriver_linux"));

        FileUtil.makeFileExecutable(file);
        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
        return options;
    }
}
