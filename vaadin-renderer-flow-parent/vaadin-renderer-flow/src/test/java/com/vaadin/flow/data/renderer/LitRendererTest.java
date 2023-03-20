
package com.vaadin.flow.data.renderer;

import com.vaadin.flow.component.UI;

import org.junit.Before;
import org.junit.Test;

public class LitRendererTest {

    @Before
    public void init() {
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

}
