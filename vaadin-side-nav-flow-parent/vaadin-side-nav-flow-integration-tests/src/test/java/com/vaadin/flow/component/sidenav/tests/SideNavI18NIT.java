/*
 * Copyright 2023 Vaadin Ltd.
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

package com.vaadin.flow.component.sidenav.tests;

import com.vaadin.flow.component.sidenav.testbench.SideNavItemElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Integration tests for the {@link SideNavI18NPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-side-nav/side-nav-i18n")
public class SideNavI18NIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void itemsHaveCorrectDefaultI18N() {
        List<SideNavItemElement> elements = $(SideNavItemElement.class).all();
        elements.forEach(element -> Assert.assertEquals("Toggle child items",
                getI18nText(element)));
    }

    @Test
    public void setI18n_i18nIsUpdated() {
        $("button").id("set-i18n").click();

        List<SideNavItemElement> elements = $(SideNavItemElement.class).all();
        elements.forEach(element -> Assert.assertEquals("Updated",
                getI18nText(element)));
    }

    private String getI18nText(SideNavItemElement element) {
        return element.getWrappedElement().getShadowRoot()
                .findElement(By.id("i18n")).getText();
    }
}
