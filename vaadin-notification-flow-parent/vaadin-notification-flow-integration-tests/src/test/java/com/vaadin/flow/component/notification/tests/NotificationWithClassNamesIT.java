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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-notification/notification-class-names-test")
public class NotificationWithClassNamesIT extends AbstractComponentIT {

    public static final String NOTIFICATION_CARD = "vaadin-notification-card";

    private ButtonElement openNotification;
    private ButtonElement closeNotification;
    private ButtonElement openOtherNotification;
    private ButtonElement addClassName;
    private ButtonElement clearClassNames;

    @Before
    public void init() {
        open();
        openNotification = $(ButtonElement.class).id("open-notification-btn");
        closeNotification = $(ButtonElement.class).id("close-notification-btn");
        openOtherNotification = $(ButtonElement.class)
                .id("open-other-notification-btn");
        addClassName = $(ButtonElement.class).id("add-class-btn");
        clearClassNames = $(ButtonElement.class).id("clear-classes-btn");
    }

    @Test
    public void openNotification_cardHasSameClassNames() {
        openNotification.click();
        waitForElementPresent(By.tagName(NOTIFICATION_CARD));

        NotificationElement notification = $(NotificationElement.class).first();
        assertClassAttribute(notification, "custom");
    }

    @Test
    public void openNotification_cardChangeClassName() {
        openNotification.click();
        waitForElementPresent(By.tagName(NOTIFICATION_CARD));

        addClassName.click();

        NotificationElement notification = $(NotificationElement.class).first();
        assertClassAttribute(notification, "custom added");
    }

    @Test
    public void openNotification_cardNoClassNameAfterClearClassName() {

        openNotification.click();
        waitForElementPresent(By.tagName(NOTIFICATION_CARD));

        clearClassNames.click();

        NotificationElement notification = $(NotificationElement.class).first();
        assertClassAttribute(notification, "");
    }

    @Test
    public void openNotification_cardChagedClassNameAfterSecondOpening() {
        openNotification.click();
        waitForElementPresent(By.tagName(NOTIFICATION_CARD));

        clearClassNames.click();
        addClassName.click();

        closeNotification.click();
        waitForElementNotPresent(By.tagName(NOTIFICATION_CARD));

        openNotification.click();
        waitForElementPresent(By.tagName(NOTIFICATION_CARD));

        NotificationElement notification = $(NotificationElement.class).first();
        assertClassAttribute(notification, "added");
    }

    @Test
    public void openTwoNotification_CorrectClassNamesApplied() {
        openNotification.click();
        waitForElementPresent(By.tagName(NOTIFICATION_CARD));

        openOtherNotification.click();

        waitForElementPresent(
                By.cssSelector("vaadin-notification-card.custom"));
        waitForElementPresent(By.cssSelector("vaadin-notification-card.other"));

        addClassName.click();

        NotificationElement notification = $(NotificationElement.class).first();
        assertClassAttribute(notification, "custom added");

        NotificationElement otherNotification = $(NotificationElement.class)
                .get(1);
        assertClassAttribute(otherNotification, "other");
    }

    private void assertClassAttribute(TestBenchElement notification,
            String expected) {
        String className = notification.getAttribute("class");
        Assert.assertEquals(expected, className);

        TestBenchElement card = (TestBenchElement) notification.getContext();
        String cardClassName = card.getAttribute("class");
        Assert.assertEquals(expected, cardClassName);
    }
}
