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
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;

@TestPath("vaadin-radio-button/helper")
public class HelperIT extends AbstractComponentIT {

    /**
     * Assert that helper component exists after setItems. This issue is similar
     * to https://github.com/vaadin/vaadin-checkbox/issues/191
     */
    @Test
    public void assertHelperComponentExists() {
        open();
        TestBenchElement radioGroup = $("vaadin-radio-group").first();

        TestBenchElement helperComponent = radioGroup.$("span")
                .attributeContains("slot", "helper").first();
        Assert.assertEquals("Helper text", helperComponent.getText());

    }
}
