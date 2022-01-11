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
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-notification/notification-test")
public class NotificationTestPageIT extends AbstractComponentIT {

    private static final String NOTIFICATION_CARD_TAG = "vaadin-notification-card";

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-notification"))
                .size() > 0);
    }

    @Test
    public void notificationWithButtonControl() {
        findElement(By.id("notification-open")).click();
        checkNotificationIsOpen();
        assertButtonSize(0);
        clickElementWithJs(findElement(By.id("notification-close")));
        checkNotificationIsClose();
    }

    @Test
    public void twoNotificationAtOnce() {
        findElement(By.id("notification-button-1")).click();
        findElement(By.id("notification-button-2")).click();

        checkNotificationIsOpen();

        List<String> notifications = getNotifications().stream()
                .map(WebElement::getText).collect(Collectors.toList());
        Assert.assertEquals(
                "Expect to have two notification pop-ups for two notification buttons clicked",
                notifications.size(), 2);
        Assert.assertTrue("Expect to have the first notification shown",
                notifications.stream()
                        .anyMatch(text -> text.contains("1111111")));
        Assert.assertTrue("Expect to have the second notification shown",
                notifications.stream()
                        .anyMatch(text -> text.contains("2222222")));
    }

    @Test
    public void notificationAddComponents() {
        findElement(By.id("open-notification-button-add")).click();
        checkNotificationIsOpen();
        assertButtonSize(3);
        clickElementWithJs(findElement(By.id("close-notification-button-add")));
        checkNotificationIsClose();
    }

    @Test
    public void notificationRemoveComponents() {
        findElement(By.id("open-notification-button-remove")).click();
        checkNotificationIsOpen();
        assertButtonSize(2);
        clickElementWithJs(
                findElement(By.id("close-notification-button-remove")));
        checkNotificationIsClose();
    }

    @Test
    public void notificationRemoveAllComponents() {
        findElement(By.id("open-notification-button-remove-all")).click();
        checkNotificationIsOpen();
        assertButtonSize(0);
        clickElementWithJs(
                findElement(By.id("close-notification-button-remove-all")));
        checkNotificationIsClose();
    }

    @Test
    public void notificationAddTwoComponents() {
        findElement(By.id("Add-two-components-open")).click();
        checkNotificationIsOpen();
        assertButtonSize(2);
        clickElementWithJs(findElement(By.id("add-two-components-close")));
        checkNotificationIsClose();
    }

    @Test
    public void notificationAddMix() {
        findElement(By.id("Add-Mix-open")).click();
        checkNotificationIsOpen();
        assertButtonSize(1);
        Assert.assertFalse(getNotifications().iterator().next().getText()
                .contains("5555555"));
        clickElementWithJs(findElement(By.id("add-Mix-close")));
        checkNotificationIsClose();
    }

    @Test
    public void notificationwithTextAndAddComponent() {
        findElement(By.id("component-add-text-open")).click();
        checkNotificationIsOpen();
        assertButtonSize(1);
        clickElementWithJs(findElement(By.id("component-add-text-close")));
        checkNotificationIsClose();
    }

    @Test
    public void notificationAddComponentAddText() {
        findElement(By.id("add-component-add-text-open")).click();
        checkNotificationIsOpen();
        assertButtonSize(0);
        assertNotificationContent("Moi");
        clickElementWithJs(findElement(By.id("add-component-add-text-close")));
        checkNotificationIsClose();
    }

    @Test
    public void notificationNotAttachedToThePage_openAndClose_notificationIsAttachedAndRemoved() {
        WebElement open = findElement(By.id("notification-outside-ui-open"));

        waitForElementNotPresent(By.id("notification-outside-ui"));
        open.click();
        waitForElementPresent(By.id("notification-outside-ui"));
        checkNotificationIsOpen();
        try {
            // wait for the notification to disappear
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        checkNotificationIsClose();
        waitForElementNotPresent(By.id("notification-outside-ui"));

        open.click();
        waitForElementPresent(By.id("notification-outside-ui"));
        checkNotificationIsOpen();
        WebElement close = findElement(By.id("notification-outside-ui-close"));
        close.click();
        checkNotificationIsClose();
        waitForElementNotPresent(By.id("notification-outside-ui"));
    }

    @Test
    public void notificationWithButtonControl_isNotRemovedOnClose() {
        List<WebElement> notification = findElements(
                By.id("notification-with-button-control"));
        Assert.assertEquals(1, notification.size());

        findElement(By.id("notification-open")).click();
        checkNotificationIsOpen();
        clickElementWithJs(findElement(By.id("notification-close")));
        checkNotificationIsClose();

        // since the notification was added manually in the DOM, it shouldn't be
        // removed after close
        notification = findElements(By.id("notification-with-button-control"));
        Assert.assertEquals(1, notification.size());
    }

    @Test
    public void openNotificationAddComponentAtFirst() {
        verifyInitialNotification(3);
        findElement(By.id("button-to-first")).click();
        assertButtonNumberInNotification(4);
        assertButtonText(0, "Added Button");
    }

    @Test
    public void openNotificationAddComponentAtIndex() {
        verifyInitialNotification(3);
        findElement(By.id("button-to-second")).click();
        assertButtonNumberInNotification(4);
        assertButtonText(1, "Added Button");
    }

    @Test
    public void addComponentToOpenedNotification() {
        waitForElementNotPresent(
                By.id("notification-add-component-after-open"));
        findElement(By.id("Open-notification-add-component")).click();
        waitForElementPresent(By.id("notification-add-component-after-open"));
        assertButtonNumberInNotification(4);
        findElement(By.id("Add-component-to-notification")).click();
        assertButtonNumberInNotification(5);
        assertButtonText(1, "text");
    }

    private void assertButtonText(int indexOfButton, String expectedText) {
        Assert.assertEquals("Button Text is not correct", expectedText,
                findElements(By.tagName(NOTIFICATION_CARD_TAG)).get(0)
                        .findElements(By.tagName("button")).get(indexOfButton)
                        .getText());
    }

    private void verifyInitialNotification(int initialNumber) {
        waitForElementNotPresent(By.id("notification-add-component-at-index"));
        findElement(By.id("open-notification-add-component-at-index")).click();
        waitForElementPresent(By.id("notification-add-component-at-index"));
        assertButtonNumberInNotification(initialNumber);
        findElement(By.id("close-notification-add-component-at-index")).click();
        waitForElementNotPresent(By.tagName(NOTIFICATION_CARD_TAG));
    }

    private void assertButtonNumberInNotification(int expectedButtonNumber) {
        Assert.assertEquals(
                "Number of buttons in the notification overlay is not correct.",
                expectedButtonNumber,
                findElement(By.tagName(NOTIFICATION_CARD_TAG))
                        .findElements(By.tagName("button")).size());
    }

    private void assertButtonSize(int number) {
        Assert.assertEquals(number, getNotifications().iterator().next()
                .findElements(By.tagName("button")).size());
    }

    private void checkNotificationIsClose() {
        waitForElementNotPresent(By.tagName(NOTIFICATION_CARD_TAG));
    }

    private void checkNotificationIsOpen() {
        waitForElementPresent(By.tagName(NOTIFICATION_CARD_TAG));
    }

    private void assertNotificationContent(String expected) {
        List<WebElement> notifications = getNotifications();
        Assert.assertEquals("Expect to have exactly one notification", 1,
                notifications.size());
        String content = notifications.iterator().next().getText();
        Assert.assertTrue(String.format(
                "Expected the notification to contain string '%s', but got '%s'",
                expected, content), content.contains(expected));
    }

    private List<WebElement> getNotifications() {
        return findElements(By.tagName(NOTIFICATION_CARD_TAG));
    }
}
