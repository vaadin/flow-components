/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.testutil.TestPath;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-combo-box/initially-empty")
public class InitiallyEmptyPageIT extends AbstractComboBoxIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void shouldAddAComboBoxInsideADetachedContainer() {
        clickButton("add-inside-detached-container-button");
        checkLogsForErrors();
    }
}
