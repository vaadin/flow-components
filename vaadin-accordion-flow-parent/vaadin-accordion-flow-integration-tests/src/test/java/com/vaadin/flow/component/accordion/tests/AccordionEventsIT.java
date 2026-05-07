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
package com.vaadin.flow.component.accordion.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.accordion.testbench.AccordionElement;
import com.vaadin.flow.component.accordion.testbench.AccordionPanelElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-accordion/events")
public class AccordionEventsIT extends AbstractComponentIT {
    private AccordionElement accordion;
    private TestBenchElement openSecond;
    private TestBenchElement close;
    private TestBenchElement output;

    @Before
    public void init() {
        open();
        accordion = $(AccordionElement.class).waitForFirst();
        openSecond = $(TestBenchElement.class).id("open-second");
        close = $(TestBenchElement.class).id("close");
        output = $(TestBenchElement.class).id("output");
    }

    @Test
    public void noInitialOpenedChangeEvent() {
        Assert.assertEquals("", output.getText());
    }

    @Test
    public void toggleOnClient_openedChangeEventIsFromClient() {
        accordion.$(AccordionPanelElement.class).all().get(2).toggle();
        Assert.assertEquals("Opened changed: index=2, isFromClient=true",
                output.getText());
    }

    @Test
    public void toggleOnServer_openedChangeEventIsNotFromClient() {
        openSecond.click();
        Assert.assertEquals("Opened changed: index=1, isFromClient=false",
                output.getText());
    }

    @Test
    public void closeOnServer_openedChangeEventIsNotFromClient() {
        openSecond.click();
        close.click();
        Assert.assertEquals(
                "Opened changed: index=1, isFromClient=false\n"
                        + "Opened changed: index=null, isFromClient=false",
                output.getText());
    }
}
