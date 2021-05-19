/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.component.checkbox.tests;

import java.util.List;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;

@TestPath("vaadin-checkbox-group-disabled-item")
public class CheckboxGroupDisabledItemIT extends AbstractComponentIT {

    @Test
    public void disabledGroupItemChecked() {
        open();
        TestBenchElement group = $(TestBenchElement.class)
                .id("checkbox-group-disabled-item");

        List<TestBenchElement> checkboxes = group.$("vaadin-checkbox").all();

        Assert.assertEquals(Boolean.TRUE.toString(),
                checkboxes.get(1).getAttribute("checked"));
    }
}
