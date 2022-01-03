/*
 * Copyright 2000-2022 Vaadin Ltd.
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
import org.junit.Test;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-button/icon-button")
public class IconForButtonIT extends AbstractComponentIT {

    @Test
    public void slotAttributeIsNotRemoved() {
        open();

        TestBenchElement button = $("vaadin-button").first();
        TestBenchElement icon = button.$("vaadin-icon").first();
        String slot = icon.getAttribute("slot");

        // self check: this is expected in the initialization and not part of
        // the test
        Assert.assertEquals("prefix", slot);

        button.click();
        // self check: the text is updated.
        Assert.assertEquals("Updated text", button.getText());

        icon = button.$("vaadin-icon").first();
        slot = icon.getAttribute("slot");
        // slot should have the same value after text update
        Assert.assertEquals("prefix", slot);
    }
}
