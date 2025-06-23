/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.tabs.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import com.vaadin.flow.component.tabs.testbench.TabSheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link TabSheetPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-tabs/tabsheet")
public class TabSheetIT extends AbstractComponentIT {

    TabSheetElement tabSheet;

    @Before
    public void init() {
        open();
        tabSheet = $(TabSheetElement.class).first();
    }

    @Test
    public void init_shouldHaveFirstTabSelected() {
        Assert.assertEquals(0, tabSheet.getSelectedTabIndex());
    }

    @Test
    public void setSelectedIndex_shouldHaveSecondTabSelected() {
        tabSheet.setSelectedTabIndex(1);
        Assert.assertEquals(1, tabSheet.getSelectedTabIndex());
    }

    @Test
    public void setSelectedIndex_shouldHaveExpectedTextInSelectedTab() {
        tabSheet.setSelectedTabIndex(1);
        Assert.assertEquals("Tab two",
                tabSheet.getSelectedTabElement().getText());
    }

    @Test
    public void setSelectedIndex_shouldGetTabByTextContent() {
        tabSheet.setSelectedTabIndex(1);
        Assert.assertEquals(tabSheet.getSelectedTabElement(),
                tabSheet.getTabElement("Tab two"));
    }

    @Test
    public void setSelectedIndex_shouldThrowOnGetTabByNonexistentTextContent() {
        Assert.assertThrows(NoSuchElementException.class,
                () -> tabSheet.getTabElement("Tab foo"));
    }

    @Test
    public void shouldGetTabIndexByTextContent() {
        Assert.assertEquals(0, tabSheet.getTab("Tab one"));
        Assert.assertEquals(1, tabSheet.getTab("Tab two"));
    }

    @Test
    public void shouldNotGetTabIndexByNonexistentTextContent() {
        Assert.assertEquals(-1, tabSheet.getTab("Tab foo"));
    }

    @Test
    public void shouldGetTheContentOfSelectedTab() {
        Assert.assertEquals("Tab one content", tabSheet.getContent().getText());
    }

    @Test
    public void unselect_shouldThrowOnGetContent() {
        tabSheet.setSelectedTabIndex(-1);
        Assert.assertThrows(NoSuchElementException.class,
                () -> tabSheet.getContent());
    }
}
