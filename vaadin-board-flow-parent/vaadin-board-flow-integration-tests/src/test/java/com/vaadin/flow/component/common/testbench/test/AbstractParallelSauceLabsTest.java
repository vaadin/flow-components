/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.flow.component.common.testbench.test;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.saucelabs.ci.sauceconnect.AbstractSauceTunnelManager;
import com.vaadin.testbench.annotations.RunOnHub;
import com.vaadin.testbench.parallel.Browser;
import com.vaadin.testbench.parallel.ParallelTest;

@RunOnHub
public abstract class AbstractParallelSauceLabsTest extends ParallelTest {

    @Override
    protected String getHubURL() {
        String username = getSauceUser();
        String accessKey = System.getProperty("sauce.sauceAccessKey");

        if (username == null) {
            throw new IllegalArgumentException(
                    "You must give a Sauce Labs user name using -Dsauce.user=<username> "
                            + "or by adding sauce.user=<username> to local.properties");
        }
        if (accessKey == null) {
            throw new IllegalArgumentException(
                    "You must give a Sauce Labs access key using -Dsauce.sauceAccessKey=<accesskey> "
                            + "or by adding sauce.sauceAccessKey=<accesskey> to local.properties");
        }
        return "http://" + username + ":" + accessKey
                + "@localhost:4445/wd/hub";
    }

    private String getSauceUser() {
        return System.getProperty("sauce.user");
    }

    @Override
    public void setDesiredCapabilities(
            DesiredCapabilities desiredCapabilities) {
        String tunnelId = AbstractSauceTunnelManager
                .getTunnelIdentifier(System.getProperty("sauce.options"), null);
        if (tunnelId != null) {
            desiredCapabilities.setCapability("tunnelIdentifier", tunnelId);
        }
        super.setDesiredCapabilities(desiredCapabilities);
    }

    @Override
    protected Browser getRunLocallyBrowser() {
        if (getSauceUser() == null) {
            System.err.println(
                    "No Sauce Labs user given using -Dsauce.user=<username>. Running on local Chrome");
            return Browser.CHROME;
        } else {
            return super.getRunLocallyBrowser();
        }
    }

}
