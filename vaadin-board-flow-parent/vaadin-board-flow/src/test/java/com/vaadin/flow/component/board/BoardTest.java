package com.vaadin.flow.component.board;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasOrderedComponents;

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
    public void removeUnrelatedRow() throws Exception {
        Board board = new Board();
        board.addRow(new DummyComponent());
        board.remove(new Row());
    }

    static void assertChildren(HasOrderedComponents<?> parent,
            Component... expectedChildren) {
        Assert.assertEquals(expectedChildren.length,
                parent.getComponentCount());
        Assert.assertArrayEquals(expectedChildren,
                parent.get().getChildren().toArray());
    }

}
