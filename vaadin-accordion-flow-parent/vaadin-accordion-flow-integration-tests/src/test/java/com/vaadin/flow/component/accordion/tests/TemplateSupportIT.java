/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.accordion.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.accordion.testbench.AccordionElement;
import com.vaadin.flow.component.accordion.testbench.AccordionPanelElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-accordion/template-support")
public class TemplateSupportIT extends AbstractComponentIT {

    private static final String APP_ROOT = "accordion-app";

    @Before
    public void init() {
        open();
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
        return $(APP_ROOT).waitForFirst().$(VerticalLayoutElement.class).last()
                .$("span").last().getText();
    }
}
