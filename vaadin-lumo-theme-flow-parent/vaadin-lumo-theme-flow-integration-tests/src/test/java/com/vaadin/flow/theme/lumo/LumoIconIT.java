/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
                .attribute("icon", iconName);

        Assert.assertTrue("Could not find icon: " + iconName, icon.exists());
    }
}
