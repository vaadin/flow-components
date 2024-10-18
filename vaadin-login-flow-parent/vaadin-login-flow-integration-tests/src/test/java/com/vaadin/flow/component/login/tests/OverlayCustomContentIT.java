/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.login.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-login/custom-content")
public class OverlayCustomContentIT extends AbstractComponentIT {

    private static final String LOGIN_OVERLAY_WRAPPER_TAG = "vaadin-login-overlay-wrapper";

    private NativeButtonElement openOverlay;
    private NativeButtonElement addFooter;
    private NativeButtonElement removeFooter;
    private NativeButtonElement addCustomFormArea;
    private NativeButtonElement removeCustomFormArea;

    @Before
    public void init() {
        open();
        openOverlay = $(NativeButtonElement.class).id("open-overlay-btn");
        addFooter = $(NativeButtonElement.class).id("add-footer-btn");
        removeFooter = $(NativeButtonElement.class).id("remove-footer-btn");
        addCustomFormArea = $(NativeButtonElement.class)
                .id("add-custom-form-btn");
        removeCustomFormArea = $(NativeButtonElement.class)
                .id("remove-custom-form-btn");
    }

    @Test
    public void addFooter_openOverlay_contentIsRendered() {
        addFooter.click();
        openOverlay.click();
        verifyOverlayOpened();
        assertOverlayContains(OverlayCustomContentPage.FOOTER_CONTENT);
    }

    @Test
    public void openOverlay_addFooter_contentIsNotRendered() {
        openOverlay.click();
        addFooter.click();
        verifyOverlayOpened();
        assertOverlayNotContains(OverlayCustomContentPage.FOOTER_CONTENT);
    }

    @Test
    public void addAndRemoveFooter_openOverlay_contentIsNotRendered() {
        addFooter.click();
        removeFooter.click();
        openOverlay.click();
        verifyOverlayOpened();
        assertOverlayNotContains(OverlayCustomContentPage.FOOTER_CONTENT);
    }

    @Test
    public void addCustomFormArea_openOverlay_contentIsRendered() {
        addCustomFormArea.click();
        openOverlay.click();
        verifyOverlayOpened();
        assertOverlayContains(OverlayCustomContentPage.CUSTOM_FORM_CONTENT);
    }

    @Test
    public void openOverlay_addCustomFormArea_contentIsNotRendered() {
        openOverlay.click();
        addCustomFormArea.click();
        verifyOverlayOpened();
        assertOverlayNotContains(OverlayCustomContentPage.CUSTOM_FORM_CONTENT);
    }

    @Test
    public void addAndRemoveCustomFormArea_openOverlay_contentIsNotRendered() {
        addCustomFormArea.click();
        removeCustomFormArea.click();
        openOverlay.click();
        verifyOverlayOpened();
        assertOverlayNotContains(OverlayCustomContentPage.CUSTOM_FORM_CONTENT);
    }

    private void assertOverlayContains(String text) {
        var wrapper = getOverlayWrapper();
        Assert.assertTrue("Overlay should contain text " + text,
                wrapper.getText().contains(text));
    }

    private void assertOverlayNotContains(String text) {
        var wrapper = getOverlayWrapper();
        Assert.assertFalse("Overlay should not contain text " + text,
                wrapper.getText().contains(text));
    }

    private void verifyOverlayOpened() {
        waitForElementPresent(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));
    }

    private WebElement getOverlayWrapper() {
        return findElement(By.tagName(LOGIN_OVERLAY_WRAPPER_TAG));
    }
}
