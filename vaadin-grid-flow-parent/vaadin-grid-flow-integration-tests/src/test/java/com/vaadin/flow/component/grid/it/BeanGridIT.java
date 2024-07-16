/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/beangridpage")
public class BeanGridIT extends AbstractComponentIT {

    @Test
    public void gridNullValuesRenderedAsEmptyStrings() {
        open();
        GridElement grid = $(GridElement.class).first();
        String text = grid.getText();
        Assert.assertFalse("Null values should be presented as empty strings",
                text.contains("null"));
    }

}
