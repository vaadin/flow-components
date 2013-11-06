package com.vaadin.addon.spreadsheet.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CommentTest extends Test1 {

    @Test
    public void testFromUpload() {
        loadSheetFile("cell_comments.xlsx");
        commonAssertions();
    }

    @Test
    public void testFromAPI() {
        loadServerFixture("COMMENTS");
        commonAssertions();
    }

    private void commonAssertions() {
        assertCommentPresent("Always Visible Comment.");

        assertCommentNotPresent("first cell comment");
        mouse.toolTip(c.getCellElement("A1"));
        assertCommentPresent("first cell comment");
    }

    private void assertCommentPresent(String text) {
        Assert.assertTrue(
                "Comment not present",
                findAllByXPath(
                        "//div[@class='comment-overlay-label' and contains(text(), '"
                                + text + "')]").size() > 0);
    }

    private void assertCommentNotPresent(String text) {
        Assert.assertTrue(
                "Comment found while expecting not",
                findAllByXPath(
                        "//div[@class='comment-overlay-label' and contains(text(), '"
                                + text + "')]").size() == 0);
    }

    private List<WebElement> findAllByXPath(String xpath) {
        return driver.findElements(By.xpath(xpath));
    }
}
