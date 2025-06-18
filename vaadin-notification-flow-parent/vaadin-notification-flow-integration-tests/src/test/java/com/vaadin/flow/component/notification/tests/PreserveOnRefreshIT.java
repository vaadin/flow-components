/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.notification.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-notification/preserve-on-refresh")
public class PreserveOnRefreshIT extends AbstractComponentIT {
    private static final String NOTIFICATION_TAG = "vaadin-notification-card";

    private TestBenchElement openNotification;
    private TestBenchElement closeNotification;
    private TestBenchElement addComponent;

    @Before
    public void init() {
        open();
        openNotification = $("button").id("show-notification");
        closeNotification = $("button").id("close-notification");
        addComponent = $("button").id("add-component");
    }

    @Test
    public void open_reload_shouldStayOpen() {
        openNotification.click();
        assertNotificationIsOpen();

        getDriver().navigate().refresh();
        assertNotificationIsOpen();
    }

    @Test
    public void open_reload_close_reload_shouldStayClosed() {
        openNotification.click();
        assertNotificationIsOpen();

        getDriver().navigate().refresh();
        assertNotificationIsOpen();

        closeNotification = $("button").id("close-notification");
        closeNotification.click();
        assertNotificationIsClosed();

        getDriver().navigate().refresh();
        assertNotificationIsClosed();
    }

    @Test
    public void addComponent_open_reload_shouldContainComponent() {
        addComponent.click();
        openNotification.click();
        assertNotificationIsOpen();

        getDriver().navigate().refresh();
        TestBenchElement notification = $(NOTIFICATION_TAG).first();
        boolean containsComponentContent = notification.$("span")
                .attribute("id", "component-content").exists();
        Assert.assertTrue(
                "Notification card does not contain added component anymore",
                containsComponentContent);
    }

    private void assertNotificationIsOpen() {
        $(NOTIFICATION_TAG).waitForFirst();
    }

    private void assertNotificationIsClosed() {
        waitUntil(driver -> $(NOTIFICATION_TAG).all().size() == 0);
    }
}
