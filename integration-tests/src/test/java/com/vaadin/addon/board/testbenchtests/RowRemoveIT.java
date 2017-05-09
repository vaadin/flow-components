package com.vaadin.addon.board.testbenchtests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.board.testUI.RowRemoveUI;
import com.vaadin.testbench.By;
import com.vaadin.testbench.elements.ButtonElement;

public class RowRemoveIT extends AbstractParallelTest {

    @Override
    protected Class<?> getUIClass() {
        return RowRemoveUI.class;
    }

    @Test
    public void twoRowsBoard_removeRow_hasOneRow() {
        WebElement board =getDriver().findElement(By.tagName("vaadin-board"));
        List<WebElement> rows = board.findElements(By.xpath("//vaadin-board/vaadin-board-row"));
        List<WebElement> children = board.findElements(By.xpath("//vaadin-board/vaadin-board-row/*"));
        Assert.assertEquals("Before removing board has 2 rows" , 2, rows.size());
        Assert.assertEquals("Board removing board has 4 elements", 4, children.size());

        ButtonElement rmvButton =$(ButtonElement.class).id(RowRemoveUI.RMV_BUTTON_ID);
        rmvButton.click();

        rows = board.findElements(By.xpath("//vaadin-board/vaadin-board-row"));
        children = board.findElements(By.xpath("//vaadin-board/vaadin-board-row/*"));
        Assert.assertEquals("Board has only one row" , 1, rows.size());
        Assert.assertEquals("Board has two buttons", 2, children.size());

    }

}
