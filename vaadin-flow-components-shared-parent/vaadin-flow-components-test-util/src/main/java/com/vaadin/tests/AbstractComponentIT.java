/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.util.List;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

public abstract class AbstractComponentIT
        extends com.vaadin.flow.testutil.AbstractComponentIT {

    protected int getDeploymentPort() {
        return 8080;
    }

    @Override
    protected List<DesiredCapabilities> customizeCapabilities(
            List<DesiredCapabilities> capabilities) {
        // This method is overridden to force the legacy Chrome headless mode
        // for the time being. The new `--headless=new` mode has an issue
        // that doesn't allow tests to adjust the browser window size with
        // `getDriver().manage().window().setSize(...)`, see more:
        // https://github.com/SeleniumHQ/selenium/issues/11706
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless", "--disable-gpu");

        capabilities.stream()
                .filter(cap -> "chrome".equalsIgnoreCase(cap.getBrowserName()))
                .forEach(cap -> cap.setCapability(ChromeOptions.CAPABILITY,
                        chromeOptions));

        return capabilities;
    }

    @Override
    public void setup() throws Exception {
        super.setup();

        // Set a default window size
        testBench().resizeViewPortTo(1024, 800);
    }
}
