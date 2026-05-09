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

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.codec.w3c.W3CHttpCommandCodec;
import org.openqa.selenium.remote.codec.w3c.W3CHttpResponseCodec;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.flow.testutil.net.PortProber;
import com.vaadin.testbench.Parameters;
import com.vaadin.testbench.ScreenshotOnFailureRule;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchTestCase;

/**
 * Base class for Flow component integration tests.
 * <p>
 * Four independent opt-in features, each enabled by a system property:
 * <ul>
 * <li>{@code -Dtest.headed=true} — run Chrome in headed (visible) mode. Default
 * is headless. Also implied when a JDWP debug agent is attached.</li>
 * <li>{@code -Dtest.reuseDriver=true} — one Chrome window per test
 * <em>class</em>. The driver is created in {@code @BeforeClass} and destroyed
 * in {@code @AfterClass}; between methods cookies are cleared and the browser
 * navigates to {@code about:blank}. Default: one window per test
 * <em>method</em>.</li>
 * <li>{@code -Dtest.shareDriver=true} — one Chrome window for the entire JVM
 * fork (all test classes). Implies {@code test.reuseDriver}. A health-check
 * guards against stale sessions. In debug (JDWP) mode the session is also
 * persisted to a temp file so the same browser can be reconnected on subsequent
 * runs.</li>
 * <li>{@code -Dtest.useSpa=true} — when combined with {@code test.reuseDriver}
 * or {@code test.shareDriver}, navigate between routes via the Vaadin
 * {@code vaadin-navigate} client event instead of a full page load. Falls back
 * to a full load automatically when the browser is not on a Vaadin page or when
 * navigating to the same route (which would leak session state).</li>
 * </ul>
 * Subclasses can opt out of driver reuse on a per-class basis by overriding
 * {@link #isReuseDriver()} to return {@code false} regardless of system
 * properties.
 */
public abstract class AbstractComponentIT extends TestBenchTestCase {

    private static final Logger logger = LoggerFactory
            .getLogger(AbstractComponentIT.class);

    // ----- Feature flags -----

    /** One driver per class (reuse between methods). */
    private static final boolean REUSE_DRIVER = Boolean
            .getBoolean("test.reuseDriver");

    /**
     * One driver for the whole JVM fork (reuse between classes). Implies
     * REUSE_DRIVER.
     */
    private static final boolean SHARE_DRIVER = Boolean
            .getBoolean("test.shareDriver");

    /** Use Vaadin SPA navigation instead of full page loads. */
    private static final boolean USE_SPA = Boolean.getBoolean("test.useSpa");

    /** Run Chrome in headed (visible) mode. Default is headless. */
    private static final boolean HEADED = Boolean.getBoolean("test.headed");

    private static final boolean USE_HUB = Boolean.getBoolean("test.use.hub");

    private static final String HUB_URL = System.getProperty("test.hub.url",
            "http://" + Parameters.getHubHostname() + ":4444/wd/hub");

    // ----- Driver state -----

    /**
     * Per-thread driver. Persists across classes when SHARE_DRIVER is true,
     * across methods when REUSE_DRIVER is true, created fresh per method
     * otherwise.
     */
    private static final ThreadLocal<WebDriver> sharedDriver = new ThreadLocal<>();

    /**
     * Tracks all drivers created per thread so the JVM shutdown hook can quit
     * them cleanly. Only used when SHARE_DRIVER is true.
     */
    private static final ConcurrentHashMap<Long, WebDriver> allDrivers = new ConcurrentHashMap<>();

    private static volatile boolean shutdownHookRegistered = false;

    /**
     * File used to persist the ChromeDriver session across JVM restarts in
     * local debug mode (JDWP present). One file per surefire fork.
     */
    private static final Path DRIVER_FILE = Path
            .of(System.getProperty("java.io.tmpdir"), "vaadin-test-driver-"
                    + System.getProperty("surefire.forkNumber", "0") + ".txt");

    // ----- Consecutive-failure abort (only meaningful with driver sharing)
    // -----

    private static int consecutiveFailures = 0;
    private static final int MAX_CONSECUTIVE_FAILURES = 5;

    @Rule
    public TestRule consecutiveFailureAbort = new TestRule() {
        @Override
        public Statement apply(Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    if (SHARE_DRIVER
                            && consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                        throw new AssumptionViolatedException("Aborting: "
                                + consecutiveFailures
                                + " consecutive failures, driver may be unstable");
                    }
                    try {
                        base.evaluate();
                        consecutiveFailures = 0;
                    } catch (AssumptionViolatedException e) {
                        throw e;
                    } catch (Throwable t) {
                        consecutiveFailures++;
                        throw t;
                    }
                }
            };
        }
    };

    @Rule
    public ScreenshotOnFailureRule screenshotOnFailure = new ScreenshotOnFailureRule(
            this, false);

    // ----- JUnit lifecycle -----

    @BeforeClass
    public static void createDriver() {
        if (!REUSE_DRIVER && !SHARE_DRIVER) {
            return;
        }
        if (SHARE_DRIVER) {
            ensureShutdownHook();
            // Reuse the existing driver if it is still alive.
            WebDriver existing = sharedDriver.get();
            if (existing != null && isDriverAlive(existing)) {
                return;
            }
            if (existing != null) {
                tryQuitDriver(existing);
                sharedDriver.remove();
            }
            // Try to reconnect to a persisted session in local debug mode.
            if (isDebugMode() && Files.exists(DRIVER_FILE)) {
                try {
                    WebDriver reconnected = reconnectDriver();
                    if (reconnected != null && isDriverAlive(reconnected)) {
                        sharedDriver.set(reconnected);
                        allDrivers.put(Thread.currentThread().getId(),
                                reconnected);
                        return;
                    }
                } catch (Exception e) {
                    // Reconnect failed; fall through to create a fresh driver.
                }
            }
        }
        WebDriver driver = createWebDriver();
        driver.manage().window().setSize(new Dimension(1024, 800));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        sharedDriver.set(driver);
        if (SHARE_DRIVER) {
            allDrivers.put(Thread.currentThread().getId(), driver);
            if (isDebugMode()) {
                saveDriverInfo(driver);
            }
        }
    }

    /**
     * Returns whether this test class participates in driver reuse. Override
     * and return {@code false} in subclasses that require a fresh browser per
     * method regardless of {@code test.reuseDriver} / {@code test.shareDriver}.
     */
    protected boolean isReuseDriver() {
        return REUSE_DRIVER || SHARE_DRIVER;
    }

    @Before
    public void resetDriver() throws Exception {
        if (!isReuseDriver()) {
            WebDriver driver = createWebDriver();
            driver.manage().window().setSize(new Dimension(1024, 800));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
            sharedDriver.set(driver);
        }
        setDriver(sharedDriver.get());
        getDriver().manage().deleteAllCookies();
        getDriver().navigate().to("about:blank");
    }

    @After
    public void quitDriverPerMethod() {
        if (!isReuseDriver()) {
            tryQuitDriver(sharedDriver.get());
            sharedDriver.remove();
        }
    }

    @AfterClass
    public static void quitDriver() {
        if (SHARE_DRIVER) {
            // Keep the driver alive for the next test class.
            // The shutdown hook will quit it when the JVM exits.
            return;
        }
        if (!REUSE_DRIVER) {
            return;
        }
        tryQuitDriver(sharedDriver.get());
        sharedDriver.remove();
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
        String host = "localhost";
        if (USE_HUB) {
            host = findHostAddress();
        }
        return "http://" + host + ":8080";
    }

    private String findHostAddress() {
        try {
            return NetworkInterface.networkInterfaces()
                    .filter((networkInterface) -> {
                        try {
                            return networkInterface.isUp()
                                    && !networkInterface.isLoopback()
                                    && !networkInterface.isVirtual();
                        } catch (SocketException e) {
                            return false;
                        }
                    }).flatMap(NetworkInterface::inetAddresses)
                    .filter(InetAddress::isSiteLocalAddress)
                    .map(InetAddress::getHostAddress).findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "No compatible (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16) ip address found."));
        } catch (SocketException e) {
            throw new RuntimeException("Could not find the host name", e);
        }
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

        if (isReuseDriver() && USE_SPA) {
            if (trySpaNavigation(getTestPath())) {
                return;
            }
        }

        TimeoutException lastTimeout = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                getDriver().get(url);
                waitForDevServer();
                return;
            } catch (TimeoutException e) {
                lastTimeout = e;
                logger.warn(
                        "Page load timed out for {} (attempt {}/3), retrying",
                        url, attempt);
            }
        }
        throw lastTimeout;
    }

    /**
     * Attempts SPA navigation via the Vaadin client router.
     *
     * @return {@code true} if navigation succeeded, {@code false} if a full
     *         page load is required
     */
    private boolean trySpaNavigation(String path) {
        try {
            Boolean hasVaadin = (Boolean) executeScript(
                    "return !!(window.Vaadin && window.Vaadin.Flow)");
            if (!Boolean.TRUE.equals(hasVaadin)) {
                return false;
            }
            String currentPath = (String) executeScript(
                    "return window.location.pathname");
            String normalizedPath = path.startsWith("/") ? path : "/" + path;
            if (normalizedPath.equals(currentPath)) {
                // Same route: clear cookies so the server creates a fresh
                // session, then fall through to a full page load.
                getDriver().manage().deleteAllCookies();
                return false;
            }
            executeScript(
                    "window.dispatchEvent(new CustomEvent('vaadin-navigate',"
                            + "{detail:{url:arguments[0],state:null,replace:false,callback:true}}))",
                    path);
            getCommandExecutor().waitForVaadin();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ----- Driver management -----

    private static boolean isDriverAlive(WebDriver driver) {
        try {
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

    private static boolean isDebugMode() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments()
                .toString().contains("jdwp");
    }

    private static void ensureShutdownHook() {
        if (shutdownHookRegistered) {
            return;
        }
        synchronized (AbstractComponentIT.class) {
            if (shutdownHookRegistered) {
                return;
            }
            shutdownHookRegistered = true;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                allDrivers.values().forEach(AbstractComponentIT::tryQuitDriver);
                allDrivers.clear();
            }, "vaadin-test-driver-shutdown"));
        }
    }

    private static WebDriver reconnectDriver() throws Exception {
        String content = Files.readString(DRIVER_FILE);
        String[] parts = content.strip().split("\n");
        if (parts.length < 2) {
            return null;
        }
        String driverUrl = parts[0];
        String sessionId = parts[1];

        HttpCommandExecutor executor = new HttpCommandExecutor(
                new URL(driverUrl));
        Field commandCodecField = HttpCommandExecutor.class
                .getDeclaredField("commandCodec");
        commandCodecField.setAccessible(true);
        commandCodecField.set(executor, new W3CHttpCommandCodec());
        Field responseCodecField = HttpCommandExecutor.class
                .getDeclaredField("responseCodec");
        responseCodecField.setAccessible(true);
        responseCodecField.set(executor, new W3CHttpResponseCodec());

        RemoteWebDriver driver = new RemoteWebDriver(executor,
                (Capabilities) null) {
            @Override
            protected void startSession(Capabilities capabilities) {
                // Do not start a new session; we are reconnecting.
            }
        };
        Field sessionIdField = RemoteWebDriver.class
                .getDeclaredField("sessionId");
        sessionIdField.setAccessible(true);
        sessionIdField.set(driver, new SessionId(sessionId));
        Field capsField = RemoteWebDriver.class
                .getDeclaredField("capabilities");
        capsField.setAccessible(true);
        capsField.set(driver, new ChromeOptions());

        return TestBench.createDriver(driver);
    }

    private static void saveDriverInfo(WebDriver driver) {
        try {
            WebDriver actual = unwrap(driver);
            if (actual instanceof RemoteWebDriver) {
                RemoteWebDriver rwd = (RemoteWebDriver) actual;
                SessionId sid = rwd.getSessionId();
                if (sid != null) {
                    HttpCommandExecutor exec = (HttpCommandExecutor) rwd
                            .getCommandExecutor();
                    String info = exec.getAddressOfRemoteServer().toString()
                            + "\n" + sid.toString();
                    Files.writeString(DRIVER_FILE, info);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private static WebDriver unwrap(WebDriver driver) {
        WebDriver actual = driver;
        while (actual instanceof WrapsDriver) {
            actual = ((WrapsDriver) actual).getWrappedDriver();
        }
        return actual;
    }

    private static WebDriver createWebDriver() {
        for (int i = 0; i < 3; i++) {
            try {
                if (USE_HUB) {
                    return tryCreateRemoteDriver();
                }
                return tryCreateChromeDriver();
            } catch (Exception e) {
                logger.warn("Unable to create driver on attempt " + i, e);
            }
        }
        throw new RuntimeException(
                "Gave up trying to create a driver instance");
    }

    private static ChromeOptions buildChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        if (!HEADED && !isDebugMode()) {
            options.addArguments("--headless=new", "--disable-gpu",
                    "--disable-backgrounding-occluded-windows");
        }

        String extraArgs = System.getenv("TESTBENCH_CHROME_EXTRA_ARGS");
        if (extraArgs != null && !extraArgs.isBlank()) {
            options.addArguments(extraArgs.split("\\s+"));
        }

        String chromeBinary = System.getenv("TESTBENCH_CHROME_BINARY");
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }
        return options;
    }

    private static WebDriver tryCreateChromeDriver() {
        ChromeOptions options = buildChromeOptions();
        int port = PortProber.findFreePort();
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingPort(port).withSilent(true).build();
        ChromeDriver chromeDriver = new ChromeDriver(service, options);
        return TestBench.createDriver(chromeDriver);
    }

    private static WebDriver tryCreateRemoteDriver()
            throws MalformedURLException {
        ChromeOptions options = buildChromeOptions();
        RemoteWebDriver remoteDriver = new RemoteWebDriver(
                URI.create(HUB_URL).toURL(), options);
        return TestBench.createDriver(remoteDriver);
    }

    // ----- Test helper methods -----

    protected <T> void waitUntilNot(ExpectedCondition<T> condition) {
        waitUntilNot(condition, 10);
    }

    protected <T> void waitUntilNot(ExpectedCondition<T> condition,
            long timeoutInSeconds) {
        waitUntil(ExpectedConditions.not(condition), timeoutInSeconds);
    }

    public boolean isElementPresent(By by) {
        try {
            WebElement element = getDriver().findElement(by);
            return element != null;
        } catch (Exception e) {
            return false;
        }
    }

    protected void clickElementWithJs(String elementId) {
        executeScript(String.format("document.getElementById('%s').click();",
                elementId));
    }

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

    protected void scrollToElement(WebElement element) {
        Objects.requireNonNull(element,
                "The element to scroll to should not be null");
        getCommandExecutor().executeScript("arguments[0].scrollIntoView(true);",
                element);
    }

    protected void scrollIntoViewAndClick(WebElement element) {
        scrollToElement(element);
        element.click();
    }

    protected List<LogEntry> getLogEntries(Level level) {
        getCommandExecutor().waitForVaadin();

        return getDriver().manage().logs().get(LogType.BROWSER).getAll()
                .stream()
                .filter(logEntry -> logEntry.getLevel().intValue() >= level
                        .intValue())
                .filter(logEntry -> !logEntry.getMessage()
                        .contains("favicon.ico"))
                .collect(Collectors.toList());
    }

    private static final String WEB_SOCKET_CONNECTION_ERROR_PREFIX = "WebSocket connection to ";

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

    protected void checkLogsForErrors() {
        checkLogsForErrors(msg -> false);
    }

    protected void waitForDevServer() {
        Object result;
        do {
            getCommandExecutor().waitForVaadin();
            result = getCommandExecutor().executeScript(
                    "return window.Vaadin && window.Vaadin.Flow && window.Vaadin.Flow.devServerIsNotLoaded;");
        } while (Boolean.TRUE.equals(result));
    }

    public void blur() {
        executeScript(
                "!!document.activeElement ? document.activeElement.blur() : 0");
    }

    public String getProperty(WebElement element, String propertyName) {
        Object result = executeScript(
                "return arguments[0]." + propertyName + ";", element);
        return result == null ? null : String.valueOf(result);
    }

}
