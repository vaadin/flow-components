/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.board.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.board.testbench.BoardElement;
import com.vaadin.flow.component.board.testbench.RowElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-board/SalesDashboard")
public class BoardBasicIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void firstRowContainsExpectedTexts() {
        BoardElement board = $(BoardElement.class).first();
        RowElement firstRow = board.getRow(0);

        List<TestBenchElement> children = firstRow.getChildren();
        Assert.assertEquals(4, children.size());

        Assert.assertEquals("Total Revenue / 1 k$", children.get(0).getText());
        Assert.assertEquals("Billed / 1k$", children.get(1).getText());
        Assert.assertEquals("Outstanding / 1k$", children.get(2).getText());
        Assert.assertEquals("Refunded / 1k$", children.get(3).getText());
    }
}
