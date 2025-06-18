/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.applayout.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.applayout.testbench.AppLayoutElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-app-layout/i18n")
public class AppLayoutI18nIT extends AbstractComponentIT {
    private AppLayoutElement layout;

    @Before
    public void init() {
        open();
        layout = $(AppLayoutElement.class).first();
    }

    @Test
    public void setEmptyI18n_defaultI18nIsNotOverridden() {
        clickButton("set-empty-i18n");

        Assert.assertNotNull(
                "The i18n drawer property should contain the default value",
                layout.getPropertyString("i18n", "drawer"));
    }

    @Test
    public void setI18n_i18nIsUpdated() {
        clickButton("set-i18n");

        Assert.assertEquals(
                "The i18n drawer property should contain a custom value",
                "Custom drawer", layout.getPropertyString("i18n", "drawer"));
    }

    @Test
    public void setI18n_detach_attach_i18nIsPersisted() {
        clickButton("set-i18n");
        clickButton("toggle-attached");
        clickButton("toggle-attached");

        layout = $(AppLayoutElement.class).first();

        Assert.assertEquals(
                "The i18n drawer property should contain a custom value",
                "Custom drawer", layout.getPropertyString("i18n", "drawer"));
    }

    private void clickButton(String id) {
        $("button").id(id).click();
    }
}
