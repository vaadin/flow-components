/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.tests;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.gridpro.testbench.GridProElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("gridpro-item-details")
public class GridProItemDetailsIT extends AbstractComponentIT {

    @Before
    public void before() {
        open();
        $(GridProElement.class).waitForFirst();
    }

    @Test
    public void noErrorsLogged() {
        checkLogsForErrors();
    }
}
