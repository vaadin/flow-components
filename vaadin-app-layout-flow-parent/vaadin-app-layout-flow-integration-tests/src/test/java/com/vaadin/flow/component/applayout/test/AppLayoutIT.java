package com.vaadin.flow.component.applayout.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.applayout.testbench.AppLayoutElement;

public class AppLayoutIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void content() {
        Assert.assertEquals("Welcome home",
                $(AppLayoutElement.class).waitForFirst().getContent().getText());

        getDriver().get(getBaseURL() + "/Page1");
        Assert.assertEquals("This is Page 1",
            $(AppLayoutElement.class).waitForFirst().getContent().getText());

        getDriver().get(getBaseURL() + "/Page2");
        Assert.assertEquals("This is Page 2",
            $(AppLayoutElement.class).waitForFirst().getContent().getText());

    }

    @Test
    public void navigateToNotFound() {
        getDriver().get(getBaseURL() + "/nonexistingpage");
        Assert.assertTrue($(AppLayoutElement.class).waitForFirst().getContent()
                .getText().contains("Could not navigate to"));

    }
}
