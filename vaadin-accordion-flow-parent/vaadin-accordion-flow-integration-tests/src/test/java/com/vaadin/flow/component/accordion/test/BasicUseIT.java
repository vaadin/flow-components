package com.vaadin.flow.component.accordion.test;

import com.vaadin.flow.component.accordion.testbench.AccordionPanelElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.accordion.testbench.AccordionElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static com.vaadin.flow.component.accordion.examples.MainView.ACCORDION_EVENTS;
import static com.vaadin.flow.component.accordion.examples.MainView.PANEL_EVENTS;

public class BasicUseIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void accordionIsPresent() {
        Assert.assertTrue($(AccordionElement.class).exists());
    }

    @Test
    public void progammaticExpandByIndex() {
        getTestButton("1").click();
        Assert.assertEquals(1, $(AccordionElement.class).first().getExpandedIndex().intValue());
        Assert.assertEquals("Green opened", getLastEvent(ACCORDION_EVENTS));
        Assert.assertEquals("Panel Green opened", getLastEvent(PANEL_EVENTS));

        getTestButton("3").click();
        Assert.assertEquals(3, $(AccordionElement.class).first().getExpandedIndex().intValue());
        Assert.assertEquals("Blue opened", getLastEvent(ACCORDION_EVENTS));
        Assert.assertEquals("Panel Blue opened", getLastEvent(PANEL_EVENTS));
    }

    public void programmaticExpandByPanel() {
        getTestButton("green").click();

        final AccordionPanelElement secondPanel =
                $(AccordionElement.class).first().$(AccordionPanelElement.class).all().get(1);

        Assert.assertEquals(secondPanel, $(AccordionElement.class).first().getExpandedPanel());

        Assert.assertEquals("Green opened", getLastEvent(ACCORDION_EVENTS));
        Assert.assertEquals("Panel Green opened", getLastEvent(PANEL_EVENTS));

        getTestButton("blue").click();
        Assert.assertEquals("Blue opened", getLastEvent(ACCORDION_EVENTS));
        Assert.assertEquals("Panel Blue opened", getLastEvent(PANEL_EVENTS));
    }

    public void programmaticCollapse() {
        getTestButton("1").click();
        getTestButton("collapse").click();
        Assert.assertEquals("Accordion collapsed", getLastEvent(ACCORDION_EVENTS));
    }

    public void userExpand() {
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().click();
        Assert.assertEquals("Panel Blue opened", getLastEvent(PANEL_EVENTS));
    }

    public void userCollapse() {
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().click();
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().click();
        Assert.assertEquals("Accordion collapsed", getLastEvent(ACCORDION_EVENTS));
    }

    public void panelSummaryText() {
        Assert.assertEquals("Blue",
                $(AccordionElement.class).first().$(AccordionPanelElement.class).last().getSummaryText());
    }

    public void expandPanel() {
        Assert.assertFalse($(AccordionElement.class).first().$(AccordionPanelElement.class).last().isExpanded());
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().expand();
        Assert.assertTrue($(AccordionElement.class).first().$(AccordionPanelElement.class).last().isExpanded());
    }

    public void collapsePanel() {
        Assert.assertFalse($(AccordionElement.class).first().$(AccordionPanelElement.class).last().isExpanded());
        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().collapse();
        Assert.assertFalse($(AccordionElement.class).first().$(AccordionPanelElement.class).last().isExpanded());

        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().expand();
        Assert.assertTrue($(AccordionElement.class).first().$(AccordionPanelElement.class).last().isExpanded());

        $(AccordionElement.class).first().$(AccordionPanelElement.class).last().collapse();
        Assert.assertFalse($(AccordionElement.class).first().$(AccordionPanelElement.class).last().isExpanded());
    }

    private ButtonElement getTestButton(String id) {
        return $(ButtonElement.class).onPage().id(id);
    }

    private String getLastEvent(String type) {
        return $(TestBenchElement.class).id(type).$("span").last().getText();
    }
}
