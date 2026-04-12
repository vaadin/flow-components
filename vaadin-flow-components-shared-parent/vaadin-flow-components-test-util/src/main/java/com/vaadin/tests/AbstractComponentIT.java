/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tests;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.flow.testutil.net.PortProber;
import com.vaadin.testbench.ScreenshotOnFailureRule;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchTestCase;

/**
 * Base class for component integration tests.
 * <p>
 * Extends {@link TestBenchTestCase} directly and manages a local headless
 * Chrome driver that is reused across all test methods in the same test class.
 * <p>
 * Test classes must be annotated with {@link TestPath} to specify the URL path
 * of the test view.
 */
public abstract class AbstractComponentIT extends TestBenchTestCase {

    private static final Logger logger = LoggerFactory
            .getLogger(AbstractComponentIT.class);

    private static WebDriver sharedDriver;

    @Rule
    public ScreenshotOnFailureRule screenshotOnFailure = new ScreenshotOnFailureRule(
            this, false);

    @Before
    public void setup() throws Exception {
        if (sharedDriver != null && isDriverAlive(sharedDriver)) {
            setDriver(sharedDriver);
            getDriver().manage().deleteAllCookies();
            getDriver().navigate().to("about:blank");
        } else {
            if (sharedDriver != null) {
                tryQuitDriver(sharedDriver);
                sharedDriver = null;
            }
            setDriver(createHeadlessChromeDriver());
            sharedDriver = getDriver();
            testBench().resizeViewPortTo(1024, 800);
        }
    }

    @AfterClass
    public static void quitSharedDriver() {
        if (sharedDriver != null) {
            tryQuitDriver(sharedDriver);
            sharedDriver = null;
        }
    }

    // ----- Test path and URL resolution -----

    protected String getTestPath() {
        TestPath annotation = getClass().getAnnotation(TestPath.class);
        if (annotation == null) {
            throw new IllegalStateException(
                    "The test class should be annotated with @TestPath");
        }
        String path = annotation.value();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    protected String getRootURL() {
        return "http://localhost:8080";
    }

    protected String getTestURL(String... parameters) {
        return getTestURL(getRootURL(), getTestPath(), parameters);
    }

    public static String getTestURL(String rootUrl, String testPath,
            String... parameters) {
        while (rootUrl.endsWith("/")) {
            rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
        }
        rootUrl = rootUrl + testPath;

        if (parameters != null && parameters.length != 0) {
            if (!rootUrl.contains("?")) {
                rootUrl += "?";
            } else {
                rootUrl += "&";
            }
            rootUrl += Arrays.stream(parameters)
                    .collect(Collectors.joining("&"));
        }

        return rootUrl;
    }

    protected void open() {
        open((String[]) null);
    }

    protected void open(String... parameters) {
        String url = getTestURL(parameters);
        getDriver().get(url);
        waitForDevServer();
    }

    // ----- Driver management -----

    private static boolean isDriverAlive(WebDriver driver) {
        try {
            WebDriver realDriver = driver;
            while (realDriver instanceof WrapsDriver) {
                realDriver = ((WrapsDriver) realDriver).getWrappedDriver();
            }
            if (realDriver instanceof RemoteWebDriver) {
                return ((RemoteWebDriver) realDriver).getSessionId() != null;
            }
            driver.getTitle();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void tryQuitDriver(WebDriver driver) {
        try {
            driver.quit();
        } catch (Exception e) {
            // Ignore - driver may already be dead
        }
    }

    private static WebDriver createHeadlessChromeDriver() {
        for (int i = 0; i < 3; i++) {
            try {
                return tryCreateHeadlessChromeDriver();
            } catch (Exception e) {
                logger.warn("Unable to create chromedriver on attempt " + i, e);
            }
        }
        throw new RuntimeException(
                "Gave up trying to create a chromedriver instance");
    }

    private static WebDriver tryCreateHeadlessChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--disable-gpu",
                "--disable-backgrounding-occluded-windows");

        String extraArgs = System.getenv("TESTBENCH_CHROME_EXTRA_ARGS");
        if (extraArgs != null && !extraArgs.isBlank()) {
            options.addArguments(extraArgs.split("\\s+"));
        }

        int port = PortProber.findFreePort();
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingPort(port).withSilent(true).build();
        ChromeDriver chromeDriver = new ChromeDriver(service, options);
        return TestBench.createDriver(chromeDriver);
    }

    // =========================================================
    // Helper methods duplicated from flow test-util base classes
    // =========================================================

    /**
     * Waits up to 10s for the given condition to become false.
     */
    protected <T> void waitUntilNot(ExpectedCondition<T> condition) {
        waitUntilNot(condition, 10);
    }

    /**
     * Waits the given number of seconds for the given condition to become
     * false.
     */
    protected <T> void waitUntilNot(ExpectedCondition<T> condition,
            long timeoutInSeconds) {
        waitUntil(ExpectedConditions.not(condition), timeoutInSeconds);
    }

    /**
     * Returns true if an element can be found from the driver with given
     * selector.
     */
    public boolean isElementPresent(By by) {
        try {
            WebElement element = getDriver().findElement(by);
            return element != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clicks on the element, using JS. This method is more convenient than
     * Selenium {@code findElement(By.id(urlId)).click()}, because Selenium
     * method changes scroll position, which is not always needed.
     */
    protected void clickElementWithJs(String elementId) {
        executeScript(String.format("document.getElementById('%s').click();",
                elementId));
    }

    /**
     * Clicks on the element, using JS.
     */
    protected void clickElementWithJs(WebElement element) {
        executeScript("arguments[0].click();", element);
    }

    protected void waitForElementPresent(final By by) {
        waitUntil(ExpectedConditions.presenceOfElementLocated(by));
    }

    protected void waitForElementNotPresent(final By by) {
        waitUntil(input -> input.findElements(by).isEmpty());
    }

    protected void waitForElementVisible(final By by) {
        waitUntil(ExpectedConditions.visibilityOfElementLocated(by));
    }

    /**
     * Scrolls the page to the element given using javascript.
     */
    protected void scrollToElement(WebElement element) {
        Objects.requireNonNull(element,
                "The element to scroll to should not be null");
        getCommandExecutor().executeScript("arguments[0].scrollIntoView(true);",
                element);
    }

    /**
     * Scrolls the page to the element specified and clicks it.
     */
    protected void scrollIntoViewAndClick(WebElement element) {
        scrollToElement(element);
        element.click();
    }

    /**
     * Gets the log entries from the browser that have the given logging level
     * or higher.
     */
    protected List<LogEntry> getLogEntries(Level level) {
        getCommandExecutor().waitForVaadin();

        return driver.manage().logs().get(LogType.BROWSER).getAll().stream()
                .filter(logEntry -> logEntry.getLevel().intValue() >= level
                        .intValue())
                .filter(logEntry -> !logEntry.getMessage()
                        .contains("favicon.ico"))
                .collect(Collectors.toList());
    }

    private static final String WEB_SOCKET_CONNECTION_ERROR_PREFIX = "WebSocket connection to ";

    /**
     * Checks browser's log entries, throws an error for any client-side error
     * and logs any client-side warnings.
     */
    protected void checkLogsForErrors(
            Predicate<String> acceptableMessagePredicate) {
        getLogEntries(Level.WARNING).forEach(logEntry -> {
            if (logEntry.getMessage().contains(
                    "Lit is in dev mode. Not recommended for production")) {
                return;
            }
            if ((Objects.equals(logEntry.getLevel(), Level.SEVERE)
                    || logEntry.getMessage().contains(" 404 "))
                    && !logEntry.getMessage()
                            .contains(WEB_SOCKET_CONNECTION_ERROR_PREFIX)
                    && !acceptableMessagePredicate
                            .test(logEntry.getMessage())) {
                throw new AssertionError(String
                        .format("Error message in browser log: %s", logEntry));
            } else {
                LoggerFactory.getLogger(AbstractComponentIT.class.getName())
                        .warn("This message in browser log console may be a potential error: '{}'",
                                logEntry);
            }
        });
    }

    /**
     * Checks browser's log entries, throws an error for any client-side error
     * and logs any client-side warnings.
     */
    protected void checkLogsForErrors() {
        checkLogsForErrors(msg -> false);
    }

    /**
     * If dev server start in progress wait until it's started. Otherwise return
     * immediately.
     */
    protected void waitForDevServer() {
        Object result;
        do {
            getCommandExecutor().waitForVaadin();
            result = getCommandExecutor().executeScript(
                    "return window.Vaadin && window.Vaadin.Flow && window.Vaadin.Flow.devServerIsNotLoaded;");
        } while (Boolean.TRUE.equals(result));
    }

    /**
     * Calls the {@code blur()} function on the current active element of the
     * page, if any.
     */
    public void blur() {
        executeScript(
                "!!document.activeElement ? document.activeElement.blur() : 0");
    }

    /**
     * Gets a property value from a web element using JavaScript.
     */
    public String getProperty(WebElement element, String propertyName) {
        Object result = executeScript(
                "return arguments[0]." + propertyName + ";", element);
        return result == null ? null : String.valueOf(result);
    }

}
