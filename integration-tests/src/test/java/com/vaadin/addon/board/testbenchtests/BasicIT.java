package com.vaadin.addon.board.testbenchtests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.board.testUI.BasicUI;
import com.vaadin.board.elements.BoardElement;
import com.vaadin.testbench.elements.ButtonElement;

public class BasicIT extends AbstractParallelTest {

    @Override
    protected Class<?> getUIClass() {
        return BasicUI.class;
    }

    @Test
    public void basicLayout_boardTabletSize_twoRowsAndTwoItemsInRow() {
        BoardElement board = $(BoardElement.class).get(0);
        ButtonElement btn1 = $(ButtonElement.class).caption("Button 1").first();
        ButtonElement btn2 = $(ButtonElement.class).caption("Button 2").first();
        ButtonElement btn3 = $(ButtonElement.class).caption("Button 3").first();
        ButtonElement btn4 = $(ButtonElement.class).caption("Button 4").first();

        Assert.assertEquals("Btn1 should have same Y as the board Y coordinate",
            board.getLocation().getY(), btn1.getLocation().getY());
        Assert.assertEquals("Btn2 should have same Y as the board Y coordinate",
            board.getLocation().getY(), btn2.getLocation().getY());

        Assert.assertEquals("Btn3 should have Y == board.location + btn1.height",
            board.getLocation().getY() + btn1.getSize().getHeight(), btn3.getLocation().getY());
        Assert.assertEquals("Btn4 should have Y == board.location +btn2.height",
            board.getLocation().getY() + btn2.getSize().getHeight(), btn4.getLocation().getY());
    }

}
