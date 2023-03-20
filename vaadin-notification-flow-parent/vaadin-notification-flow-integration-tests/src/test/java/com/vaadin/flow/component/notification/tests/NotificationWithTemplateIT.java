
package com.vaadin.flow.component.notification.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-notification/notification-template-test")
public class NotificationWithTemplateIT extends AbstractComponentIT {

    private static final String NOTIFICATION_CONTAINER_TAG = "vaadin-notification-container";

    @Before
    public void init() {
        open();
    }

    @Test
    public void openNotification_clickThreeTimes_containerIsUpdated() {
        waitForElementPresent(By.id("open"));
        WebElement open = findElement(By.id("open"));
        open.click();

        waitForElementPresent(By.tagName(NOTIFICATION_CONTAINER_TAG));
        TestBenchElement overlay = $(NOTIFICATION_CONTAINER_TAG).first();
        TestBenchElement template = overlay.$("*").id("template");

        WebElement btn = template.$("*").id("btn");
        WebElement container = template.$("*").id("container");

        List<WebElement> spans = container.findElements(By.tagName("span"));
        Assert.assertTrue(spans.isEmpty());

        for (int i = 0; i < 3; i++) {
            btn.click();

            int size = i + 1;
            WebElement label = container.findElement(By.id("label-" + size));
            Assert.assertEquals("Label " + size, label.getText());
        }
    }
}
