/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.By;

@TestPath("notification-test")
public class NotificationTestPageIT extends AbstractComponentIT {

    private static final String DIALOG_OVERLAY_TAG = "vaadin-notification-overlay";

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-notification"))
                .size() > 0);
    }
    @Test
    public void notificationWithButtonControl() {
        findElement(By.id("notification-open")).click();
        checkNotificaitonIsOpen();
        assertButtonSize(0);
        clickElementWithJs(findElement(By.id("notification-close")));
        checkNotificationIsClose();
    }

    @Test
    public void twoNotificitonAtSamePosition() {
        findElement(By.id("notification-button-1")).click();
        clickElementWithJs(findElement(By.id("notification-button-2")));
        checkNotificaitonIsOpen();
        assertNotificationOverlayContent("1111111");
        assertNotificationOverlayContent("2222222");
    }

    @Test
    public void notificitonAddComponents() {
        findElement(By.id("open-notification-button-add")).click();
        checkNotificaitonIsOpen();
        assertButtonSize(3);
        clickElementWithJs(findElement(By.id("close-notification-button-add")));
        checkNotificationIsClose();
    }

    @Test
    public void notificitonRemoveComponents() {
        findElement(By.id("open-notification-button-remove")).click();
        checkNotificaitonIsOpen();
        assertButtonSize(2);
        clickElementWithJs(
                findElement(By.id("close-notification-button-remove")));
        checkNotificationIsClose();
    }

    @Test
    public void notificitonRemoveAllComponents() {
        findElement(By.id("open-notification-button-remove-all")).click();
        checkNotificaitonIsOpen();
        assertButtonSize(0);
        clickElementWithJs(
                findElement(By.id("close-notification-button-remove-all")));
        checkNotificationIsClose();
    }

    @Test
    public void notificationAddTwoComponents() {
        findElement(By.id("Add-two-components-open")).click();
        checkNotificaitonIsOpen();
        assertButtonSize(2);
        clickElementWithJs(findElement(By.id("add-two-components-close")));
        checkNotificationIsClose();
    }

    @Test
    public void notificationAddMix() {
        findElement(By.id("Add-Mix-open")).click();
        checkNotificaitonIsOpen();
        assertButtonSize(1);
        Assert.assertFalse(getOverlayContent().getText().contains("5555555"));
        clickElementWithJs(findElement(By.id("add-Mix-close")));
        checkNotificationIsClose();
    }

    @Test
    public void notificationwithTextAndAddComponent() {
        findElement(By.id("component-add-text-open")).click();
        checkNotificaitonIsOpen();
        assertButtonSize(1);
        clickElementWithJs(findElement(By.id("component-add-text-close")));
        checkNotificationIsClose();
    }
    
    @Test
    public void notificationAddComponentAddText() {
        findElement(By.id("add-component-add-text-open")).click();
        checkNotificaitonIsOpen();
        assertButtonSize(0);
        assertNotificationOverlayContent("Moi");
        clickElementWithJs(findElement(By.id("add-component-add-text-close")));
        checkNotificationIsClose();
        
    }

    private void assertButtonSize(int number) {
        Assert.assertEquals(number,
                getOverlayContent().findElements(By.tagName("button")).size());
    }

    private void checkNotificationIsClose() {
        waitUntil(driver -> Boolean.FALSE.toString()
                .equals(findElement(By.tagName(DIALOG_OVERLAY_TAG))
                        .getAttribute("opened")));
    }

    private void checkNotificaitonIsOpen() {
        waitUntil(driver -> Boolean.TRUE.toString()
                .equals(findElement(By.tagName(DIALOG_OVERLAY_TAG))
                        .getAttribute("opened")));
    }

    private void assertNotificationOverlayContent(String expected) {
        String content = getOverlayContent().getText();
        Assert.assertTrue(content.contains(expected));
    }

    private WebElement getOverlayContent() {
        WebElement overlay = findElement(By.tagName(DIALOG_OVERLAY_TAG));
        return getInShadowRoot(overlay, By.id("content"));
    }

}
