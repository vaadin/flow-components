/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import com.vaadin.flow.testutil.net.PortProber;
import com.vaadin.testbench.TestBench;

public abstract class AbstractComponentIT
        extends com.vaadin.flow.testutil.AbstractComponentIT {

    protected int getDeploymentPort() {
        return 8080;
    }

    @Override
    public void setup() throws Exception {
        if (isInDevContainer()) {
            WebDriver driver = createDevContainerChromeDriver();
            setDriver(driver);
        } else {
            super.setup();
        }

        // Set a default window size
        testBench().resizeViewPortTo(1024, 800);
    }

    private static boolean isInDevContainer() {
        // This should be set in devcontainer.json
        String devContainer = System.getenv("FLOW_COMPONENTS_DEV_CONTAINER");
        return devContainer != null && devContainer.equals("true");
    }

    private static WebDriver createDevContainerChromeDriver() {
        // Adapted from ChromeBrowserTest
        // For dev containers we always need headless mode. Chrome binary needs
        // to be set to use Chromium installed in the container. For Chrome
        // Driver, TestBench seems to correctly pick it up from PATH.
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--disable-gpu");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.setBinary("/usr/bin/chromium");

        int port = PortProber.findFreePort();
        ChromeDriverService service = (new ChromeDriverService.Builder())
                .usingPort(port).withSilent(true).build();
        ChromeDriver chromeDriver = new ChromeDriver(service, options);
        return TestBench.createDriver(chromeDriver);
    }
}
