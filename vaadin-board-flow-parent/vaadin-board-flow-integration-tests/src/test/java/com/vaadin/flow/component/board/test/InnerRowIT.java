package com.vaadin.flow.component.board.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.board.testbench.BoardElement;
import com.vaadin.flow.component.board.testbench.RowElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;

public class InnerRowIT extends AbstractParallelTest {

    @Test
    public void oneRowBoard_addInnerRow_hasOneInnerRow() {
        open(InnerRowView.class);
        ButtonElement addButton = $(ButtonElement.class)
                .id(InnerRowView.BUTTON_ADD_ID);
        addButton.click();

        BoardElement board = $(BoardElement.class).get(0);
        List<RowElement> rows = board.getRows();
        List<WebElement> innerRow = board.findElements(
                By.xpath("//vaadin-board/vaadin-board-row/vaadin-board-row"));
        List<WebElement> innerRowChildren = board.findElements(
                By.xpath("//vaadin-board/vaadin-board-row/vaadin-board-row/*"));

        Assert.assertEquals("Board has only one row", 1, rows.size());
        Assert.assertEquals("Board has one inner row", 1, innerRow.size());
        Assert.assertEquals("Board has one inner row has 4 children", 4,
                innerRowChildren.size());
    }

    @Test
    public void oneRowBoard_addThenRemoveInnerRow_hasNoInnerRow() {
        open(InnerRowView.class);

        ButtonElement addButton = $(ButtonElement.class)
                .id(InnerRowView.BUTTON_ADD_ID);
        ButtonElement rmvButton = $(ButtonElement.class)
                .id(InnerRowView.BUTTON_RMV_ID);
        addButton.click();
        rmvButton.click();

        BoardElement board = $(BoardElement.class).get(0);
        List<RowElement> rows = board.getRows();
        List<WebElement> innerRow = board.findElements(
                By.xpath("//vaadin-board/vaadin-board-row/vaadin-board-row"));

        Assert.assertEquals("Board has only one row", 1, rows.size());
        Assert.assertEquals("Board has one inner row", 0, innerRow.size());
    }

}
