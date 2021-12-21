/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.component.applayout.test;

import com.vaadin.flow.component.applayout.testbench.AppLayoutElement;
import com.vaadin.tests.AbstractParallelTest;

import org.openqa.selenium.By;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;

public class AppLayoutI18nIT extends AbstractParallelTest {
    private AppLayoutElement layout;

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-app-layout/i18n");
        getDriver().get(url);
        layout = $(AppLayoutElement.class).waitForFirst();
    }

    @Test
    public void setEmptyI18n_defaultI18nIsNotOverriden() {
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
