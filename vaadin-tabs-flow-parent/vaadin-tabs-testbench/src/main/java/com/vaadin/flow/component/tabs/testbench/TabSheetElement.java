/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.tabs.testbench;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-tabsheet&gt;</code>
 * element.
 */
@Element("vaadin-tabsheet")
public class TabSheetElement extends TestBenchElement {

    /**
     * Selects the tab with the given index.
     *
     * @param selectedTab
     *            the index of the tab to select
     */
    public void setSelectedTabIndex(int selectedTab) {
        setProperty("selected", selectedTab);
    }

    /**
     * Gets the index of the currently selected tab.
     *
     * @return the index of the currenly selected tab
     */
    public int getSelectedTabIndex() {
        return getPropertyInteger("selected");
    }

    /**
     * Gets the tab element for the currently selected tab.
     *
     * @return a tab element for the currently selected tab
     */
    public TabElement getSelectedTabElement() {
        return getTabs().getSelectedTabElement();
    }

    /**
     * Gets the tab element for the tab with the given text.
     *
     * @param text
     *            the text to look for in the tabs
     * @return the first tab element which matches the given text
     * @throws NoSuchElementException
     *             if no match was found
     */
    public TabElement getTabElement(String text) throws NoSuchElementException {
        return getTabs().getTabElement(text);
    }

    /**
     * Gets the index of the tab with the given text.
     *
     * @param text
     *            the text to look for in the tabs
     * @return the index of the first tab element which matches the given text
     *         or -1 if no match was found
     */
    public int getTab(String text) {
        return getTabs().getTab(text);
    }

    /**
     * Gets the the content related to the currently selected tab.
     *
     * @return the content of the currently selected tab.
     * @throws NoSuchElementException
     *             if no content is visible (no selection)
     */
    public TestBenchElement getContent() throws NoSuchElementException {
        return findElement(By.cssSelector("[tab]:not([hidden])"));
    }

    /**
     * Gets the tabs element.
     *
     * @return the tabs element
     */
    public TabsElement getTabs() {
        return $(TabsElement.class).first();
    }

}
