/*
 * Copyright 2000-2014 Vaadin Ltd.
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

package com.vaadin.addon.spreadsheet.test.tb3;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestName;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.testbench.annotations.BrowserConfiguration;
import com.vaadin.testbench.parallel.Browser;
import com.vaadin.testbench.parallel.BrowserUtil;

/**
 * Base class for tests which should be run on all supported browsers. The test
 * is automatically launched for multiple browsers in parallel by the test
 * runner.
 * 
 * Sub classes can, but typically should not, restrict the browsers used by
 * implementing a
 * 
 * <pre>
 * &#064;Parameters
 * public static Collection&lt;DesiredCapabilities&gt; getBrowsersForTest() {
 * }
 * </pre>
 * 
 * @author Vaadin Ltd
 */
public abstract class MultiBrowserTest extends PrivateTB3Configuration {

    @Rule
    public TestName testName = new TestName();

    @Override
    public void setDesiredCapabilities(DesiredCapabilities desiredCapabilities) {
        if (BrowserUtil.isIE(desiredCapabilities)) {
            desiredCapabilities.setCapability(
                    InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
            desiredCapabilities.setCapability(
                    InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
        }

        desiredCapabilities.setCapability("project", "Vaadin Spreadsheet");
        desiredCapabilities.setCapability("build", String.format("%s / %s",
                getDeploymentHostname(), Calendar.getInstance().getTime()));
        desiredCapabilities.setCapability(
                "name",
                String.format("%s.%s", getClass().getCanonicalName(),
                        testName.getMethodName()));

        super.setDesiredCapabilities(desiredCapabilities);
    }

    @BrowserConfiguration
    public List<DesiredCapabilities> getBrowsersToTest() {
        return getBrowserCapabilities( Browser.IE11,
                Browser.FIREFOX, Browser.CHROME, Browser.PHANTOMJS);
    }

    protected List<DesiredCapabilities> getBrowsersExcludingPhantomJS() {
        return getBrowserCapabilities(Browser.IE11,
                Browser.CHROME, Browser.FIREFOX);
    }

    protected List<DesiredCapabilities> getBrowserCapabilities(
            Browser... browsers) {
        List<DesiredCapabilities> capabilities = new ArrayList<DesiredCapabilities>();
        for (Browser browser : browsers) {
            capabilities.add(browser.getDesiredCapabilities());
        }
        return capabilities;
    }

    /**
     * Exception which is thrown when some specific browser is wanted to be skipped.
     * Extends AssumptionViolatedException which causes JUnit to ignore the running
     * test.
     */
    private class BrowserSkipped extends AssumptionViolatedException {
        public BrowserSkipped(String message) {
            super("Skipped <"+message+">");
        }
    }

    /**
     * Call this method if you want to skip the test on some specific browser.
     * For example, some versions of PhantomJS does not fire onContextMenu event on right click so
     * that browser could be skipped for a test which relays on it.
     *
     *
     * @param reason why the browser is skipped. This will be shown in test results.
     * @param browser which is wanted to be skipped
     */
    protected void skipBrowser(String reason, Browser... browser) {
        for (int i = 0; i < browser.length; i++) {
            skipBrowser(reason, browser[i]);
        }
    }

    private void skipBrowser(String reason, Browser browser) {
        DesiredCapabilities capabilities = getDesiredCapabilities();
        switch (browser) {
            case FIREFOX: if(BrowserUtil.isFirefox(capabilities)) { throw new BrowserSkipped(reason); }
                break;
            case CHROME: if(BrowserUtil.isChrome(capabilities)) { throw new BrowserSkipped(reason); }
                break;
            case SAFARI: if(BrowserUtil.isSafari(capabilities)) { throw new BrowserSkipped(reason); }
                break;
            case IE8: if(BrowserUtil.isIE(capabilities, 8)) { throw new BrowserSkipped(reason); }
                break;
            case IE9: if(BrowserUtil.isIE(capabilities, 9)) { throw new BrowserSkipped(reason); }
                break;
            case IE10: if(BrowserUtil.isIE(capabilities, 10)) { throw new BrowserSkipped(reason); }
                break;
            case IE11: if(BrowserUtil.isIE(capabilities, 11)) { throw new BrowserSkipped(reason); }
                break;
            case PHANTOMJS: if(BrowserUtil.isPhantomJS(capabilities)) { throw new BrowserSkipped(reason); }
                break;
            default: throw new RuntimeException("Unknown browser: "+browser);
        }
    }
}
