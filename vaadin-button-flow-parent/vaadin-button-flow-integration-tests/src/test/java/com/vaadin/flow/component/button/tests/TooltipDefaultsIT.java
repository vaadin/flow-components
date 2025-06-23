/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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

    private ButtonElement button;

    @Before
    public void init() {
        open();
        button = $(ButtonElement.class).first();
    }

    @Test
    public void checkTooltipConfig() {
        Assert.assertEquals(500, getActiveFocusDelay(button));
        Assert.assertEquals(100, getActiveHoverDelay(button));
    }

    @Test
    public void dynamicallyChangeDefaults_checkTooltipConfig() {
        button.click();
        Assert.assertEquals(1000, getActiveHideDelay(button));
    }

    @Test
    public void refreshBrowser_checkTooltipConfig() {
        getDriver().navigate().refresh();
        button = $(ButtonElement.class).first();
        Assert.assertEquals(500, getActiveFocusDelay(button));
        Assert.assertEquals(100, getActiveHoverDelay(button));
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
