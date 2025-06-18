/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/virtual-child")
public class GridVirtualChildIT extends AbstractComponentIT {

    @Test
    public void shouldNotHaveErrors() {
        open();
        checkLogsForErrors();
    }

}
