/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.data.renderer;

import com.vaadin.flow.component.UI;

import org.junit.Assert;
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
