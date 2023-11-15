/*
 * Copyright 2000-2023 Vaadin Ltd.
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
import org.junit.Before;
import org.junit.Test;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-button/tooltip-defaults")
public class TooltipDefaultsIT extends AbstractComponentIT {

    private ButtonElement button;

    @Before
    public void init() {
        open();
        button = $(ButtonElement.class).first();
    }

    @Test
    public void changeDefaults_checkTooltipConfig() {
        button.click();
        Assert.assertEquals(5000, getActiveHideDelay(button));
        Assert.assertEquals(5000, getActiveFocusDelay(button));
        Assert.assertEquals(5000, getActiveHoverDelay(button));
    }

    @Test
    public void changeDefaults_refreshPage_checkTooltipConfig() {
        button.click();
        getDriver().navigate().refresh();
        button = $(ButtonElement.class).first();
        Assert.assertEquals(5000, getActiveHideDelay(button));
        Assert.assertEquals(5000, getActiveFocusDelay(button));
        Assert.assertEquals(5000, getActiveHoverDelay(button));
    }

    private int getActiveFocusDelay(ButtonElement button) {
        return getTooltipControllerPropertyValue(button, "focusDelay");
    }

    private int getActiveHoverDelay(ButtonElement button) {
        return getTooltipControllerPropertyValue(button, "hoverDelay");
    }

    private int getActiveHideDelay(ButtonElement button) {
        return getTooltipControllerPropertyValue(button, "hideDelay");
    }

    private int getTooltipControllerPropertyValue(ButtonElement button,
            String functionName) {
        var tooltipElement = button.$("vaadin-tooltip").first();
        var value = executeScript(
                "return arguments[0]._stateController[arguments[1]];",
                tooltipElement, functionName);
        return ((Number) value).intValue();
    }
}
