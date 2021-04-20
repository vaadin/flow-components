package com.vaadin.flow.component.grid.it;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/joined-headers")
public class JoinedHeadersIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void noExceptionsAreThrowWhenGridsAreCreated() {
        $(GridElement.class).id("grid1");
        $(GridElement.class).id("grid2");
    }
}
