/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu.it;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-context-menu/manually-attached-context-menu")
public class ManuallyAttachedContextMenuIT extends AbstractContextMenuIT {

    @Before
    public void init() {
        open();
        checkLogsForErrors();
    }

    @Test
    public void contextMenuAttachedToThePage_setItemChecked_open_itemChecked() {
        leftClickOn("toggle-checked");
        rightClickOn("target");
        TestBenchElement item = getMenuItems().get(0);
        ContextMenuPageIT.assertCheckedInClientSide(item, true);
    }
}
