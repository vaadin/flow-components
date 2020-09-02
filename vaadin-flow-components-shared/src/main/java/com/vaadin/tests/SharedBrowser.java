package com.vaadin.tests;

import com.vaadin.testbench.Parameters;
import com.vaadin.testbench.ScreenshotOnFailureRule;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchDriverProxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.http.W3CHttpCommandCodec;
import org.openqa.selenium.remote.http.W3CHttpResponseCodec;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SharedBrowser {
    static final SharedBrowser instance = new SharedBrowser();
    URL url;
    SessionId sessionId;
    private volatile TestBenchDriverProxy driver;

    private static Logger logger = Logger.getLogger(SharedBrowser.class.getName());

    SharedBrowser() {

    }

    TestBenchDriverProxy getDriver(DriverSupplier driverSupplier)
        throws Exception {
        if (driver == null) {
            synchronized (this) {
                if (driver == null) {
                    useDriver(driverSupplier.get());
                    return driver;
                }
            }
        }

        return createDriverFromSession(sessionId, url);
    }

    void clear() {
        if(driver == null) {
            return;
        }
        logger.log(Level.FINE, String.format("Clearing driver for session %s\turl %s", sessionId, url));
        driver.quit();
        driver = null;
        sessionId = null;
        url = null;
    }

    interface ISetup {
        void setup() throws Exception;
    }

    Optional<WebDriver> setup(ISetup realSetup, Supplier<WebDriver> getDriver,
        ScreenshotOnFailureRule screenshotOnFailure) throws Exception {
        if (Parameters.getTestsInParallel() != 1) {
            realSetup.setup();
            return Optional.empty();
        }
        screenshotOnFailure.setQuitDriverOnFinish(false);
        return Optional.of(getDriver(() -> {
            realSetup.setup();
            return getDriver.get();
        }));
    }

    private TestBenchDriverProxy createDriverFromSession(
        final SessionId sessionId, URL command_executor) {
        CommandExecutor executor = new HttpCommandExecutor(command_executor) {

            @Override
            public Response execute(Command command) throws IOException {
                Response response = null;
                if (command.getName() == "newSession") {

                    driver.manage().deleteAllCookies();
                    if (driver instanceof WebStorage) {
                        ((WebStorage)driver).getSessionStorage().clear();
                        ((WebStorage)driver).getLocalStorage().clear();
                    }
                    driver.manage().logs().get(LogType.BROWSER).getAll();
                    driver.get("about:blank");
                    response = new Response();
                    response.setSessionId(sessionId.toString());
                    response.setStatus(0);
                    response.setValue(Collections.<String, String>emptyMap());

                    try {
                        Field commandCodec = this.getClass().getSuperclass()
                            .getDeclaredField("commandCodec");
                        commandCodec.setAccessible(true);
                        commandCodec.set(this, new W3CHttpCommandCodec());

                        Field responseCodec = null;
                        responseCodec = this.getClass().getSuperclass()
                            .getDeclaredField("responseCodec");
                        responseCodec.setAccessible(true);
                        responseCodec.set(this, new W3CHttpResponseCodec());
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                } else {
                    response = super.execute(command);
                }
                return response;
            }
        };
        logger.log(Level.FINE, String.format("Reusing driver for session %s\turl %s", sessionId, url));

        RemoteWebDriver driver = new RemoteWebDriver(executor,
            new DesiredCapabilities());
        return TestBench.createDriver(driver);
    }

    private void useDriver(WebDriver currentDriver) {
        driver = (TestBenchDriverProxy) currentDriver;
        RemoteWebDriver webDriver = (RemoteWebDriver) driver.getWrappedDriver();
        HttpCommandExecutor executor = (HttpCommandExecutor) webDriver
            .getCommandExecutor();
        url = executor.getAddressOfRemoteServer();
        sessionId = webDriver.getSessionId();
        logger.log(Level.FINE, String.format("Creating driver for session %s\turl %s", sessionId, url));
    }

    public Optional<List<DesiredCapabilities>> getGridBrowsers() {
        List<DesiredCapabilities> result = Parameters.getGridBrowsers();
        if(result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    @FunctionalInterface
    interface DriverSupplier {
        WebDriver get() throws Exception;
    }
}
