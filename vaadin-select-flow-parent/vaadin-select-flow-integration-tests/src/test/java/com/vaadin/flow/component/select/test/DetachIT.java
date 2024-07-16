/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.select.test;

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
        open(getTestURL());
        getCommandExecutor().waitForVaadin();
        $("button").first().click();
        getCommandExecutor().waitForVaadin();
        $("button").first().click();
        getCommandExecutor().waitForVaadin();
        assertFalse($("div.v-system-error").exists());
    }

}
