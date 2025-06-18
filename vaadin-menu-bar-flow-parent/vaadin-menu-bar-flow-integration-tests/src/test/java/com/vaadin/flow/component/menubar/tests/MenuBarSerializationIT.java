/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.menubar.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/serialization")
public class MenuBarSerializationIT extends AbstractComponentIT {
    private MenuBarElement menuBar;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).waitForFirst();
    }

    @Test
    public void serializeAndDeserializeUi_noExceptionIsThrown() {
        $("button").id("serialize-and-deserialize-ui").click();
        Assert.assertEquals("",
                $("span").id("exception-message-span").getText());
    }
}
