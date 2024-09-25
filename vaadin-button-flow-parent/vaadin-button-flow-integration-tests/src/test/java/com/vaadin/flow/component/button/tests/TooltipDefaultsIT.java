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
package com.vaadin.flow.component.button.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-button/tooltip-defaults")
public class TooltipDefaultsIT extends AbstractComponentIT {

    private ButtonElement buttonWithTooltip;

    @Before
    public void init() {
        open();
        buttonWithTooltip = $(ButtonElement.class).first();
    }

    @Test
    public void changeDefaults_checkTooltipConfig() {
        $("button").id("set-default-delays-to-2000").click();
        Assert.assertEquals(2000, getActiveHideDelay(buttonWithTooltip));
        Assert.assertEquals(2000, getActiveFocusDelay(buttonWithTooltip));
        Assert.assertEquals(2000, getActiveHoverDelay(buttonWithTooltip));
    }

    @Test
    public void changeDefaults_refreshPage_checkTooltipConfig() {
        $("button").id("set-default-delays-to-5000").click();
        getDriver().navigate().refresh();
        buttonWithTooltip = $(ButtonElement.class).first();
        Assert.assertEquals(5000, getActiveHideDelay(buttonWithTooltip));
        Assert.assertEquals(5000, getActiveFocusDelay(buttonWithTooltip));
        Assert.assertEquals(5000, getActiveHoverDelay(buttonWithTooltip));
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
