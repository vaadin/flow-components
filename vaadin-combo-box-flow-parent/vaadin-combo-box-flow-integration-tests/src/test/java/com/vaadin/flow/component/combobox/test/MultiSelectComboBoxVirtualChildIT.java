package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-multi-select-combo-box/virtual-child")
public class MultiSelectComboBoxVirtualChildIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void addAsVirtualChild_noErrors() {
        // Verify that the connector initializes successfully even though the
        // element is not attached to the DOM, and Polymer has not finalized the
        // element yet
        checkLogsForErrors();
    }
}
