/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.popover.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.popover.testbench.PopoverElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-popover/delay-defaults")
public class PopoverDelayDefaultsIT extends AbstractComponentIT {

    private PopoverElement popover;

    @Before
    public void init() {
        open();
        popover = $(PopoverElement.class).first();
    }

    @Test
    public void changeDefaults_checkPopoverConfig() {
        $("button").id("set-default-delays-to-2000").click();
        Assert.assertEquals(2000, getActiveHideDelay(popover));
        Assert.assertEquals(2000, getActiveFocusDelay(popover));
        Assert.assertEquals(2000, getActiveHoverDelay(popover));
    }

    @Test
    public void changeDefaults_refreshPage_checkPopoverConfig() {
        $("button").id("set-default-delays-to-5000").click();
        getDriver().navigate().refresh();
        popover = $(PopoverElement.class).first();
        Assert.assertEquals(5000, getActiveHideDelay(popover));
        Assert.assertEquals(5000, getActiveFocusDelay(popover));
        Assert.assertEquals(5000, getActiveHoverDelay(popover));
    }

    private int getActiveFocusDelay(PopoverElement popover) {
        return getOpenedStateControllerPropertyValue(popover, "__focusDelay");
    }

    private int getActiveHoverDelay(PopoverElement popover) {
        return getOpenedStateControllerPropertyValue(popover, "__hoverDelay");
    }

    private int getActiveHideDelay(PopoverElement popover) {
        return getOpenedStateControllerPropertyValue(popover, "__hideDelay");
    }

    private int getOpenedStateControllerPropertyValue(PopoverElement popover,
            String propertyName) {
        var value = executeScript(
                "return arguments[0]._openedStateController[arguments[1]];",
                popover, propertyName);
        return ((Number) value).intValue();
    }
}
