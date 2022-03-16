package com.vaadin.flow.component.treegrid.it;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

import static org.junit.Assert.assertFalse;

@TestPath("vaadin-grid/" + TreeGridBasicFeaturesPage.VIEW)
public class TreeGridExpandDataRequestIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();

        setupTreeGrid();
        findElement(By.id("LoggingDataProvider")).click();
        clickClearLog();
    }

    private void clickClearLog() {
        findElement(By.id(makeId("Clear log"))).click();
    }

    @Test
    public void expand_node0_does_not_request_root_nodes() {
        getTreeGrid().expandWithClick(0);
        assertFalse("Log should not contain request for root nodes.",
                logContainsText("Root node request: "));
    }

    @Test
    public void expand_node0_after_node1_does_not_request_children_of_node1() {
        getTreeGrid().expandWithClick(1);
        assertFalse("Log should not contain request for root nodes.",
                logContainsText("Root node request: "));
        clickClearLog();
        getTreeGrid().expandWithClick(0);
        assertFalse("Log should not contain request for children of '0 | 1'.",
                logContainsText("Children request: 0 | 1"));
        assertFalse("Log should not contain request for root nodes.",
                logContainsText("Root node request: "));
    }
}
