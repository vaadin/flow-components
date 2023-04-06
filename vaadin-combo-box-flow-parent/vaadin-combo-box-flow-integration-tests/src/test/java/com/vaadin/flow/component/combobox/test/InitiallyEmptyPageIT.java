
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
