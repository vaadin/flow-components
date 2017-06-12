package com.vaadin.addon.board.testbenchtests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.board.testUI.RowRemoveUI;
import com.vaadin.board.elements.BoardElement;
import com.vaadin.board.elements.RowElement;
import com.vaadin.testbench.By;
import com.vaadin.testbench.elements.ButtonElement;

public class RowRemoveIT extends AbstractParallelTest {

    @Override
    protected Class<?> getUIClass() {
        return RowRemoveUI.class;
    }

    @Test
    public void twoRowsBoard_removeRow_hasOneRow() {
        BoardElement board =$(BoardElement.class).first();
        List<RowElement> rows = board.getRows();
        List<WebElement> children = board.findElements(By.xpath("//vaadin-board/vaadin-board-row/*"));
        Assert.assertEquals("Before removing board has 2 rows" , 2, rows.size());
        Assert.assertEquals("Board removing board has 4 elements", 4, children.size());

        ButtonElement rmvButton =$(ButtonElement.class).id(RowRemoveUI.RMV_BUTTON_ID);
        rmvButton.click();

        rows = board.getRows();
        children = board.findElements(By.xpath("//vaadin-board/vaadin-board-row/*"));
        Assert.assertEquals("Board has only one row" , 1, rows.size());
        Assert.assertEquals("Board has two buttons", 2, children.size());

    }

}
