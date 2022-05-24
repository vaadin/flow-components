/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.notification.tests;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Integration tests for the {@link NotificationView}.
 */
@TestPath("vaadin-notification")
public class NotificationIT extends AbstractComponentIT {
    private static final String NOTIFICATION_TAG = "vaadin-notification-card";

    @Before
    public void init() {
        open();
    }

    @Test
    public void defaultNotification() {
        findElement(By.id("default-notification-button")).click();
        checkNotificationIsOpen();
        assertNotificationContent("text content");
        Assert.assertEquals(1,
                findElements(By.id("default-notification")).size());
        checkNotificationIsClosed();
    }

    @Test
    public void notificationWithPosition() {
        findElement(By.id("position-notification-button")).click();
        checkNotificationIsOpen();
        assertNotificationContent("Top-Left");
        Assert.assertEquals(1,
                findElements(By.id("position-notification")).size());
        checkNotificationIsClosed();
    }

    @Test
    public void notificationWithStaticConvenienceMethod() {
        checkNotificationIsOpen();
        assertNotificationContent("static");
        checkNotificationIsClosed();
        Assert.assertEquals(0,
                findElements(By.id("static-notification")).size());
    }

    @Test
    public void notificationWithComponent() {
        clickElementWithJs(findElement(By.id("component-notification-button")));
        // findElement(By.id("component-notification-button")).click();
        checkNotificationIsOpen();
        Assert.assertEquals(1,
                findElements(By.id("component-notification")).size());

        Optional<WebElement> expectedNotification = getNotifications().stream()
                .filter(notificationElement -> notificationElement.getText()
                        .contains("Bye"))
                .findFirst();
        Assert.assertTrue("Expect to have a notification with 'Bye' word in it",
                expectedNotification.isPresent());
        WebElement notification = expectedNotification.get();

        Assert.assertEquals(1, notification
                .findElements(By.id("button-inside-notification")).size());
        Assert.assertEquals(1, notification
                .findElements(By.id("label-inside-notification")).size());
        notification.findElement(By.id("button-inside-notification")).click();
        checkNotificationIsClosed();
    }

    @Test
    public void styleNotificationContent() {
        scrollIntoViewAndClick(
                findElement(By.id("styled-content-notification-button")));

        WebElement content = null;
        for (WebElement notification : getNotifications()) {
            List<WebElement> nestedElements = notification
                    .findElements(By.className("my-style"));
            if (!nestedElements.isEmpty()) {
                content = nestedElements.get(0);
                break;
            }
        }

        Assert.assertNotNull("Notification content element is not found",
                content);
        Assert.assertEquals("rgba(255, 0, 0, 1)", content.getCssValue("color"));
    }

    private void assertNotificationContent(String expected) {
        List<String> notifications = getNotifications().stream()
                .map(WebElement::getText).collect(Collectors.toList());
        Assert.assertTrue(String.format(
                "Expected any of the notifications to contain the string '%s' but neither of them did. Notifications: '%s'",
                expected, notifications),
                notifications.stream().anyMatch(
                        notification -> notification.contains(expected)));
    }

    private List<WebElement> getNotifications() {
        return findElements(By.tagName(NOTIFICATION_TAG));
    }

    private void checkNotificationIsClosed() {
        waitForElementNotPresent(By.tagName(NOTIFICATION_TAG));
    }

    private void checkNotificationIsOpen() {
        waitForElementPresent(By.tagName(NOTIFICATION_TAG));
    }
}
