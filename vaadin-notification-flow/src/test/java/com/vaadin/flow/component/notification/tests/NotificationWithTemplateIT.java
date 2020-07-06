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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("notification-template-test")
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
        WebElement overlay = findElement(
                By.tagName(NOTIFICATION_CONTAINER_TAG));
        WebElement template = overlay.findElement(By.id("template"));

        WebElement btn = findInShadowRoot(template, By.id("btn")).get(0);
        WebElement container = findInShadowRoot(template, By.id("container"))
                .get(0);

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
