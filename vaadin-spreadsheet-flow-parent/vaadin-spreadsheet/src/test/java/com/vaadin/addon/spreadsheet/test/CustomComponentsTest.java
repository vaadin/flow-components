package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.server.browserlaunchers.Sleeper;
import org.openqa.selenium.support.ui.Select;

public class CustomComponentsTest extends Test1 {

    final static String TEXT_PROXY = "text";
    final static Integer NUM_PROXY = 42;

    @Test
    public void testTextField() {
        loadServerFixture("CUSTOM_COMPONENTS");

        c.selectCell("B2");
        driver.findElement(By.xpath(c.cellToXPath("B2") + "/input")).click();
        testBench(driver).waitForVaadin();
        c.insertAndRet(TEXT_PROXY);

        c.selectCell("B3");
        c.insertAndRet("=B2");

        Assert.assertEquals(c.getCellContent("B2"), TEXT_PROXY);
        Assert.assertEquals(c.getCellContent("B3"), TEXT_PROXY);

        c.selectCell("B2");
        driver.findElement(By.xpath(c.cellToXPath("B2") + "/input")).clear();
        driver.findElement(By.xpath(c.cellToXPath("B2") + "/input")).click();
        testBench(driver).waitForVaadin();
        c.insertAndRet(NUM_PROXY.toString());

        c.selectCell("B3");
        c.insertAndRet("=B2*2");

        Assert.assertEquals(c.getCellContent("B2"), NUM_PROXY.toString());
        Assert.assertEquals(c.getCellContent("B3"), (NUM_PROXY * 2) + "");
    }

    @Test
    public void testCheckBox() {
        loadServerFixture("CUSTOM_COMPONENTS");

        c.selectCell("C3");
        c.insertAndRet("=C2*2");
        c.insertAndRet("=IF(C2,1,0)");

        Assert.assertEquals(c.getCellContent("C3"), "0");
        Assert.assertEquals(c.getCellContent("C4"), "0");

        c.selectCell("C2");
        driver.findElement(By.xpath(c.cellToXPath("C2") + "//input")).click();
        testBench(driver).waitForVaadin();

        Assert.assertEquals(c.getCellContent("C3"), "2");
        Assert.assertEquals(c.getCellContent("C4"), "1");
    }

    @Test
    public void testNativeSelect() {
        loadServerFixture("CUSTOM_COMPONENTS");

        c.selectCell("I3");
        c.insertAndRet("=I2*3");

        c.selectCell("I2");
        Select select = new Select(driver.findElement(By.xpath(c
                .cellToXPath("I2") + "//select")));
        select.getOptions().get(3).click();
        testBench(driver).waitForVaadin();

        Assert.assertEquals(c.getCellContent("I3"), "90");
    }

    @Test
    public void testScrollingBug() {
        loadServerFixture("CUSTOM_COMPONENTS");

        c.selectCell("B2");
        driver.findElement(By.xpath(c.cellToXPath("B2") + "/input")).click();
        testBench(driver).waitForVaadin();
        c.insertAndRet(TEXT_PROXY);
        c.selectCell("B3");
        c.selectCell("B2");

        Assert.assertEquals(
                driver.findElement(By.xpath(c.cellToXPath("B2") + "/input"))
                        .getAttribute("value"), TEXT_PROXY);
        testBench(driver).waitForVaadin();
        c.selectCell("B5");
        testBench(driver).waitForVaadin();
        c.navigateToCell("B100");
        testBench(driver).waitForVaadin();

        Sleeper.sleepTightInSeconds(1);
        c.navigateToCell("B1");
        Sleeper.sleepTightInSeconds(3);
        testBench(driver).waitForVaadin();
        c.selectCell("B2");

        testBench(driver).waitForVaadin();
        Assert.assertEquals(
                driver.findElement(By.xpath(c.cellToXPath("B2") + "/input"))
                        .getAttribute("value"), TEXT_PROXY);
    }

    @Test
    public void testButtonHandling() {
        loadServerFixture("CUSTOM_COMPONENTS");

        driver.findElement(By.id("b10-btn")).click();
        testBench(driver).waitForVaadin();
        Assert.assertEquals(c.getCellContent("B11"), "42");
        Assert.assertEquals(driver.findElement(By.id("b12-label")).getText(),
                "b12");
    }

}
