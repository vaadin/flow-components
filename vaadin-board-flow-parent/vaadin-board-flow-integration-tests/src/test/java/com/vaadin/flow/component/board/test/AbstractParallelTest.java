package com.vaadin.flow.component.board.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.function.Supplier;

import org.junit.Assert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.common.testbench.test.AbstractParallelSauceLabsTest;

public abstract class AbstractParallelTest
        extends AbstractParallelSauceLabsTest {

    public Supplier<WebElement> buttonSwitchSupplier = () -> $(
            ButtonElement.class).id(AbstractComponentTestView.SWITCH);

    public void testGenericWidth(WebElement testedElement) throws Exception {
        WebElement controlElement = $(ButtonElement.class)
                .id(AbstractComponentTestView.SWITCH);
        TestFunctions.assertDimension(controlElement, testedElement, (elem) -> {
            return elem.getSize().width;
        });
    }

    public void setup() throws Exception {
        super.setup();
        getDriver().manage().window().setSize(new Dimension(1024, 768));
    }

    public void compareScreen(String screenshotName) throws IOException {
        String prefix = getClass().getSimpleName().replaceAll("IT", "");
        String referenceName = prefix + "_" + screenshotName;
        Assert.assertTrue(
                "Screenshot " + referenceName + " contains differences",
                testBench().compareScreen(referenceName));
    }

    protected void open(Class<?> viewClass) {
        String url = getTestUrl(viewClass);
        getDriver().get(url);
    }

    protected String getBaseURL() {
        return "http://" + findAutoHostname() + ":" + getPort();
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

        final Package aPackage = viewClass.getPackage();
        final String aPackageName = aPackage.getName();
        return viewClass.getName().replace(aPackageName, "").replace(".", "/");
    }

    protected String getPort() {
        return "8080";
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

}
