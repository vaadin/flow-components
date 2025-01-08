/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.cookieconsent.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-cookie-consent/styling")
public class StylingIT extends AbstractComponentIT {

    private NativeButtonElement addConsent;
    private NativeButtonElement addClassEdgeless;
    private NativeButtonElement setClassBottom;

    @Before
    public void init() {
        open();
        addConsent = $(NativeButtonElement.class).id("add-consent");
        addClassEdgeless = $(NativeButtonElement.class).id("add-edgeless");
        setClassBottom = $(NativeButtonElement.class).id("set-bottom");
    }

    @Test
    public void addClassBeforeAdd() {
        addClassEdgeless.click();

        addConsent.click();

        String classValue = getBannerClassName();
        Assert.assertTrue(classValue.contains("cc-theme-edgeless"));
    }

    @Test
    public void addClassAfterAdd() {
        addConsent.click();

        setClassBottom.click();

        String classValue = getBannerClassName();
        Assert.assertFalse(classValue.contains("cc-top"));
        Assert.assertTrue(classValue.contains("cc-bottom"));
    }

    private WebElement getConsentBanner() {
        return findElement(By.cssSelector("div[role='alert']"));
    }

    private String getBannerClassName() {
        return getConsentBanner().getAttribute("class");
    }
}
