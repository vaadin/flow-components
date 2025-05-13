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
package com.vaadin.flow.component.accordion.tests;

import static com.vaadin.flow.component.accordion.tests.BasicView.ACCORDION_EVENTS;
import static com.vaadin.flow.component.accordion.tests.BasicView.PANEL_EVENTS;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.accordion.testbench.AccordionElement;
import com.vaadin.flow.component.accordion.testbench.AccordionPanelElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-accordion")
public class BasicIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        // Close the initially opened panel, otherwise the panel can fire a
        // close event after opening a different panel, breaking some assertions
        $(AccordionElement.class).first().$(AccordionPanelElement.class).first()
                .click();
    }

    @Test
    public void accordionIsPresent() {
        Assert.assertTrue($(AccordionElement.class).exists());
    }

    @Test
    public void programmaticOpenByIndex() {
        getTestButton("1").click();
        Assert.assertEquals(1, $(AccordionElement.class).first()
                .getOpenedIndex().orElseThrow());
        Assert.assertEquals("Green opened", getLastEvent(ACCORDION_EVENTS));
        Assert.assertEquals("Panel Green opened", getLastEvent(PANEL_EVENTS));

        getTestButton("3").click();
        Assert.assertEquals(3, $(AccordionElement.class).first()
                .getOpenedIndex().orElseThrow());
        Assert.assertEquals("Blue opened", getLastEvent(ACCORDION_EVENTS));
        Assert.assertEquals("Panel Blue opened", getLastEvent(PANEL_EVENTS));
    }

    @Test
    public void programmaticOpenByPanel() {
        getTestButton("green").click();

        final AccordionPanelElement secondPanel = $(AccordionElement.class)
                .first().$(AccordionPanelElement.class).all().get(1);

        Assert.assertEquals(secondPanel, $(AccordionElement.class).first()
                .getOpenedPanel().orElse(null));

        Assert.assertEquals("Green opened", getLastEvent(ACCORDION_EVENTS));
        Assert.assertEquals("Panel Green opened", getLastEvent(PANEL_EVENTS));

        getTestButton("blue").click();
        Assert.assertEquals("Blue opened", getLastEvent(ACCORDION_EVENTS));
        Assert.assertEquals("Panel Blue opened", getLastEvent(PANEL_EVENTS));
    }

    @Test
    public void programmaticClose() {
        getTestButton("1").click();
        getTestButton("close").click();
        Assert.assertEquals("Accordion closed", getLastEvent(ACCORDION_EVENTS));
    }

    @Test
    public void userOpen() {
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last()
                .click();
        Assert.assertEquals("Panel Blue opened", getLastEvent(PANEL_EVENTS));
    }

    @Test
    public void userClose() {
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last()
                .click();
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last()
                .click();
        Assert.assertEquals("Accordion closed", getLastEvent(ACCORDION_EVENTS));
    }

    @Test
    public void panelSummaryText() {
        Assert.assertEquals("Blue", $(AccordionElement.class).first()
                .$(AccordionPanelElement.class).last().getSummaryText());
    }

    @Test
    public void openPanel() {
        Assert.assertFalse($(AccordionElement.class).first()
                .$(AccordionPanelElement.class).last().isOpened());
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last()
                .open();
        Assert.assertTrue($(AccordionElement.class).first()
                .$(AccordionPanelElement.class).last().isOpened());
    }

    @Test
    public void closePanel() {
        Assert.assertFalse($(AccordionElement.class).first()
                .$(AccordionPanelElement.class).last().isOpened());
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last()
                .close();
        Assert.assertFalse($(AccordionElement.class).first()
                .$(AccordionPanelElement.class).last().isOpened());

        $(AccordionElement.class).first().$(AccordionPanelElement.class).last()
                .open();
        Assert.assertTrue($(AccordionElement.class).first()
                .$(AccordionPanelElement.class).last().isOpened());

        $(AccordionElement.class).first().$(AccordionPanelElement.class).last()
                .close();
        Assert.assertFalse($(AccordionElement.class).first()
                .$(AccordionPanelElement.class).last().isOpened());
    }

    @Test
    public void removePanel() {
        final int initialCount = $(AccordionElement.class).first()
                .$(AccordionPanelElement.class).all().size();

        Assert.assertEquals("Red", $(AccordionElement.class).first()
                .$(AccordionPanelElement.class).first().getSummaryText());

        getTestButton("removeRed").click();

        Assert.assertEquals(initialCount - 1, $(AccordionElement.class).first()
                .$(AccordionPanelElement.class).all().size());
        Assert.assertEquals("Green", $(AccordionElement.class).first()
                .$(AccordionPanelElement.class).first().getSummaryText());

        Assert.assertEquals("Blue", $(AccordionElement.class).first()
                .$(AccordionPanelElement.class).last().getSummaryText());

        getTestButton("removeBlueByContent").click();

        Assert.assertEquals(initialCount - 2, $(AccordionElement.class).first()
                .$(AccordionPanelElement.class).all().size());
        Assert.assertEquals("Disabled", $(AccordionElement.class).first()
                .$(AccordionPanelElement.class).last().getSummaryText());
    }

    private ButtonElement getTestButton(String id) {
        return $(ButtonElement.class).onPage().id(id);
    }

    private String getLastEvent(String type) {
        return $(TestBenchElement.class).id(type).$("span").last().getText();
    }
}
