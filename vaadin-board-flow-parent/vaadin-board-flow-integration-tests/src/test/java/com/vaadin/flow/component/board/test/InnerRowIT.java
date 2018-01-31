package com.vaadin.flow.component.board.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.board.elements.BoardElement;
import com.vaadin.board.elements.RowElement;
import com.vaadin.flow.component.board.test.InnerRowUI;
import com.vaadin.testbench.By;
import com.vaadin.testbench.elements.ButtonElement;

public class InnerRowIT extends AbstractParallelTest {

    @Override
    protected Class<?> getUIClass() {
        return InnerRowUI.class;
    }

    @Override
    public void setup() throws Exception {
        super.setup();
        openURL();
    }

    @Test
    public void oneRowBoard_addInnerRow_hasOneInnerRow() {

        ButtonElement addButton = $(ButtonElement.class).id(InnerRowUI.BUTTON_ADD_ID);
        addButton.click();

        BoardElement board = $(BoardElement.class).get(0);
        List<RowElement> rows = board.getRows();
        List<WebElement> innerRow = board.findElements(By.xpath("//vaadin-board/vaadin-board-row/vaadin-board-row"));
        List<WebElement> innerRowChildren = board.findElements(By.xpath("//vaadin-board/vaadin-board-row/vaadin-board-row/*"));

        Assert.assertEquals("Board has only one row" , 1, rows.size());
        Assert.assertEquals("Board has one inner row", 1, innerRow.size());
        Assert.assertEquals("Board has one inner row has 4 children", 4, innerRowChildren.size());
    }

    @Test
    public void oneRowBoard_addThenRemoveInnerRow_hasNoInnerRow() {

        ButtonElement addButton = $(ButtonElement.class).id(InnerRowUI.BUTTON_ADD_ID);
        ButtonElement rmvButton = $(ButtonElement.class).id(InnerRowUI.BUTTON_RMV_ID);
        addButton.click();
        rmvButton.click();

        BoardElement board = $(BoardElement.class).get(0);
        List<RowElement> rows = board.getRows();
        List<WebElement> innerRow = board.findElements(By.xpath("//vaadin-board/vaadin-board-row/vaadin-board-row"));

        Assert.assertEquals("Board has only one row" , 1, rows.size());
        Assert.assertEquals("Board has one inner row", 0, innerRow.size());
    }

}
