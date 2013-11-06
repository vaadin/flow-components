/*
 * Copyright 2000-2013 Vaadind Ltd.
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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.testbench.TestBench;

/**
 * Provides values for parameters which depend on where the test is run.
 * Parameters should be configured in test/resources/test.properties.
 * 
 * @author Vaadin Ltd
 */
public abstract class PrivateTB3Configuration extends ScreenshotTB3Test {
    public static final String PORT_PROPERTY = "com.vaadin.testbench.deployment.port";
    public static final String DEFAULT_PORT = "9998";
    private static final String HOSTNAME_PROPERTY = "com.vaadin.testbench.deployment.hostname";
    private final Properties properties = new Properties();

    public PrivateTB3Configuration() {
        try {
            properties.load(getClass().getResourceAsStream(
                    File.separator + "test.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getProperty(String name) {
        String property = properties.getProperty(name);
        if (property == null) {
            property = System.getProperty(name);
        }

        return property;
    }

    @Override
    protected String getScreenshotDirectory() {
        String screenshotDirectory = getProperty("com.vaadin.testbench.screenshot.directory");
        if (screenshotDirectory == null) {
            throw new RuntimeException(
                    "No screenshot directory defined. Use -Dcom.vaadin.testbench.screenshot.directory=<path>");
        }
        return screenshotDirectory;
    }

    @Override
    protected String getHubHostname() {
        return "tb3-hub.intra.itmill.com";
    }

    @Override
    protected String getDeploymentHostname() {
        String hostName = getProperty(HOSTNAME_PROPERTY);

        if (hostName == null || "".equals(hostName)) {
            hostName = findAutoHostname();
        }

        return hostName;
    }

    @Override
    protected String getDeploymentPort() {
        String port = getProperty(PORT_PROPERTY);

        if (port == null || "".equals(port)) {
            port = DEFAULT_PORT;
        }

        return port;
    }

    /**
     * Tries to automatically determine the IP address of the machine the test
     * is running on.
     * 
     * @return An IP address of one of the network interfaces in the machine.
     * @throws RuntimeException
     *             if there was an error or no IP was found
     */
    private String findAutoHostname() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface current = interfaces.nextElement();
                if (!current.isUp() || current.isLoopback()
                        || current.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = current.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress current_addr = addresses.nextElement();
                    if (current_addr.isLoopbackAddress()) {
                        continue;
                    }
                    String hostAddress = current_addr.getHostAddress();
                    if (hostAddress.startsWith("192.168.")) {
                        return hostAddress;
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("Could not enumerate ");
        }

        throw new RuntimeException(
                "No compatible (192.168.*) ip address found.");
    }

    private boolean chrome = false;

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.tests.tb3.AbstractTB3Test#setupLocalDriver()
     */
    @Override
    protected void setupLocalDriver() {
        if (chrome) {
            WebDriver driver = new ChromeDriver();
            setDriver(TestBench.createDriver(driver));
            DesiredCapabilities chrome = BrowserUtil
                    .chrome(MultiBrowserTest.TESTED_CHROME_VERSION);
            chrome.setPlatform(Platform.MAC);
            setDesiredCapabilities(chrome);
        } else {
            String firefoxPath = getProperty("firefox.path");
            WebDriver driver;
            if (firefoxPath != null) {
                driver = new FirefoxDriver(new FirefoxBinary(new File(
                        firefoxPath)), null);
            } else {
                driver = new FirefoxDriver();
            }
            setDriver(TestBench.createDriver(driver));
            // desired capabilities for FF are set on AbstractTB3Test
        }
        testBench().resizeViewPortTo(SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT);
    }

    @Override
    protected Platform getPlatform() {
        // XXX remember to set the correct local platform so that
        // SheetController will work
        return Platform.MAC;
    }
}
