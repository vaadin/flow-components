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

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.component.login.testbench.LoginOverlayElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-login/overlay-class-name")
public class OverlayClassNameIT extends AbstractComponentIT {

    private static final String LOGIN_OVERLAY_WRAPPER_TAG = "vaadin-login-overlay-wrapper";

    private NativeButtonElement openOverlay;
    private NativeButtonElement closeOverlay;
    private NativeButtonElement addClassName;
    private NativeButtonElement clearClassNames;

    @Before
    public void init() {
        open();
        openOverlay = $(NativeButtonElement.class).id("open-overlay-btn");
        closeOverlay = $(NativeButtonElement.class).id("close-overlay-btn");
        addClassName = $(NativeButtonElement.class).id("add-class-btn");
        clearClassNames = $(NativeButtonElement.class).id("clear-classes-btn");
    }

    @Test
    public void openLoginOverlay_overlayWrapperHasSameClassNames() {
        openOverlay.click();
        waitForElementPresent(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));

        LoginOverlayElement overlay = $(LoginOverlayElement.class).first();
        assertClassAttribute(overlay, "custom");
    }

    @Test
    public void openLoginOverlay_overlayWrapperChangeClassName() {
        openOverlay.click();
        waitForElementPresent(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));

        addClassName.click();

        LoginOverlayElement overlay = $(LoginOverlayElement.class).first();
        assertClassAttribute(overlay, "custom added");
    }

    @Test
    public void openLoginOverlay_overlayWrapperNoClassNameAfterClearClassName() {
        openOverlay.click();
        waitForElementPresent(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));

        clearClassNames.click();

        LoginOverlayElement overlay = $(LoginOverlayElement.class).first();
        assertClassAttribute(overlay, "");
    }

    @Test
    public void openLoginOverlay_overlayWrapperChangedClassNameAfterSecondOpening() {
        openOverlay.click();
        waitForElementPresent(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));

        clearClassNames.click();
        addClassName.click();

        closeOverlay.click();
        waitForElementNotPresent(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));

        openOverlay.click();
        waitForElementPresent(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));

        LoginOverlayElement overlay = $(LoginOverlayElement.class).first();
        assertClassAttribute(overlay, "added");
    }

    private void assertClassAttribute(LoginOverlayElement overlay,
            String expected) {
        String className = overlay.getAttribute("class");
        Assert.assertEquals(expected, className);

        WebElement wrappedElement = overlay.getLoginOverlayWrapper();
        String cardClassName = wrappedElement.getAttribute("class");
        Assert.assertEquals(expected, cardClassName);
    }
}
