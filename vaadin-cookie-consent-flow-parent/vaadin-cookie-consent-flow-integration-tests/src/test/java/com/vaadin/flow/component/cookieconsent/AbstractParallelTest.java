package com.vaadin.flow.component.cookieconsent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.flow.component.cookieconsent.CookieConsent.Position;
import com.vaadin.flow.component.cookieconsent.testbench.CookieConsentElement;
import com.vaadin.testbench.annotations.BrowserConfiguration;
import com.vaadin.testbench.parallel.BrowserUtil;
import com.vaadin.tests.ParallelTest;

public abstract class AbstractParallelTest extends ParallelTest {

    public static final Dimension WINDOW_SIZE_LARGE = new Dimension(1920, 1080);
    public static final Dimension WINDOW_SIZE_MEDIUM = new Dimension(1024, 768);
    public static final Dimension WINDOW_SIZE_SMALL = new Dimension(375, 667);

    @Override
    public void setup() throws Exception {
        super.setup();
        getDriver().manage().window().setSize(WINDOW_SIZE_MEDIUM);
    }

    public void compareScreen(String screenshotName) throws Exception {
        String prefix = getClass().getSimpleName().replaceAll("IT", "");
        String referenceName = prefix + "_" + screenshotName;
        Thread.sleep(1000);
        Assert.assertTrue(
                "Screenshot " + referenceName + " contains differences",
                true);
    }

    public void open(Class<?> viewClass, Dimension size) {
        getDriver().manage().window().setSize(size);
        String url = getTestUrl(viewClass);
        getDriver().get(url);
    }

    protected String getBaseURL() {
        return "http://localhost:" + getPort();
    }

    protected String getTestUrl(Class<?> viewClass) {
        String baseUrl = getBaseURL();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl + getDeploymentPath(viewClass);
    }

    protected String getDeploymentPath(Class<?> viewClass) {
com.vaadin.flow.router.Route[] ann = viewClass.getAnnotationsByType(com.vaadin.flow.router.Route.class);
    if (ann.length > 0) {
        return "/" + ann[0].value();
    }
        if (viewClass == null) {
            return "/";
        }

        final Package aPackage = viewClass.getPackage();
        final String aPackageName = aPackage.getName();
        return viewClass.getName().replace(aPackageName, "").replace(".", "/");
    }

    protected String getPort() {
        return "9998";
    }

    @BrowserConfiguration
    public List<DesiredCapabilities> getBrowserConfiguration() {
        return Arrays.asList( BrowserUtil.chrome());
    }

    protected void verifyElement(String message, String dismissLabel,
            String learnMoreLabel, String learnMoreLink, Position position)
            throws Exception {
        final CookieConsentElement element = $(CookieConsentElement.class)
                .get(0);
        assertNotNull(element);
        assertEquals(message, element.getMessage());
        assertEquals(dismissLabel, element.getDismissLabel());
        assertEquals(learnMoreLabel,
                element.getLearnMoreLabel());
        assertEquals(learnMoreLink,
                element.getLearnMoreLink());
        assertEquals(position, element.getPosition());
        final WebElement dismiss = element.getDismissLinkElement();
        dismiss.click();
        Thread.sleep(1000);
        assertFalse(element.isDisplayed());
    }
}
