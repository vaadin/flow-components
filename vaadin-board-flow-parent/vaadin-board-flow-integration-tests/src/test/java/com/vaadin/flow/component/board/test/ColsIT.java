package com.vaadin.flow.component.board.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.board.elements.BoardElement;
import com.vaadin.board.elements.RowElement;
import com.vaadin.flow.component.board.test.ColsUI;
import com.vaadin.testbench.elements.ButtonElement;

/**
 *
 */
public class ColsIT extends AbstractParallelTest {

    @Override
    protected Class<?> getUIClass() {
        return ColsUI.class;
    }


    @Before
    public void setup() throws Exception {
        super.setup();
        openURL();
    }
    @Test
    public void removeColsTest() throws Exception {

        BoardElement board = $(BoardElement.class).get(0);
        List<RowElement> rows = board.getRows();
        Assert.assertEquals(1, rows.size());

        ButtonElement btn = $(ButtonElement.class).caption("remove").first();

        int widthOld = $(ButtonElement.class).caption("Button A").first().getSize().getWidth();
        btn.click();
        int widthNew = $(ButtonElement.class).caption("Button A").first().getSize().getWidth();
        Assert.assertTrue(widthOld > widthNew);
    }

    @Test
    public void exceptionColsTest() throws Exception {
        BoardElement board = $(BoardElement.class).get(0);
        List<RowElement> rows = board.getRows();
        Assert.assertEquals(1, rows.size());

        ButtonElement btn = $(ButtonElement.class).caption("exception").first();
        int widthOld = $(ButtonElement.class).caption("Button A").first().getSize().getWidth();
        btn.click();
        int widthNew = $(ButtonElement.class).caption("Button A").first().getSize().getWidth();
        Assert.assertTrue(widthOld == widthNew);
    }

}
