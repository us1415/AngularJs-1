package com.harishkannarao.angularjs.webapp.producer;

import org.openqa.selenium.WebDriver;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class WebDriverProducer {

    @Produces
    @ApplicationScoped
    public WebDriver getWebDriver() {
        return SingletonWebDriver.getInstance();
    }
}
