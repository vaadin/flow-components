package com.vaadin.flow.theme.lumo;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

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
