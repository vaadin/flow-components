/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.data.renderer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;

public class LitRendererTest {

    @Before
    public void setup() {
        UI ui = new UI();
        UI.setCurrent(ui);
    }

    @Test(expected = IllegalArgumentException.class)
    public void doNotAllowFunctionNamesWithFunctions() {
        LitRenderer.of("").withFunction("foo=0; alert(\"gotcha\"); const bar",
                item -> {
                });
    }

    @Test(expected = IllegalArgumentException.class)
    public void doNotAllowFunctionNamesWithSpaces() {
        LitRenderer.of("").withFunction("illegal name", item -> {
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void doNotAllowFunctionNamesWithDots() {
        LitRenderer.of("").withFunction("illegal.name", item -> {
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void doNotAllowFunctionNamesWithParenthesis() {
        LitRenderer.of("").withFunction("illegalname()", item -> {
        });
    }

    @Test
    public void allowAlphaNumericFunctionNames() {
        LitRenderer.of("<div></div>").withFunction("legalName1", item -> {
        });
    }

    @Test
    public void supportGettingValueProviders() {
        LitRenderer<?> renderer = LitRenderer.of("<div></div>")
                .withProperty("foo", item -> 1).withProperty("bar", item -> 2);

        Assert.assertTrue(
                renderer.getValueProviders().keySet().contains("foo"));
        Assert.assertTrue(
                renderer.getValueProviders().keySet().contains("bar"));
        Assert.assertTrue(renderer.getValueProviders().size() == 2);
    }

}
