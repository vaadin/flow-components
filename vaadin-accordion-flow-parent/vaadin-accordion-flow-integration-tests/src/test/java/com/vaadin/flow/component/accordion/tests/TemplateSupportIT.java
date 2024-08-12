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
