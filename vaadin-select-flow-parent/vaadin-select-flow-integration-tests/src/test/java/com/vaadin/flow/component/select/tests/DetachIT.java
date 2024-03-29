package com.vaadin.flow.component.select.tests;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-select/reattach-test")
public class DetachIT extends AbstractComponentIT {

    /**
     * https://github.com/vaadin/vaadin-select-flow/issues/43
     *
     * Test that setting a renderer after detach won't cause an exception on the
     * client side.
     *
     */
    @Test
    public void testReattach_setRenderer() {
        open();
        getCommandExecutor().waitForVaadin();
        $("button").first().click();
        getCommandExecutor().waitForVaadin();
        $("button").first().click();
        getCommandExecutor().waitForVaadin();
        assertFalse($("div.v-system-error").exists());
    }

}
