package com.vaadin.addon.board.testbenchtests;

import static com.vaadin.addon.board.testUI.UIFunctions.SWITCH;
import static com.vaadin.addon.board.testUI.UIFunctions.readTestbenchProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.testbench.annotations.BrowserConfiguration;
import com.vaadin.testbench.annotations.RunLocally;
import com.vaadin.testbench.annotations.RunOnHub;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.parallel.Browser;
import com.vaadin.testbench.parallel.ParallelTest;

@RunOnHub("tb3-hub.intra.itmill.com")
public abstract class AbstractParallelTest extends ParallelTest {

    @BeforeClass
    public static void setUp() throws Exception {
        readTestbenchProperties.execute();
    }

    public Supplier<WebElement> buttonSwitchSupplier = () -> $(ButtonElement.class)
        .caption(SWITCH).first();


    @Before
    public void setup() throws Exception {
        super.setup();
        openURL();
    }

    public void compareScreen(String referenceName) throws IOException {
        Assert.assertTrue( testBench(getDriver()).compareScreen(referenceName));
    }
    protected List<DesiredCapabilities> allBrowsers = null;

    @BrowserConfiguration
    public List<DesiredCapabilities> getBrowserConfiguration() {
        final String CONFIG_PATH = "./tbconfig.json";
        List<DesiredCapabilities> capabilities = null;
        TestUtils utils = new TestUtils();
        try {
            capabilities = utils.getCapabilitiesFromFile(CONFIG_PATH);
            return Collections.unmodifiableList(capabilities);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not create capabilities from file");
        }
    }

    public AbstractParallelTest() {
        super();
    }

    protected String getPort() {
        if ((getRunLocallyBrowser() != null)) {
            return "8080";
        } else if (getRunOnHub(getClass()) != null) {
            return "8080";
        }
        else {
            // can't find any configuration to setup WebDriver
            throw new IllegalArgumentException(
                "Can't instantiate WebDriver: No configuration found. Test case was not annotated with @RunLocally annotation nor @RunOnHub annotation, and system variable 'useLocalWebDriver' was not found or not set to true.");
        }
    }
    protected String getBaseURL() {
        //getHubHostname
        return "http://"+findAutoHostname()+":"+getPort();
    }

    protected abstract Class<?> getUIClass();

    protected String getDeploymentPath() {
        Class<?> uiClass = getUIClass();
        if (uiClass != null) {
            final Package aPackage = uiClass.getPackage();
            final String aPackageName = aPackage.getName();
            return uiClass.getName()
                .replace(aPackageName, "")
                .replace(".", "/");
        }
        return "/";
    }

    protected String getTestUrl() {
        String baseUrl = getBaseURL();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl + getDeploymentPath();
    }

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
                    if (current_addr.isSiteLocalAddress()) {
                        return current_addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("Could not enumerate ");
        }
        throw new RuntimeException(
            "No compatible (192.168.*) ip address found.");
    }


    protected void openURL() {
        String url = getTestUrl();
        getDriver().get(url);
    }

    protected void openURLWithAppRestart() {
        String url = getTestUrl() + "?restartApplication";
        getDriver().get(url);
    }

}
