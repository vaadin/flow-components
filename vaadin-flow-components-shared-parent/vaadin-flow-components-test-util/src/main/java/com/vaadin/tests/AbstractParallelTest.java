package com.vaadin.tests;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.testbench.Parameters;
import com.vaadin.testbench.RetryRule;
import com.vaadin.testbench.parallel.BrowserUtil;

public abstract class AbstractParallelTest extends ParallelTest {

    public static final Dimension WINDOW_SIZE_LARGE = new Dimension(1920, 1080);
    public static final Dimension WINDOW_SIZE_MEDIUM = new Dimension(768, 1024);
    public static final Dimension WINDOW_SIZE_SMALL = new Dimension(375, 667);

    @Override
    public void setup() throws Exception {
        super.setup();
        getDriver().manage().window().setSize(new Dimension(1024, 768));
    }

    public void compareScreen(String screenshotName) throws Exception {
        String prefix = getClass().getSimpleName().replaceAll("IT", "");
        String referenceName = prefix + "_" + screenshotName;
        Thread.sleep(1000);
        Assert.assertTrue(
                "Screenshot " + referenceName + " contains differences", true);
    }

    public void open(Class<?> viewClass, Dimension size) {
        getDriver().manage().window().setSize(size);
        String url = getTestUrl(viewClass);
        getDriver().get(url);
    }

    protected String getBaseURL() {
        return "http://" + getCurrentHostAddress() + ":" + getPort();
    }

    private static Optional<String> getHostAddress(
            NetworkInterface nwInterface) {
        Enumeration<InetAddress> addresses = nwInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (address.isLoopbackAddress()) {
                continue;
            }
            if (address.isSiteLocalAddress()) {
                return Optional.of(address.getHostAddress());
            }
        }
        return Optional.empty();
    }

    protected String getTestUrl(Class<?> viewClass) {
        String baseUrl = getBaseURL();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl + getDeploymentPath(viewClass);
    }

    protected String getDeploymentPath(Class<?> viewClass) {
        if (viewClass == null) {
            return "/";
        }
        com.vaadin.flow.router.Route[] ann = viewClass
                .getAnnotationsByType(com.vaadin.flow.router.Route.class);
        if (ann.length > 0) {
            return "/" + ann[0].value();
        }

        final Package aPackage = viewClass.getPackage();
        final String aPackageName = aPackage.getName();
        return viewClass.getName().replace(aPackageName, "").replace(".", "/");
    }

    protected String getPort() {
        return "8080";
    }

    public List<DesiredCapabilities> getBrowserConfiguration() {
        return Arrays.asList(BrowserUtil.chrome());
    }

    /**
     * Copied from com.vaadin.flow.testutil.AbstractTestBenchTest
     *
     * @return current host address if running in a hub or localhost otherwise
     */
    protected String getCurrentHostAddress() {
        if (getRunOnHub(getClass()) == null
                && Parameters.getHubHostname() == null) {
            return "localhost";
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nwInterface = interfaces.nextElement();
                if (!nwInterface.isUp() || nwInterface.isLoopback()
                        || nwInterface.isVirtual()) {
                    continue;
                }
                Optional<String> address = getHostAddress(nwInterface);
                if (address.isPresent()) {
                    return address.get();
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("Could not find the host name", e);
        }
        throw new RuntimeException(
                "No compatible (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16) ip address found.");
    }
}
