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
package com.vaadin.flow.component.applayout.tests;

import static com.vaadin.flow.component.applayout.tests.AppRouterLayout.CUSTOM_ICON_ID;
import static com.vaadin.flow.component.applayout.tests.AppRouterLayout.CUSTOM_TOGGLE_ID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.applayout.testbench.AppLayoutElement;
import com.vaadin.flow.component.applayout.testbench.DrawerToggleElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-app-layout")
public class AppLayoutIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void content() {
        final AppLayoutElement layout = $(AppLayoutElement.class)
                .waitForFirst();
        Assert.assertEquals("Welcome home", layout.getContent().getText());

        Assert.assertNotNull(layout.getDrawerToggle());

        layout.$("a").attribute("href", "vaadin-app-layout/Page1").first()
                .click();
        Assert.assertEquals("This is Page 1", $(AppLayoutElement.class)
                .waitForFirst().getContent().getText());

        layout.$("a").attribute("href", "vaadin-app-layout/Page2").first()
                .click();
        Assert.assertEquals("This is Page 2", $(AppLayoutElement.class)
                .waitForFirst().getContent().getText());
    }

    @Test
    public void properties() {
        final AppLayoutElement layout = $(AppLayoutElement.class)
                .waitForFirst();
        Assert.assertEquals(true, layout.isDrawerOpened());
        Assert.assertEquals(false, layout.isDrawerFirst());
        Assert.assertEquals(false, layout.isOverlay());
    }

    @Test
    public void navigateToNotFound() {
        String url = getRootURL() + getTestPath() + "/nonexistingpage";
        getDriver().get(url);
        Assert.assertTrue($(AppLayoutElement.class).waitForFirst().getContent()
                .getText().contains("Could not navigate to"));

    }

    @Test
    public void customIcon() {
        AppLayoutElement layout = $(AppLayoutElement.class).waitForFirst();
        Assert.assertEquals(2,
                layout.$(DrawerToggleElement.class).all().size());

        DrawerToggleElement customToggle = layout.$(DrawerToggleElement.class)
                .id(CUSTOM_TOGGLE_ID);
        Assert.assertTrue(customToggle.isDisplayed());

        TestBenchElement iconElement = customToggle.$("vaadin-icon")
                .id(CUSTOM_ICON_ID);
        Assert.assertTrue(iconElement.isDisplayed());
    }
}
