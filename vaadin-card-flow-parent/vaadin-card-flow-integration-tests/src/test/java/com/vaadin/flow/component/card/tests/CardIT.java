/*
 * Copyright 2000-2025 Vaadin Ltd.
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

    private CardElement card;

    @Before
    public void init() {
        open();
        card = $(CardElement.class).waitForFirst();
    }

    @Test
    public void rendersCardComponent() {
        boolean hasShadowRoot = (Boolean) executeScript(
                "return arguments[0].shadowRoot !== null", card);
        String componentName = (String) executeScript(
                "return Object.getPrototypeOf(arguments[0]).constructor.is",
                card);

        Assert.assertTrue(hasShadowRoot);
        Assert.assertEquals("vaadin-card", componentName);
        Assert.assertNotNull(card.getTitle());
        Assert.assertNotNull(card.getSubtitle());
        Assert.assertNotNull(card.getMedia());
        Assert.assertNotNull(card.getHeaderPrefix());
        Assert.assertNotNull(card.getHeaderSuffix());
        Assert.assertFalse(card.getFooterContents().isEmpty());
        Assert.assertFalse(card.getContents().isEmpty());
    }

    @Test
    public void setTitleComponent_setTitleString_titleComponentIsRemoved() {
        clickElementWithJs("set-title-component");
        var titleComponent = card.getTitle();
        clickElementWithJs("set-string-title");
        Assert.assertNotEquals(titleComponent, card.getTitle());
        Assert.assertEquals("String title", card.getStringTitle());
    }

    @Test
    public void setStringTitle_setTitleComponent_stringTitleIsRemoved() {
        clickElementWithJs("set-string-title");
        var stringTitleComponent = card.getTitle();
        clickElementWithJs("set-title-component");
        Assert.assertNotEquals(stringTitleComponent, card.getTitle());
        Assert.assertNull(card.getStringTitle());
    }
}
