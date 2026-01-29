/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.button.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-button/tooltip-markdown")
public class TooltipMarkdownIT extends AbstractComponentIT {

    private TestBenchElement tooltipContent;

    @Before
    public void init() {
        open();
        tooltipContent = $(ButtonElement.class).first().$("vaadin-tooltip")
                .first().$("div").withAttribute("slot", "overlay").first();
    }

    @Test
    public void initialContent_renderedAsText() {
        Assert.assertEquals("Initial tooltip",
                tooltipContent.getPropertyString("innerHTML"));
    }

    @Test
    public void setMarkdownText_renderedAsMarkdown() {
        $("button").id("set-markdown-tooltip").click();

        waitForElementPresent(By.tagName("strong"));

        Assert.assertEquals("<p><strong>Markdown</strong> <em>tooltip</em></p>",
                tooltipContent.getPropertyString("innerHTML").strip());
    }

    @Test
    public void switchBackToText_renderedAsText() {
        $("button").id("set-markdown-tooltip").click();

        waitForElementPresent(By.tagName("strong"));

        $("button").id("set-text-tooltip").click();

        Assert.assertEquals("**Plain text** _tooltip_",
                tooltipContent.getPropertyString("innerHTML"));
    }
}
