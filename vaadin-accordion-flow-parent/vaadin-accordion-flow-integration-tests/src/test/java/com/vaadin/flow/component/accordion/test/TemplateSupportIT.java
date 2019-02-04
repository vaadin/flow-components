package com.vaadin.flow.component.accordion.test;

import com.vaadin.flow.component.accordion.testbench.AccordionElement;
import com.vaadin.flow.component.accordion.testbench.AccordionPanelElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.ElementQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TemplateSupportIT extends AbstractParallelTest {

    private static final String APP_ROOT = "accordion-app";

    @Before
    public void init() {
        getDriver().get(getBaseURL() + "/accordionintemplate");
    }

    @Test
    public void accordionIsPresent() {
        Assert.assertTrue(getAccordion().exists());
    }

    @Test
    public void interactions() {
        getPanels().get(1).open();
        Assert.assertEquals("Billing Address opened", getLastEvent());

        getAccordion().first().close();
        Assert.assertEquals("Accordion closed", getLastEvent());
    }

    private ElementQuery<AccordionElement> getAccordion() {
        return $(APP_ROOT).waitForFirst().$(AccordionElement.class);
    }

    private ElementQuery<AccordionPanelElement> getPanels() {
        return getAccordion().first().$(AccordionPanelElement.class);
    }

    protected String getLastEvent() {
        return $(APP_ROOT).waitForFirst().$(VerticalLayoutElement.class)
                .last().$("span").last().getText();
    }
}
