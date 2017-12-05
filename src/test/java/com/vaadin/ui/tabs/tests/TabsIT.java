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

package com.vaadin.ui.tabs.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.demo.ComponentDemoTest;
import com.vaadin.testbench.By;
import com.vaadin.ui.tabs.demo.TabsView;
/**
 * Integration tests for the {@link TabsView}.
 *
 * @author Vaadin Ltd.
 */
public class TabsIT extends ComponentDemoTest {

    @Test
    public void pageGetsDisplayedWhenAssociatedTabIsSelected() {
        WebElement tab3 = layout.findElement(By.id("tab3"));
        WebElement page1 = layout.findElement(By.id("page1"));
        WebElement page3 = layout.findElement(By.id("page3"));
        assertThat(page1.getCssValue("display"), is("block"));
        assertThat(page3.getCssValue("display"), is("none"));

        scrollIntoViewAndClick(tab3);

        waitUntil(driver -> page1.getCssValue("display").equals("none"));
        assertThat(page3.getCssValue("display"), is("block"));
    }

    @Override
    protected String getTestPath() {
        return "/vaadin-tabs";
    }
}
