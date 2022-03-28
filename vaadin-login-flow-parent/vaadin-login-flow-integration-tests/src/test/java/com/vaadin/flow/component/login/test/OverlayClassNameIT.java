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

package com.vaadin.flow.component.login.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.login.testbench.LoginOverlayElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-login/overlay-class-name")
public class OverlayClassNameIT extends AbstractComponentIT {

    private static final String LOGIN_OVERLAY_WRAPPER_TAG = "vaadin-login-overlay-wrapper";

    @Before
    public void init() {
        open();
    }

    @Test
    public void openDialog_overlayHasSameClassNames() {
        findElement(By.id("open")).click();

        waitForElementPresent(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));
        LoginOverlayElement overlay = $(LoginOverlayElement.class).first();

        WebElement overlayWrapper = $(LOGIN_OVERLAY_WRAPPER_TAG).first();

        String overlayClassNames = overlay.getAttribute("class");
        String wrapperClassNames = overlayWrapper.getAttribute("class");

        Assert.assertEquals("custom", overlayClassNames);
        Assert.assertEquals("custom", wrapperClassNames);
    }

    @Test
    public void openDialog_overlayChangeClassName() {
        findElement(By.id("open")).click();

        waitForElementPresent(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));

        findElement(By.id("add")).click();

        LoginOverlayElement overlay = $(LoginOverlayElement.class).first();
        WebElement overlayWrapper = $(LOGIN_OVERLAY_WRAPPER_TAG).first();

        String overlayClassNames = overlay.getAttribute("class");
        String wrapperClassNames = overlayWrapper.getAttribute("class");

        Assert.assertEquals("custom added", overlayClassNames);
        Assert.assertEquals("custom added", wrapperClassNames);
    }

    @Test
    public void openDialog_overlayNoClassNameAfterClearClassName() {
        findElement(By.id("open")).click();

        waitForElementPresent(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));

        findElement(By.id("clear")).click();

        LoginOverlayElement overlay = $(LoginOverlayElement.class).first();
        WebElement overlayWrapper = $(LOGIN_OVERLAY_WRAPPER_TAG).first();

        String overlayClassNames = overlay.getAttribute("class");
        String wrapperClassNames = overlayWrapper.getAttribute("class");

        Assert.assertEquals("", overlayClassNames);
        Assert.assertEquals("", wrapperClassNames);
    }

    @Test
    public void openDialog_overlayChagedClassNameAfterSecondOpening() {
        findElement(By.id("open")).click();

        waitForElementPresent(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));

        findElement(By.id("clear")).click();
        findElement(By.id("add")).click();

        $(LOGIN_OVERLAY_WRAPPER_TAG).first().click();

        findElement(By.id("open")).click();
        waitForElementPresent(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));

        LoginOverlayElement overlay = $(LoginOverlayElement.class).first();
        WebElement overlayWrapper = $(LOGIN_OVERLAY_WRAPPER_TAG).first();

        String overlayClassNames = overlay.getAttribute("class");
        String wrapperClassNames = overlayWrapper.getAttribute("class");

        Assert.assertEquals("added", overlayClassNames);
        Assert.assertEquals("added", wrapperClassNames);
    }
}
