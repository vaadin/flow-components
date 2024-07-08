/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component;

import java.net.URL;
import java.util.Optional;

import com.vaadin.flow.testutil.LocalExecution;
import com.vaadin.testbench.parallel.Browser;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.testbench.TestBench;

/**
 * Temp class for disabling the w3c communication mode on remote chrome.
 */
public class AbstractNoW3c extends AbstractComponentIT {

    @Override
    public void setup() throws Exception {
        final WebDriver webDriver = createChromeDriverWithoutW3c(
                getLocalExecution(), getDesiredCapabilities(), getHubURL());
        if (webDriver != null) {
            setDesiredCapabilities(getDesiredCapabilities());
            setDriver(webDriver);
        } else {
            super.setup();
        }
    }

    public static WebDriver createChromeDriverWithoutW3c(
            Optional<LocalExecution> localExecution,
            DesiredCapabilities capabilities, String hubURL) throws Exception {

        final ChromeOptions options = createChromeOptions();
        options.merge(capabilities);

        if (!localExecution.isPresent()) {
            return TestBench.createDriver(
                    new RemoteWebDriver(new URL(hubURL), options));
        } else if (localExecution.get().value() == Browser.CHROME) {
            return TestBench.createDriver(new ChromeDriver(options));
        }
        return null;
    }

    private static ChromeOptions createChromeOptions() {
        final ChromeOptions options = new ChromeOptions();
        options.addArguments(new String[] { "--headless", "--disable-gpu" });
        options.setExperimentalOption("w3c", false);
        return options;
    }
}
