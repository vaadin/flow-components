package com.vaadin.flow.component.board.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.board.testbench.BoardElement;
import com.vaadin.flow.component.board.testbench.RowElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.testbench.TestBenchElement;

public class BoardDynamicResizeIT extends AbstractParallelTest {

    @Test
    public void basicLayout_boardTabletSize_twoRowsAndTwoItemsInRow() {
        open(BoardDynamicResizeView.class);
        BoardElement board = $(BoardElement.class).get(0);
        RowElement row = board.getRow(0);
        List<TestBenchElement> rowChildren = row
                .getPropertyElements("children");
        ButtonElement resizeButton = $(ButtonElement.class).id("resize");
        resizeButton.click();

        Assert.assertEquals("Lbl1 should have same Y as the board Y coordinate",
                board.getLocation().getY(),
                rowChildren.get(0).getLocation().getY());

        Assert.assertEquals(
                "Lbl2 should have Y == board.location + lbl1.height",
                board.getLocation().getY()
                        + rowChildren.get(0).getSize().getHeight(),
                rowChildren.get(1).getLocation().getY());

        Assert.assertEquals(
                "Lbl3 should have Y == lbl2.location + lbl1.height + lbl2.height",
                rowChildren.get(1).getLocation().getY()
                        + rowChildren.get(2).getSize().getHeight(),
                rowChildren.get(2).getLocation().getY());
    }

}
