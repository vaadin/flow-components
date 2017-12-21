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
    public void NotificationWithButtonControl() {
        findElement(By.id("notification-open")).click();
        waitUntil(driver -> Boolean.TRUE.toString()
                .equals(findElement(By.tagName(DIALOG_OVERLAY_TAG))
                        .getAttribute("opened")));
        Assert.assertEquals(1,
                findElements(By.id("notification-with-button-control")).size());

        clickElementWithJs(findElement(By.id("notification-close")));
        waitUntil(driver -> Boolean.FALSE.toString()
                .equals(findElement(By.tagName(DIALOG_OVERLAY_TAG))
                        .getAttribute("opened")));
    }

    @Test
    public void TwoNotificitonAtSamePosition() {
        findElement(By.id("notification-button-1")).click();
        clickElementWithJs(findElement(By.id("notification-button-2")));
        waitUntil(driver -> Boolean.TRUE.toString()
                .equals(findElement(By.tagName(DIALOG_OVERLAY_TAG))
                        .getAttribute("opened")));
        assertNotificationOverlayContent("1111111");
        assertNotificationOverlayContent("2222222");
    }

    @Test
    public void NotificitonAddComponents() {
        findElement(By.id("open-notification-button-add")).click();
        waitUntil(driver -> Boolean.TRUE.toString()
                .equals(findElement(By.tagName(DIALOG_OVERLAY_TAG))
                        .getAttribute("opened")));
        Assert.assertEquals(3,
                getOverlayContent().findElements(By.tagName("button")).size());

        clickElementWithJs(findElement(By.id("close-notification-button-add")));
        waitUntil(driver -> Boolean.FALSE.toString()
                .equals(findElement(By.tagName(DIALOG_OVERLAY_TAG))
                        .getAttribute("opened")));
    }

    @Test
    public void NotificitonRemoveComponents() {
        findElement(By.id("open-notification-button-remove")).click();
        waitUntil(driver -> Boolean.TRUE.toString()
                .equals(findElement(By.tagName(DIALOG_OVERLAY_TAG))
                        .getAttribute("opened")));
        Assert.assertEquals(2,
                getOverlayContent().findElements(By.tagName("button")).size());

        clickElementWithJs(
                findElement(By.id("close-notification-button-remove")));
        waitUntil(driver -> Boolean.FALSE.toString()
                .equals(findElement(By.tagName(DIALOG_OVERLAY_TAG))
                        .getAttribute("opened")));
    }

    @Test
    public void NotificitonRemoveAllComponents() {
        findElement(By.id("open-notification-button-remove-all")).click();
        waitUntil(driver -> Boolean.TRUE.toString()
                .equals(findElement(By.tagName(DIALOG_OVERLAY_TAG))
                        .getAttribute("opened")));
        Assert.assertEquals(0,
                getOverlayContent().findElements(By.tagName("button")).size());

        clickElementWithJs(
                findElement(By.id("close-notification-button-remove-all")));
        waitUntil(driver -> Boolean.FALSE.toString()
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
