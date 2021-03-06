package com.harishkannarao.angularjs.webapp.producer;

import com.harishkannarao.angularjs.webapp.util.PropertiesReference;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class SingletonWebDriver {

    private static synchronized WebDriver   createAndCacheWebDriver() {
        class WebDriverFactory implements Supplier<WebDriver> {
            private final WebDriver webDriverInstance;

            private static final String DRIVER_KEY = "driver";
            private static final String FIREFOX_DRIVER = "firefox";
            private static final String PHANTOMJS_DRIVER = "phantomjs";

            public WebDriverFactory() {
                webDriverInstance = createWebDriver();
                Runtime.getRuntime().addShutdownHook(new Thread("WebDriver shutdown thread") {
                    public void run() {
                        webDriverInstance.quit();
                    }
                });
            }

            private WebDriver createWebDriver() {
                WebDriver driver = null;
                String driverType = PropertiesReference.MAVEN_PROPERTIES.getProperty(DRIVER_KEY);
                if (FIREFOX_DRIVER.equals(driverType)) {
                    driver = new FirefoxDriver();
                    driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
                } else if (PHANTOMJS_DRIVER.equals(driverType)) {
                    DesiredCapabilities phantomjsCapabilities = DesiredCapabilities.phantomjs();
                    String [] phantomJsArgs = {"--ignore-ssl-errors=true","--ssl-protocol=any"};
                    phantomjsCapabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomJsArgs);
                    driver = new PhantomJSDriver(phantomjsCapabilities);
                    driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
                }
                return driver;
            }

            @Override
            public WebDriver get() {
                return webDriverInstance;
            }
        }

        if(!WebDriverFactory.class.isInstance(webDriverSupplier)) {
            webDriverSupplier = new WebDriverFactory();
        }
        return webDriverSupplier.get();
    }

    private static Supplier<WebDriver> webDriverSupplier = SingletonWebDriver::createAndCacheWebDriver;

    public static WebDriver getInstance() {
        return webDriverSupplier.get();
    }

}
