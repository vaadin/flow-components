/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.board;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.internal.FunctionCallerTest;

class RowTest {

    @Test
    void addOne() {
        DummyComponent c1 = new DummyComponent();
        Row row = new Row();
        row.add(c1);
        BoardTest.assertChildren(row, c1);
    }

    @Test
    void addFour() {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        DummyComponent c3 = new DummyComponent();
        DummyComponent c4 = new DummyComponent();
        Row row = new Row();
        row.add(c1, c2, c3, c4);
        BoardTest.assertChildren(row, c1, c2, c3, c4);
    }

    @Test
    void addFive() throws Exception {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        DummyComponent c3 = new DummyComponent();
        DummyComponent c4 = new DummyComponent();
        DummyComponent c5 = new DummyComponent();
        Row row = new Row();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> row.add(c1, c2, c3, c4, c5));
    }

    @Test
    void remove() throws Exception {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        DummyComponent c3 = new DummyComponent();
        DummyComponent c4 = new DummyComponent();

        Row row = new Row();
        row.add(c1, c2, c3, c4);

        row.remove(c4);
        BoardTest.assertChildren(row, c1, c2, c3);
        row.remove(c2);
        BoardTest.assertChildren(row, c1, c3);
        row.remove(c1);
        BoardTest.assertChildren(row, c3);
        row.remove(c3);
        BoardTest.assertChildren(row);
    }

    @Test
    void removeUnrelated() throws Exception {
        DummyComponent c1 = new DummyComponent();

        Row row = new Row();
        row.add(c1);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Row().remove(c1));
    }

    @Test
    void setGetColspan() throws Exception {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        DummyComponent c3 = new DummyComponent();

        Row row = new Row();

        row.add(c1, 2);
        Assertions.assertEquals(2, row.getComponentSpan(c1));

        row.add(c2, c3);
        Assertions.assertEquals(2, row.getComponentSpan(c1));
        Assertions.assertEquals(1, row.getComponentSpan(c2));
        Assertions.assertEquals(1, row.getComponentSpan(c3));

        row.setComponentSpan(c1, 1);
        Assertions.assertEquals(1, row.getComponentSpan(c1));
        Assertions.assertEquals(1, row.getComponentSpan(c2));
        Assertions.assertEquals(1, row.getComponentSpan(c3));

        row.setComponentSpan(c2, 2);
        Assertions.assertEquals(1, row.getComponentSpan(c1));
        Assertions.assertEquals(2, row.getComponentSpan(c2));
        Assertions.assertEquals(1, row.getComponentSpan(c3));
    }

    @Test
    void setColspanUpdatesElement() throws Exception {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        Row row = new Row();
        row.add(c1);
        row.add(c2, 2);
        Assertions.assertNull(
                c1.getElement().getAttribute(Row.COLSPAN_ATTRIBUTE));
        Assertions.assertEquals("2",
                c2.getElement().getAttribute(Row.COLSPAN_ATTRIBUTE));

        row.setComponentSpan(c1, 2);
        row.setComponentSpan(c2, 1);
        Assertions.assertEquals("2",
                c1.getElement().getAttribute(Row.COLSPAN_ATTRIBUTE));
        Assertions.assertNull(
                c2.getElement().getAttribute(Row.COLSPAN_ATTRIBUTE));
    }

    @Test
    void setColspanUnrelated() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Row().setComponentSpan(new DummyComponent(), 2));
    }

    @Test
    void getColspanUnrelated() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Row().getComponentSpan(new DummyComponent()));
    }

    @Test
    void exceedTotalSpanThroughAdd() throws Exception {
        Row row = new Row();
        row.add(new DummyComponent(), 4);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> row.add(new DummyComponent()));
    }

    @Test
    void exceedTotalSpanThroughSetColspan() throws Exception {
        Row row = new Row();
        DummyComponent dummyComponent = new DummyComponent();
        DummyComponent dummyComponent2 = new DummyComponent();
        row.add(dummyComponent, 3);
        row.add(dummyComponent2);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> row.setComponentSpan(dummyComponent2, 2));
    }

    @Test
    void redrawCallsRedraw() throws Exception {
        UI ui = new UI();
        Board board = new Board();
        ui.add(board);

        board.redraw();
        FunctionCallerTest.assertPendingInvocations(ui, "return $0.redraw()");
    }

}
