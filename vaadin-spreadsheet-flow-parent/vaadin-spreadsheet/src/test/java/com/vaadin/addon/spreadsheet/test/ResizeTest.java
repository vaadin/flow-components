package com.vaadin.addon.spreadsheet.test;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;

public class ResizeTest extends Test1 {

    final String resizerDetail = "/div[@class='header-resize-dnd-second']";
    final By columnResizer = By
            .xpath("//div[@class='ch col3']" + resizerDetail);
    final By rowResizer = By.xpath("//div[@class='rh row3']" + resizerDetail);

    @Test
    public void testColumnResize() {
        // not working every time
        // new Actions(driver).clickAndHold(driver.findElement(columnResizer))
        // .moveByOffset(100, 0).release().perform();
        new Actions(driver).dragAndDrop(driver.findElement(columnResizer),
                driver.findElement(By.xpath("//div[@class='ch col5']")))
                .perform();
        assertInRange(370, getSize(c.getCellStyle("C1", "width")), 400);
    }

    @Test
    public void testRowResize() {
        c.selectCell("A1");
        new Actions(driver).dragAndDrop(driver.findElement(rowResizer),
                driver.findElement(By.xpath("//div[@class='rh row5']")))

        .perform();

        assertInRange(90, getSize(c.getCellStyle("A3", "height")), 110);
    }

    @Test
    public void testColumnAutoResize() {
        c.selectCell("B2");
        c.insertAndRet("text");

        new Actions(driver).doubleClick(
                driver.findElement(By.xpath("//div[@class='ch col2']"
                        + resizerDetail))).perform();
        testBench(driver).waitForVaadin();

        assertInRange(25, getSize(c.getCellStyle("B2", "width")), 35);

        c.selectCell("D2");
        c.insertAndRet("very long text inserted in D2.");

        new Actions(driver).doubleClick(
                driver.findElement(By.xpath("//div[@class='ch col4']"
                        + resizerDetail))).perform();
        testBench(driver).waitForVaadin();

        assertInRange(180, getSize(c.getCellStyle("D2", "width")), 220);
    }
}
