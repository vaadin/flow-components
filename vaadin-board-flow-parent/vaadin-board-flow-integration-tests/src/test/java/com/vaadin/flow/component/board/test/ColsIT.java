package com.vaadin.flow.component.board.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.board.testbench.BoardElement;
import com.vaadin.flow.component.board.testbench.RowElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;

/**
 *
 */
public class ColsIT extends AbstractParallelTest {

    @Test
    public void removeColsTest() throws Exception {
        open(ColsView.class);
        BoardElement board = $(BoardElement.class).get(0);
        List<RowElement> rows = board.getRows();
        Assert.assertEquals(1, rows.size());

        ButtonElement btn = $(ButtonElement.class).id("remove");
        ButtonElement btnA = $(ButtonElement.class).id("A");

        int widthOld = btnA.getSize().getWidth();
        btn.click();
        int widthNew = btnA.getSize().getWidth();
        Assert.assertTrue(widthOld > widthNew);
    }

    @Test
    public void exceptionColsTest() throws Exception {
        open(ColsView.class);
        BoardElement board = $(BoardElement.class).get(0);
        List<RowElement> rows = board.getRows();
        Assert.assertEquals(1, rows.size());

        ButtonElement btn = $(ButtonElement.class).id("exception");
        ButtonElement btnA = $(ButtonElement.class).id("A");
        int widthOld = btnA.getSize().getWidth();
        btn.click();
        int widthNew = btnA.getSize().getWidth();
        Assert.assertTrue(widthOld == widthNew);
    }

}
