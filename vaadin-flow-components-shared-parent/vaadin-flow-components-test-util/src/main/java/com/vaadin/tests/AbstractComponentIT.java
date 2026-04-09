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

import org.junit.AfterClass;
import org.junit.Rule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.vaadin.testbench.ScreenshotOnFailureRule;

public abstract class AbstractComponentIT
        extends com.vaadin.flow.testutil.AbstractComponentIT {

    private static WebDriver sharedDriver;

    /**
     * Overrides the rule from ParallelTest to prevent quitting the driver after
     * each test method, allowing driver reuse across tests in the same class.
     */
    @Rule
    public ScreenshotOnFailureRule screenshotOnFailure = new ScreenshotOnFailureRule(
            this, false);

    protected int getDeploymentPort() {
        return 8080;
    }

    @Override
    protected void updateHeadlessChromeOptions(ChromeOptions chromeOptions) {
        String extraArgs = System.getenv("TESTBENCH_CHROME_EXTRA_ARGS");
        if (extraArgs != null && !extraArgs.isBlank()) {
            chromeOptions.addArguments(extraArgs.split("\\s+"));
        }
    }

    @Override
    public void setup() throws Exception {
        if (sharedDriver != null && isDriverAlive(sharedDriver)) {
            // Reuse existing driver, reset browser state from previous test
            setDriver(sharedDriver);
            getDriver().manage().deleteAllCookies();
            getDriver().navigate().to("about:blank");
        } else {
            // Clean up dead driver reference if needed
            if (sharedDriver != null) {
                tryQuitDriver(sharedDriver);
                sharedDriver = null;
            }
            // Create new driver via parent chain
            super.setup();
            sharedDriver = getDriver();
        }

        // Set a default window size
        testBench().resizeViewPortTo(1024, 800);
    }

    @AfterClass
    public static void quitSharedDriver() {
        if (sharedDriver != null) {
            tryQuitDriver(sharedDriver);
            sharedDriver = null;
        }
    }

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
}
