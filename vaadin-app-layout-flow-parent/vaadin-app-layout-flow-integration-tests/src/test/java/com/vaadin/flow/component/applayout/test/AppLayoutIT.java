package com.vaadin.flow.component.applayout.test;

import static com.vaadin.flow.component.applayout.examples.AppRouterLayout.CUSTOM_ICON_ID;
import static com.vaadin.flow.component.applayout.examples.AppRouterLayout.CUSTOM_TOGGLE_ID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.applayout.testbench.AppLayoutElement;
import com.vaadin.flow.component.applayout.testbench.DrawerToggleElement;
import com.vaadin.testbench.TestBenchElement;

public class AppLayoutIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void content() {
        final AppLayoutElement layout = $(AppLayoutElement.class)
            .waitForFirst();
        Assert
            .assertEquals("Welcome home", layout.getContent().getText());

        Assert.assertNotNull(layout.getDrawerToggle());

        layout.$("a").attribute("href", "Page1").first().click();
        Assert.assertEquals("This is Page 1",
            $(AppLayoutElement.class).waitForFirst().getContent()
                .getText());

        layout.$("a").attribute("href", "Page2").first().click();
        Assert.assertEquals("This is Page 2",
            $(AppLayoutElement.class).waitForFirst().getContent()
                .getText());
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
        getDriver().get(getBaseURL() + "/nonexistingpage");
        Assert.assertTrue(
            $(AppLayoutElement.class).waitForFirst().getContent()
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

        TestBenchElement iconElement = customToggle.$("iron-icon")
                .id(CUSTOM_ICON_ID);
        Assert.assertTrue(iconElement.isDisplayed());
    }
}
