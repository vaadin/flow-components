package com.vaadin.flow.component.board;

/*-
 * #%L
 * Vaadin Board for Vaadin 10
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.internal.FunctionCallerTest;

public class BoardTest {

    @Test
    public void addOne() throws Exception {
        DummyComponent c1 = new DummyComponent();
        Board board = new Board();
        Row addedRow = board.addRow(c1);

        assertChildren(board, addedRow);
        assertChildren(addedRow, c1);
    }

    @Test
    public void addFour() throws Exception {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        DummyComponent c3 = new DummyComponent();
        DummyComponent c4 = new DummyComponent();
        Board board = new Board();
        Row addedRow = board.addRow(c1, c2, c3, c4);

        assertChildren(board, addedRow);
        assertChildren(addedRow, c1, c2, c3, c4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFive() throws Exception {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        DummyComponent c3 = new DummyComponent();
        DummyComponent c4 = new DummyComponent();
        DummyComponent c5 = new DummyComponent();
        Board board = new Board();
        board.addRow(c1, c2, c3, c4, c5);
    }

    @Test
    public void addManyRows() throws Exception {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        Board board = new Board();
        Row row1 = board.addRow(c1);
        Row row2 = board.addRow(c2);

        assertChildren(board, row1, row2);
        assertChildren(row1, c1);
        assertChildren(row2, c2);
    }

    @Test
    public void removeRow() throws Exception {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        DummyComponent c3 = new DummyComponent();
        DummyComponent c4 = new DummyComponent();

        Board board = new Board();
        Row row1 = board.addRow(c1);
        Row row2 = board.addRow(c2);
        Row row3 = board.addRow(c3);
        Row row4 = board.addRow(c4);

        board.removeRow(row4);
        assertChildren(board, row1, row2, row3);
        board.removeRow(row2);
        assertChildren(board, row1, row3);
        board.removeRow(row1);
        assertChildren(board, row3);
        board.removeRow(row3);
        assertChildren(board);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeUnrelated() throws Exception {
        Board board = new Board();
        Component dummy = new DummyComponent();
        board.addRow(dummy);
        board.remove(dummy);
    }

    @Test
    public void redrawCallsRedraw() throws Exception {
        UI ui = new UI();
        Board board = new Board();
        ui.add(board);

        board.redraw();
        FunctionCallerTest.assertPendingInvocations(ui, "return $0.redraw()");
    }

    static void assertChildren(Component parent,
            Component... expectedChildren) {
        Assert.assertEquals(expectedChildren.length,
                parent.getChildren().count());
        Assert.assertArrayEquals(expectedChildren,
                parent.getChildren().toArray());
    }

}
