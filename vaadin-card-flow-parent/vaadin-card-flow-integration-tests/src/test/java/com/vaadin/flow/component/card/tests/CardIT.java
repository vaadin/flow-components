/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.card.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.card.testbench.CardElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-card")
public class CardIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void rendersCardComponent() {
        CardElement card = $(CardElement.class).waitForFirst();

        boolean hasShadowRoot = (Boolean) executeScript(
                "return arguments[0].shadowRoot !== null", card);
        String componentName = (String) executeScript(
                "return Object.getPrototypeOf(arguments[0]).constructor.is",
                card);

        Assert.assertTrue(hasShadowRoot);
        Assert.assertEquals("vaadin-card", componentName);
    }
}
