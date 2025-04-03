/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.theme.lumo;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-lumo-theme/lumo-icon-view")
public class LumoIconIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void allAvailableVaadinIcons() {
        List<WebElement> icons = findElements(By.tagName("vaadin-icon"));
        // Check total size
        Assert.assertEquals(LumoIcon.values().length, icons.size());
        // Check a few samples
        assertIconExists("lumo:align-center");
        assertIconExists("lumo:angle-right");
        assertIconExists("lumo:clock");
        assertIconExists("lumo:unordered-list");
        assertIconExists("lumo:user");
    }

    private void assertIconExists(String iconName) {
        ElementQuery<TestBenchElement> icon = $(TestBenchElement.class)
                .withAttribute("icon", iconName);

        Assert.assertTrue("Could not find icon: " + iconName, icon.exists());
    }
}
