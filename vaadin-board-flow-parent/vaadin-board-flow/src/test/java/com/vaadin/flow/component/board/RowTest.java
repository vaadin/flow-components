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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.internal.FunctionCallerTest;

public class RowTest {

    @Test
    public void addOne() {
        DummyComponent c1 = new DummyComponent();
        Row row = new Row();
        row.add(c1);
        BoardTest.assertChildren(row, c1);
    }

    @Test
    public void addFour() {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        DummyComponent c3 = new DummyComponent();
        DummyComponent c4 = new DummyComponent();
        Row row = new Row();
        row.add(c1, c2, c3, c4);
        BoardTest.assertChildren(row, c1, c2, c3, c4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFive() throws Exception {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        DummyComponent c3 = new DummyComponent();
        DummyComponent c4 = new DummyComponent();
        DummyComponent c5 = new DummyComponent();
        Row row = new Row();
        row.add(c1, c2, c3, c4, c5);
    }

    @Test
    public void remove() throws Exception {
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

    @Test(expected = IllegalArgumentException.class)
    public void removeUnrelated() throws Exception {
        DummyComponent c1 = new DummyComponent();

        Row row = new Row();
        row.add(c1);

        new Row().remove(c1);
    }

    @Test
    public void setGetColspan() throws Exception {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        DummyComponent c3 = new DummyComponent();

        Row row = new Row();

        row.add(c1, 2);
        Assert.assertEquals(2, row.getComponentSpan(c1));

        row.add(c2, c3);
        Assert.assertEquals(2, row.getComponentSpan(c1));
        Assert.assertEquals(1, row.getComponentSpan(c2));
        Assert.assertEquals(1, row.getComponentSpan(c3));

        row.setComponentSpan(c1, 1);
        Assert.assertEquals(1, row.getComponentSpan(c1));
        Assert.assertEquals(1, row.getComponentSpan(c2));
        Assert.assertEquals(1, row.getComponentSpan(c3));

        row.setComponentSpan(c2, 2);
        Assert.assertEquals(1, row.getComponentSpan(c1));
        Assert.assertEquals(2, row.getComponentSpan(c2));
        Assert.assertEquals(1, row.getComponentSpan(c3));
    }

    @Test
    public void setColspanUpdatesElement() throws Exception {
        DummyComponent c1 = new DummyComponent();
        DummyComponent c2 = new DummyComponent();
        Row row = new Row();
        row.add(c1);
        row.add(c2, 2);
        Assert.assertNull(c1.getElement().getAttribute(Row.COLSPAN_ATTRIBUTE));
        Assert.assertEquals("2",
                c2.getElement().getAttribute(Row.COLSPAN_ATTRIBUTE));

        row.setComponentSpan(c1, 2);
        row.setComponentSpan(c2, 1);
        Assert.assertEquals("2",
                c1.getElement().getAttribute(Row.COLSPAN_ATTRIBUTE));
        Assert.assertNull(c2.getElement().getAttribute(Row.COLSPAN_ATTRIBUTE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setColspanUnrelated() throws Exception {
        new Row().setComponentSpan(new DummyComponent(), 2);

    }

    @Test(expected = IllegalArgumentException.class)
    public void getColspanUnrelated() throws Exception {
        new Row().getComponentSpan(new DummyComponent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceedTotalSpanThroughAdd() throws Exception {
        Row row = new Row();
        row.add(new DummyComponent(), 4);
        row.add(new DummyComponent());

    }

    @Test(expected = IllegalArgumentException.class)
    public void exceedTotalSpanThroughSetColspan() throws Exception {
        Row row = new Row();
        DummyComponent dummyComponent = new DummyComponent();
        DummyComponent dummyComponent2 = new DummyComponent();
        row.add(dummyComponent, 3);
        row.add(dummyComponent2);
        row.setComponentSpan(dummyComponent2, 2);
    }

    @Test
    public void redrawCallsRedraw() throws Exception {
        UI ui = new UI();
        Board board = new Board();
        ui.add(board);

        board.redraw();
        FunctionCallerTest.assertPendingInvocations(ui, "return $0.redraw()");
    }

}
