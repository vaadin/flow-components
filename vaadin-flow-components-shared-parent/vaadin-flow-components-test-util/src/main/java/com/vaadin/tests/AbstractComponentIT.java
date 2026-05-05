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

import org.junit.AssumptionViolatedException;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class AbstractComponentIT
        extends com.vaadin.flow.testutil.AbstractComponentIT {

    private static WebDriver sharedDriver;
    private static int consecutiveFailures = 0;
    private static final int MAX_CONSECUTIVE_FAILURES = 5;

    /**
     * Aborts test execution after too many consecutive failures,
     * preventing cascading timeouts when the server is down.
     */
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
        if (sharedDriver != null && isDriverAlive(sharedDriver)) {
            setDriver(sharedDriver);
            getDriver().manage().deleteAllCookies();
        } else {
            if (sharedDriver != null) {
                tryQuitDriver(sharedDriver);
            }
            super.setup();
            sharedDriver = getDriver();
        }
        testBench().resizeViewPortTo(1024, 800);
    }

    private static boolean isDriverAlive(WebDriver driver) {
        try {
            if (driver instanceof RemoteWebDriver) {
                return ((RemoteWebDriver) driver).getSessionId() != null;
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
