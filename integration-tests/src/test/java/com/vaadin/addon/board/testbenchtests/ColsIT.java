package com.vaadin.addon.board.testbenchtests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.board.testUI.ColsUI;
import com.vaadin.testbench.By;
import com.vaadin.testbench.elements.ButtonElement;

/**
 *
 */
public class ColsIT extends AbstractParallelTest {

    @Override
    protected Class<?> getUIClass() {
        return ColsUI.class;
    }

    @Test
    public void removeColsTest()
        throws Exception {
        WebElement board = getDriver().findElement(By.tagName("vaadin-board"));
        List<WebElement> rows = board.findElements(By.xpath("//vaadin-board/vaadin-board-row"));
        Assert.assertEquals(1, rows.size());

        ButtonElement btn = $(ButtonElement.class).caption("remove").first();

        int widthOld = $(ButtonElement.class).caption("Button A").first().getSize().getWidth();
        btn.click();
        int widthNew = $(ButtonElement.class).caption("Button A").first().getSize().getWidth();
        Assert.assertTrue(widthOld > widthNew);
    }

    @Test
    public void exceptionColsTest()
        throws Exception {
        WebElement board = getDriver().findElement(By.tagName("vaadin-board"));
        List<WebElement> rows = board.findElements(By.xpath("//vaadin-board/vaadin-board-row"));
        Assert.assertEquals(1, rows.size());

        ButtonElement btn = $(ButtonElement.class).caption("exception").first();

        int widthOld = $(ButtonElement.class).caption("Button A").first().getSize().getWidth();
        btn.click();
        int widthNew = $(ButtonElement.class).caption("Button A").first().getSize().getWidth();
        Assert.assertTrue(widthOld == widthNew);
    }

}
