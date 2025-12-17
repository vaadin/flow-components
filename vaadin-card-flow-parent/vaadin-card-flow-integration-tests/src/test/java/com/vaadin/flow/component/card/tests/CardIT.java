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

        var title = card.getTitle();
        Assert.assertNotNull(title);
        Assert.assertEquals("Title", title.getText());

        var subtitle = card.getSubtitle();
        Assert.assertNotNull(subtitle);
        Assert.assertEquals("Subtitle", subtitle.getText());

        var media = card.getMedia();
        Assert.assertNotNull(media);
        Assert.assertEquals("https://vaadin.com/images/vaadin-logo.svg",
                media.getAttribute("src"));

        var headerPrefix = card.getHeaderPrefix();
        Assert.assertNotNull(headerPrefix);
        Assert.assertEquals("Header prefix", headerPrefix.getText());

        var headerSuffix = card.getHeaderSuffix();
        Assert.assertNotNull(headerSuffix);
        Assert.assertEquals("Header suffix", headerSuffix.getText());

        var footerContents = card.getFooterContents();
        Assert.assertEquals(2, footerContents.size());
        Assert.assertEquals("Footer text", footerContents.get(0).getText());
        Assert.assertEquals("Interactive Footer Content",
                footerContents.get(1).getText());

        var contents = card.getContents();
        Assert.assertEquals(2, contents.size());
        Assert.assertEquals("Content text", contents.get(0).getText());
        Assert.assertEquals("Interactive Content", contents.get(1).getText());
    }
}
