/*
 * Copyright 2000-2013 Vaadin Ltd.
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

import java.util.logging.Logger;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.vaadin.server.LegacyApplication;
import com.vaadin.server.UIProvider;
import com.vaadin.testbench.parallel.Browser;
import com.vaadin.testbench.parallel.ParallelTest;
import com.vaadin.testbench.parallel.setup.SetupDriver;
import com.vaadin.ui.UI;

/**
 * Base class for TestBench 3+ tests. All TB3+ tests in the project should
 * extend this class.
 *
 * Provides:
 * <ul>
 * <li>Helpers for browser selection</li>
 * <li>Hub connection setup and teardown</li>
 * <li>Automatic generation of URL for a given test on the development server
 * using {@link #getUIClass()} or by automatically finding an enclosing UI class
 * and based on requested features, e.g. {@link #isDebug()}, {@link #isPush()}</li>
 * <li>Generic helpers for creating TB3+ tests</li>
 * </ul>
 *
 * @author Vaadin Ltd
 */
public abstract class AbstractTB3Test extends ParallelTest {
    /**
     * Height of the screenshots we want to capture
     */
    protected static final int SCREENSHOT_HEIGHT = 768;

    /**
     * Width of the screenshots we want to capture
     */
    protected static final int SCREENSHOT_WIDTH = 1280;

    private boolean debug = false;

    private boolean push = false;

    @Override
    public void setup() throws Exception {
        // override local driver behaviour, so we can easily specify local
        // PhantomJS
        // with a system property
        if (getBooleanProperty("localPhantom")) {
            WebDriver driver = new SetupDriver()
                    .setupLocalDriver(Browser.PHANTOMJS);
            setDriver(driver);
        } else {
            super.setup();
        }
    }

    protected boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(System.getProperty(key));
    }

    /**
     * Opens the given test (defined by {@link #getTestUrl()}, optionally with
     * debug window and/or push (depending on {@link #isDebug()} and
     * {@link #isPush()}.
     */
    protected void openTestURL() {
        driver.get(getTestUrl());
    }

    /**
     * Returns the full URL to be used for the test
     *
     * @return the full URL for the test
     */
    protected String getTestUrl() {
        String baseUrl = getBaseURL();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl + getDeploymentPath();
    }


    /**
     * Used to determine what URL to initially open for the test
     *
     * @return the host name of development server
     */
    protected abstract String getDeploymentHostname();

    /**
     * Used to determine what port the test is running on
     *
     * @return The port teh test is running on, by default 8888
     */
    protected abstract String getDeploymentPort();

    /**
     * Asserts that {@literal a} is &gt;= {@literal b}
     *
     * @param message
     *            The message to include in the {@link AssertionError}
     * @param a
     * @param b
     * @throws AssertionError
     *             If comparison fails
     */
    public static final <T> void assertGreaterOrEqual(String message,
            Comparable<T> a, T b) throws AssertionError {
        if (a.compareTo(b) >= 0) {
            return;
        }

        throw new AssertionError(decorate(message, a, b));
    }

    /**
     * Asserts that {@literal a} is &gt; {@literal b}
     *
     * @param message
     *            The message to include in the {@link AssertionError}
     * @param a
     * @param b
     * @throws AssertionError
     *             If comparison fails
     */
    public static final <T> void assertGreater(String message, Comparable<T> a,
            T b) throws AssertionError {
        if (a.compareTo(b) > 0) {
            return;
        }
        throw new AssertionError(decorate(message, a, b));
    }

    /**
     * Asserts that {@literal a} is &lt;= {@literal b}
     *
     * @param message
     *            The message to include in the {@link AssertionError}
     * @param a
     * @param b
     * @throws AssertionError
     *             If comparison fails
     */
    public static final <T> void assertLessThanOrEqual(String message,
            Comparable<T> a, T b) throws AssertionError {
        if (a.compareTo(b) <= 0) {
            return;
        }

        throw new AssertionError(decorate(message, a, b));
    }

    /**
     * Asserts that {@literal a} is &lt; {@literal b}
     *
     * @param message
     *            The message to include in the {@link AssertionError}
     * @param a
     * @param b
     * @throws AssertionError
     *             If comparison fails
     */
    public static final <T> void assertLessThan(String message,
            Comparable<T> a, T b) throws AssertionError {
        if (a.compareTo(b) < 0) {
            return;
        }
        throw new AssertionError(decorate(message, a, b));
    }

    private static <T> String decorate(String message, Comparable<T> a, T b) {
        message = message.replace("{0}", a.toString());
        message = message.replace("{1}", b.toString());
        return message;
    }

    /**
     * Returns the path that should be used for the test. The path contains the
     * full path (appended to hostname+port) and must start with a slash.
     *
     * @return The URL path to the UI class to test
     */
    protected String getDeploymentPath() {
        Class<?> uiClass = getUIClass();
        if (uiClass != null) {
            return getDeploymentPath(uiClass);
        }
        throw new IllegalArgumentException("Unable to determine path for "
                + getClass().getCanonicalName());

    }

    /**
     * Returns the UI class the current test is connected to (or in special
     * cases UIProvider or LegacyApplication). Uses the enclosing class if the
     * test class is a static inner class to a UI class.
     *
     * Test which are not enclosed by a UI class must implement this method and
     * return the UI class they want to test.
     *
     * Note that this method will update the test name to the enclosing class to
     * be compatible with TB2 screenshot naming
     *
     * @return the UI class the current test is connected to
     */
    protected Class<?> getUIClass() {
        try {
            // Convention: SomeUITest uses the SomeUI UI class
            String uiClassName = getClass().getName().replaceFirst("Test$", "");
            Class<?> cls = Class.forName(uiClassName);
            if (isSupportedRunnerClass(cls)) {
                return cls;
            }
        } catch (Exception e) {
        }
        Class<?> enclosingClass = getClass().getEnclosingClass();
        if (enclosingClass != null) {
            if (UI.class.isAssignableFrom(enclosingClass)) {
                Logger.getLogger(getClass().getName())
                        .severe("Test is an static inner class to the UI. This will no longer be supported in the future. The test should be named UIClassTest and reside in the same package as the UI");
                return enclosingClass;
            }
        }
        throw new RuntimeException(
                "Could not determine UI class. Ensure the test is named UIClassTest and is in the same package as the UIClass");
    }

    /**
     * @return true if the given class is supported by ApplicationServletRunner
     */
    @SuppressWarnings("deprecation")
    private boolean isSupportedRunnerClass(Class<?> cls) {
        if (UI.class.isAssignableFrom(cls)) {
            return true;
        }
        if (UIProvider.class.isAssignableFrom(cls)) {
            return true;
        }
        if (LegacyApplication.class.isAssignableFrom(cls)) {
            return true;
        }

        return false;
    }

    /**
     * Returns whether to run the test in debug mode (with the debug console
     * open) or not
     *
     * @return true to run with the debug window open, false by default
     */
    protected final boolean isDebug() {
        return debug;
    }

    /**
     * Sets whether to run the test in debug mode (with the debug console open)
     * or not.
     *
     * @param debug
     *            true to open debug window, false otherwise
     */
    protected final void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Returns whether to run the test with push enabled (using /run-push) or
     * not. Note that push tests can and should typically be created using @Push
     * on the UI instead of overriding this method
     *
     * @return true if /run-push is used, false otherwise
     */
    protected final boolean isPush() {
        return push;
    }

    /**
     * Sets whether to run the test with push enabled (using /run-push) or not.
     * Note that push tests can and should typically be created using @Push on
     * the UI instead of overriding this method
     *
     * @param push
     *            true to use /run-push in the test, false otherwise
     */
    protected final void setPush(boolean push) {
        this.push = push;
    }

    /**
     * Returns the path for the given UI class when deployed on the test server.
     * The path contains the full path (appended to hostname+port) and must
     * start with a slash.
     *
     * This method takes into account {@link #isPush()} and {@link #isDebug()}
     * when the path is generated.
     *
     * @param uiClass
     * @return The path to the given UI class
     */
    private String getDeploymentPath(Class<?> uiClass) {
        String runPath = "";
        String classPath = uiClass.getSimpleName();
        if (UI.class.isAssignableFrom(uiClass)) {
            return runPath + "/" + classPath
                    + (isDebug() ? "?debug" : "");
        } else if (LegacyApplication.class.isAssignableFrom(uiClass)) {
            return runPath + "/" + classPath
                    + "?restartApplication" + (isDebug() ? "&debug" : "");
        } else {
            throw new IllegalArgumentException(
                    "Unable to determine path for enclosing class "
                            + classPath);
        }
    }

    /**
     * Used to determine what URL to initially open for the test
     *
     * @return The base URL for the test. Does not include a trailing slash.
     */
    protected String getBaseURL() {
        return "http://" + getDeploymentHostname() + ":" + getDeploymentPort();
    }

    /**
     * Generates the application id based on the URL in a way compatible with
     * VaadinServletService.
     *
     * @param pathWithQueryParameters
     *            The path part of the URL, possibly still containing query
     *            parameters
     * @return The application ID string used in Vaadin locators
     */
    private String getApplicationId(String pathWithQueryParameters) {
        // Remove any possible URL parameters
        String pathWithoutQueryParameters = pathWithQueryParameters.replaceAll(
                "\\?.*", "");
        if ("".equals(pathWithoutQueryParameters)) {
            return "ROOT";
        }

        // Retain only a-z and numbers
        return pathWithoutQueryParameters.replaceAll("[^a-zA-Z0-9]", "");
    }

    /**
     * Called by the test runner whenever there is an exception in the test that
     * will cause termination of the test
     *
     * @param t
     *            the throwable which caused the termination
     */
    public void onUncaughtException(Throwable t) {
        // Do nothing by default

    }

    /**
     * Uses JavaScript to determine the currently focused element.
     * 
     * @return Focused element or null
     */
    protected WebElement getFocusedElement() {
        Object focusedElement = executeScript("return document.activeElement");
        if (null != focusedElement) {
            return (WebElement) focusedElement;
        } else {
            return null;
        }
    }

    /**
     * Executes the given Javascript
     * 
     * @param script
     *            the script to execute
     * @return whatever
     *         {@link org.openqa.selenium.JavascriptExecutor#executeScript(String, Object...)}
     *         returns
     */
    protected Object executeScript(String script) {
        return ((JavascriptExecutor) getDriver()).executeScript(script);
    }
}
