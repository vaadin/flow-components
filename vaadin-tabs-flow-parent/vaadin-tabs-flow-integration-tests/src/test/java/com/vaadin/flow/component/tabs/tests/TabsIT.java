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

package com.vaadin.flow.component.tabs.tests;

import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchTestCase;

/**
 * Integration tests for the {@link TabsPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-tabs/tabs")
public class TabsIT extends AbstractComponentIT {

    private TestBenchTestCase layout;

    @Before
    public void init() {
        layout = this;
        open();
    }

    @Test
    public void pageGetsDisplayedWhenAssociatedTabIsSelected() {
        WebElement tab3 = layout.findElement(By.id("tab3"));
        WebElement page1 = layout.findElement(By.id("page1"));
        assertFalse(isElementPresent(By.id("page3")));
        assertThat(page1.getCssValue("display"), is("block"));

        scrollIntoViewAndClick(tab3);

        assertFalse(isElementPresent(By.id("page1")));
        WebElement page3 = layout.findElement(By.id("page3"));
        assertThat(page3.getCssValue("display"), is("block"));
    }

    @Test
    public void assertThemeVariant() {
        WebElement tabs = findElement(By.id("tabs-with-theme"));
        scrollToElement(tabs);
        Assert.assertEquals(TabsVariant.LUMO_SMALL.getVariantName(),
                tabs.getAttribute("theme"));

        findElement(By.id("remove-theme-variant-button")).click();
        Assert.assertNull(tabs.getAttribute("theme"));
    }
}
