package com.vaadin.addon.board.testbenchtests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.board.testUI.BoardDynamicResizeUI;
import com.vaadin.testbench.By;
import com.vaadin.testbench.elements.ButtonElement;

public class BoardDynamicResizeIT extends AbstractParallelTest {

    @Override
    protected Class<?> getUIClass() {
        return BoardDynamicResizeUI.class;
    }

    @Test
    public void basicLayout_boardTabletSize_twoRowsAndTwoItemsInRow() {
        WebElement board = getDriver().findElement(By.tagName("vaadin-board"));
        List<WebElement> rowChildren = board.findElements(By.xpath("//vaadin-board/vaadin-board-row/*"));
        ButtonElement resizeButton = $(ButtonElement.class).caption("resize").first();
        resizeButton.click();

        Assert.assertEquals("Lbl1 should have same Y as the board Y coordinate",
            board.getLocation().getY(), rowChildren.get(0).getLocation().getY());

        Assert.assertEquals("Lbl2 should have Y == board.location + lbl1.height",
            board.getLocation().getY() + rowChildren.get(0).getSize().getHeight(),
            rowChildren.get(1).getLocation().getY());

        Assert.assertEquals("Lbl3 should have Y == lbl2.location + lbl1.height + lbl2.height",
            rowChildren.get(1).getLocation().getY() + rowChildren.get(2).getSize().getHeight(),
            rowChildren.get(2).getLocation().getY());
    }

}
