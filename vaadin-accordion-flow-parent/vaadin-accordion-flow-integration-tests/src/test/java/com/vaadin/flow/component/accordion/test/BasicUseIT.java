package com.vaadin.flow.component.accordion.test;

import com.vaadin.flow.component.accordion.testbench.AccordionPanelElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.accordion.testbench.AccordionElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.vaadin.flow.component.accordion.examples.MainView.ACCORDION_EVENTS;
import static com.vaadin.flow.component.accordion.examples.MainView.PANEL_EVENTS;

public class BasicUseIT extends AbstractParallelTest {

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(), super.getBaseURL() + "/vaadin-accordion") ;
        getDriver().get(url);
    }

    @Test
    public void accordionIsPresent() {
        Assert.assertTrue($(AccordionElement.class).exists());
    }

    @Test
    public void progammaticOpenByIndex() {
        getTestButton("1").click();
        Assert.assertEquals(1, $(AccordionElement.class).first().getOpenedIndex().getAsInt());
        Assert.assertEquals("Green opened", getLastEvent(ACCORDION_EVENTS));
        Assert.assertEquals("Panel Green opened", getLastEvent(PANEL_EVENTS));

        getTestButton("3").click();
        Assert.assertEquals(3, $(AccordionElement.class).first().getOpenedIndex().getAsInt());
        Assert.assertEquals("Blue opened", getLastEvent(ACCORDION_EVENTS));
        Assert.assertEquals("Panel Blue opened", getLastEvent(PANEL_EVENTS));
    }

    public void programmaticOpenByPanel() {
        getTestButton("green").click();

        final AccordionPanelElement secondPanel =
                $(AccordionElement.class).first().$(AccordionPanelElement.class).all().get(1);

        Assert.assertEquals(secondPanel, $(AccordionElement.class).first().getOpenedPanel());

        Assert.assertEquals("Green opened", getLastEvent(ACCORDION_EVENTS));
        Assert.assertEquals("Panel Green opened", getLastEvent(PANEL_EVENTS));

        getTestButton("blue").click();
        Assert.assertEquals("Blue opened", getLastEvent(ACCORDION_EVENTS));
        Assert.assertEquals("Panel Blue opened", getLastEvent(PANEL_EVENTS));
    }

    public void programmaticClose() {
        getTestButton("1").click();
        getTestButton("close").click();
        Assert.assertEquals("Accordion closed", getLastEvent(ACCORDION_EVENTS));
    }

    public void userOpen() {
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().click();
        Assert.assertEquals("Panel Blue opened", getLastEvent(PANEL_EVENTS));
    }

    public void userClose() {
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().click();
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().click();
        Assert.assertEquals("Accordion closed", getLastEvent(ACCORDION_EVENTS));
    }

    public void panelSummaryText() {
        Assert.assertEquals("Blue",
                $(AccordionElement.class).first().$(AccordionPanelElement.class).last().getSummaryText());
    }

    public void openPanel() {
        Assert.assertFalse($(AccordionElement.class).first().$(AccordionPanelElement.class).last().isOpened());
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().open();
        Assert.assertTrue($(AccordionElement.class).first().$(AccordionPanelElement.class).last().isOpened());
    }

    public void closePanel() {
        Assert.assertFalse($(AccordionElement.class).first().$(AccordionPanelElement.class).last().isOpened());
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().close();
        Assert.assertFalse($(AccordionElement.class).first().$(AccordionPanelElement.class).last().isOpened());

        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().open();
        Assert.assertTrue($(AccordionElement.class).first().$(AccordionPanelElement.class).last().isOpened());

        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().close();
        Assert.assertFalse($(AccordionElement.class).first().$(AccordionPanelElement.class).last().isOpened());
    }

    public void removePanel() {
        final int initialCount = $(AccordionElement.class).first().$(AccordionPanelElement.class).all().size();

        Assert.assertEquals("Red",
                $(AccordionElement.class).first().$(AccordionPanelElement.class).first().getSummaryText());

        getTestButton("removeRed").click();

        Assert.assertEquals(initialCount - 1,
                $(AccordionElement.class).first().$(AccordionPanelElement.class).all().size());
        Assert.assertEquals("Green",
                $(AccordionElement.class).first().$(AccordionPanelElement.class).first().getSummaryText());

        Assert.assertEquals("Blue",
                $(AccordionElement.class).first().$(AccordionPanelElement.class).last().getSummaryText());

        getTestButton("removeBlueByContent").click();

        Assert.assertEquals(initialCount - 2,
                $(AccordionElement.class).first().$(AccordionPanelElement.class).all().size());
        Assert.assertEquals("Disabled",
                $(AccordionElement.class).first().$(AccordionPanelElement.class).last().getSummaryText());
    }

    private ButtonElement getTestButton(String id) {
        return $(ButtonElement.class).onPage().id(id);
    }

    private String getLastEvent(String type) {
        return $(TestBenchElement.class).id(type).$("span").last().getText();
    }
}
