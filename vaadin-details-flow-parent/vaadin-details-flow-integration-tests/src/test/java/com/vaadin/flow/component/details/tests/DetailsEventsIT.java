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
package com.vaadin.flow.component.details.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.details.testbench.DetailsElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-details/events")
public class DetailsEventsIT extends AbstractComponentIT {
    private DetailsElement details;
    private TestBenchElement toggle;
    private TestBenchElement output;

    @Before
    public void init() {
        open();
        details = $(DetailsElement.class).waitForFirst();
        toggle = $(TestBenchElement.class).id("toggle");
        output = $(TestBenchElement.class).id("output");
    }

    @Test
    public void noInitialOpenedChangeEvent() {
        Assert.assertEquals("", output.getText());
    }

    @Test
    public void toggleOnClient_openedChangeEventIsFromClient() {
        details.toggle();
        Assert.assertEquals("Opened changed: opened=true, isFromClient=true",
                output.getText());
    }

    @Test
    public void toggleOnServer_openedChangeEventIsNotFromClient() {
        toggle.click();
        Assert.assertEquals("Opened changed: opened=true, isFromClient=false",
                output.getText());
    }
}
