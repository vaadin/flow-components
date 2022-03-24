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
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-notification/notification-class-names-test")
public class NotificationWithClassNamesIT extends AbstractComponentIT {

    public static final String NOTIFICATION_CARD = "vaadin-notification-card";

    @Before
    public void init() {
        open();
    }

    @Test
    public void openNotification_cardHasSameClassNames() {
        findElement(By.id("open-notification-btn")).click();

        waitForElementPresent(By.tagName(NOTIFICATION_CARD));
        NotificationElement notification = $(NotificationElement.class).first();

        WebElement card = $(NOTIFICATION_CARD).first();

        String cardClassNames = card.getAttribute("class");
        String notificationClassNames = notification.getAttribute("class");

        Assert.assertEquals("custom", notificationClassNames);
        Assert.assertEquals("custom", cardClassNames);
    }

    @Test
    public void openNotification_cardChangeClassName() {
        findElement(By.id("open-notification-btn")).click();

        waitForElementPresent(By.tagName(NOTIFICATION_CARD));

        findElement(By.id("add-notification-btn")).click();

        WebElement card = $(NOTIFICATION_CARD).first();
        NotificationElement notification = $(NotificationElement.class).first();

        String cardClassNames = card.getAttribute("class");
        String notificationClassNames = notification.getAttribute("class");

        Assert.assertEquals("custom added", notificationClassNames);
        Assert.assertEquals("custom added", cardClassNames);
    }

    @Test
    public void openNotification_cardNoClassNameAfterClearClassName() {
        findElement(By.id("open-notification-btn")).click();

        waitForElementPresent(By.tagName(NOTIFICATION_CARD));

        findElement(By.id("clear-notification-btn")).click();

        WebElement card = $(NOTIFICATION_CARD).first();
        NotificationElement notification = $(NotificationElement.class).first();

        String cardClassNames = card.getAttribute("class");
        String notificationClassNames = notification.getAttribute("class");

        Assert.assertEquals("", notificationClassNames);
        Assert.assertEquals("", cardClassNames);
    }

    @Test
    public void openNotification_cardChagedClassNameAfterSecondOpening() {
        findElement(By.id("open-notification-btn")).click();

        waitForElementPresent(By.tagName(NOTIFICATION_CARD));

        findElement(By.id("clear-notification-btn")).click();
        findElement(By.id("add-notification-btn")).click();

        findElement(By.id("close-notification-btn")).click();
        waitForElementNotPresent(By.tagName(NOTIFICATION_CARD));

        findElement(By.id("open-notification-btn")).click();
        waitForElementPresent(By.tagName(NOTIFICATION_CARD));

        WebElement card = $(NOTIFICATION_CARD).first();
        NotificationElement notification = $(NotificationElement.class).first();

        String cardClassNames = card.getAttribute("class");
        String notificationClassNames = notification.getAttribute("class");

        Assert.assertEquals("added", notificationClassNames);
        Assert.assertEquals("added", cardClassNames);
    }

    @Test
    public void openTwoNotification_CorrectClassNamesApplied() {
        findElement(By.id("open-notification-btn")).click();
        findElement(By.id("open-other-notification-btn")).click();

        waitForElementPresent(By.className("custom"));
        waitForElementPresent(By.className("other"));

        findElement(By.id("add-notification-btn")).click();

        NotificationElement notification = $(NotificationElement.class).first();
        WebElement card = $(NOTIFICATION_CARD).first();

        NotificationElement otherNotification = $(NotificationElement.class)
                .get(1);
        WebElement otherCard = $(NOTIFICATION_CARD).get(1);

        String cardClassNames = card.getAttribute("class");
        String notificationClassNames = notification.getAttribute("class");

        Assert.assertEquals("custom added", notificationClassNames);
        Assert.assertEquals("custom added", cardClassNames);

        String otherCardClassNames = otherCard.getAttribute("class");
        String otherNotificationClassNames = otherNotification
                .getAttribute("class");

        Assert.assertEquals("other", otherCardClassNames);
        Assert.assertEquals("other", otherNotificationClassNames);
    }
}
