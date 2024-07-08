/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-context-menu/initial-open-on-click")
public class InitialOpenOnClickIT extends AbstractContextMenuIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void menuHasOpenOnClickInitially_noClientSideErrors() {
        checkLogsForErrors();
    }

    @Test
    public void menuHasOpenOnClickInitially_clickTarget_menuOpens() {
        clickElementWithJs("target");
        verifyOpened();
        Assert.assertArrayEquals(new String[] { "foo" }, getMenuItemCaptions());
    }
}
