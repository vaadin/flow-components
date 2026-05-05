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

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.AssumptionViolatedException;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.codec.w3c.W3CHttpCommandCodec;
import org.openqa.selenium.remote.codec.w3c.W3CHttpResponseCodec;

import com.vaadin.testbench.ScreenshotOnFailureRule;
import com.vaadin.testbench.TestBench;

/**
 * Base class for Flow component integration tests.
 * <p>
 * When {@code -Dtest.reuseDriver=true} is set, the browser is reused across
 * test methods (within a class) and across test classes (via file-based
 * ChromeDriver session reconnect). Navigation between different routes uses
 * SPA navigation ({@code vaadin-navigate} event), while same-route navigation
 * uses {@code location.reload()} to get a fresh view state.
 */
public abstract class AbstractComponentIT
        extends com.vaadin.flow.testutil.AbstractComponentIT {

    private static final boolean REUSE_DRIVER =
            Boolean.getBoolean("test.reuseDriver");

    private static final Path DRIVER_FILE =
            Path.of(System.getProperty("java.io.tmpdir"),
                    "vaadin-test-driver-"
                            + System.getProperty("surefire.forkNumber", "0")
                            + ".txt");

    @Rule
    public ScreenshotOnFailureRule screenshotOnFailure =
            new ScreenshotOnFailureRule(this, !REUSE_DRIVER);

    {
        if (REUSE_DRIVER) {
            disableParentDriverQuit();
        }
    }

    private static WebDriver sharedDriver;
    private static int consecutiveFailures = 0;
    private static final int MAX_CONSECUTIVE_FAILURES = 5;

    @Rule
    public TestRule consecutiveFailureAbort = new TestRule() {
        @Override
        public Statement apply(Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                        throw new AssumptionViolatedException(
                                "Aborting: " + consecutiveFailures
                                        + " consecutive failures");
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

    protected int getDeploymentPort() {
        return 8080;
    }

    @Override
    protected void open(String... parameters) {
        String url = getTestURL(parameters);
        String path = getTestPath();

        if (REUSE_DRIVER) {
            try {
                String currentUrl = getDriver().getCurrentUrl();
                Boolean hasVaadin = (Boolean) executeScript(
                        "return !!(window.Vaadin && window.Vaadin.Flow)");
                if (Boolean.TRUE.equals(hasVaadin)) {
                    if (currentUrl != null && !currentUrl.contains(path)) {
                        executeScript(
                                "window.dispatchEvent(new CustomEvent("
                                + "'vaadin-navigate', {detail:{url:arguments[0],"
                                + "state:null, replace:false, callback:true}}))",
                                path);
                    } else {
                        getDriver().get(url);
                        waitForDevServer();
                    }
                    return;
                }
            } catch (Exception e) {
                // Fall through to full load
            }
        }

        getDriver().get(url);
        waitForDevServer();
    }

    @Override
    protected void updateHeadlessChromeOptions(ChromeOptions chromeOptions) {
        String extraArgs = System.getenv("TESTBENCH_CHROME_EXTRA_ARGS");
        if (extraArgs != null && !extraArgs.isBlank()) {
            chromeOptions.addArguments(extraArgs.split("\\s+"));
        }

        String chromeBinary = System.getenv("TESTBENCH_CHROME_BINARY");
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            chromeOptions.setBinary(chromeBinary);
        }
    }

    @Override
    public void setup() throws Exception {
        if (!REUSE_DRIVER) {
            super.setup();
            testBench().resizeViewPortTo(1024, 800);
            return;
        }

        if (sharedDriver != null && isDriverAlive(sharedDriver)) {
            setDriver(sharedDriver);
            testBench().resizeViewPortTo(1024, 800);
            return;
        }

        if (Files.exists(DRIVER_FILE)) {
            try {
                WebDriver reconnected = reconnectDriver();
                if (reconnected != null && isDriverAlive(reconnected)) {
                    sharedDriver = reconnected;
                    setDriver(sharedDriver);
                    testBench().resizeViewPortTo(1024, 800);
                    return;
                }
            } catch (Exception e) {
                // Reconnect failed, create new driver
            }
        }

        if (sharedDriver != null) {
            tryQuitDriver(sharedDriver);
        }
        super.setup();
        sharedDriver = getDriver();
        saveDriverInfo(sharedDriver);
        testBench().resizeViewPortTo(1024, 800);
    }

    private void disableParentDriverQuit() {
        try {
            Field parentRule = com.vaadin.testbench.parallel.ParallelTest.class
                    .getDeclaredField("screenshotOnFailure");
            parentRule.setAccessible(true);
            ScreenshotOnFailureRule parentScreenshot =
                    (ScreenshotOnFailureRule) parentRule.get(this);
            parentScreenshot.setQuitDriverOnFinish(false);
        } catch (Exception e) {
            // Ignore
        }
    }

    private static WebDriver reconnectDriver() throws Exception {
        String content = Files.readString(DRIVER_FILE);
        String[] parts = content.strip().split("\n");
        if (parts.length < 2) return null;

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
                    HttpCommandExecutor exec =
                            (HttpCommandExecutor) rwd.getCommandExecutor();
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

    private static boolean isDriverAlive(WebDriver driver) {
        try {
            WebDriver actual = unwrap(driver);
            if (actual instanceof RemoteWebDriver) {
                return ((RemoteWebDriver) actual).getSessionId() != null;
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
            // Ignore
        }
    }
}
