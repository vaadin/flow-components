
package com.vaadin.flow.component.tabs.tests;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.tabs.demo.TabsView;
import com.vaadin.tests.ComponentDemoTest;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

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
        assertFalse(isElementPresent(By.id("page3")));
        assertThat(page1.getCssValue("display"), is("block"));

        scrollIntoViewAndClick(tab3);

        assertFalse(isElementPresent(By.id("page1")));
        WebElement page3 = layout.findElement(By.id("page3"));
        assertThat(page3.getCssValue("display"), is("block"));
    }

    @Test
    public void assertThemeVariant() {
        verifyThemeVariantsBeingToggled();
    }

    @Override
    protected String getTestPath() {
        return "/vaadin-tabs";
    }
}
