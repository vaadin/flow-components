
package com.vaadin.flow.theme.lumo;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

public abstract class AbstractThemedTemplateIT extends AbstractComponentIT {

    @Test
    public void lumoThemeUsed_themedTemplateAndLumoThemeResourcesLoaded() {
        open();

        // check that all imported templates are available in the DOM
        TestBenchElement template = $(getTagName()).first();

        TestBenchElement div = template.$("div").first();

        Assert.assertEquals("Lumo themed Template", div.getText());

        // this is silly, but a concrete way to test that the lumo files are
        // imported by verifying that the lumo css variables introduced in the
        // files work
        Assert.assertEquals("color variables not applied",
                "rgba(224, 36, 26, 1)", div.getCssValue("color"));
        Assert.assertEquals("typography variables not applied", "40px",
                div.getCssValue("font-size"));
        Assert.assertEquals("sizing variables not applied",
                "36px solid rgb(0, 0, 0)", div.getCssValue("border"));
        Assert.assertEquals("spacing variables not applied", "12px 24px",
                div.getCssValue("margin"));
        Assert.assertEquals("style variables not applied", "20px",
                div.getCssValue("border-radius"));
        Assert.assertEquals("icons variables not applied", "lumo-icons",
                div.getCssValue("font-family"));
    }

    protected abstract String getTagName();

    protected abstract String getThemedTemplate();

}
