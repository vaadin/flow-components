/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.tabs.testbench;

import java.util.List;

import org.openqa.selenium.NoSuchElementException;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-tabs&gt;</code> element.
 */
@Element("vaadin-tabs")
public class TabsElement extends TestBenchElement {

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
        return ((TestBenchElement) executeScript(
                "return arguments[0].children[arguments[0].selected];", this))
                .wrap(TabElement.class);
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
        int index = getTab(text);
        if (index == -1) {
            throw new NoSuchElementException(
                    "No tab with text '" + text + "' found");
        }
        return $(TabElement.class).get(index);
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
        List<TabElement> children = getTabs();
        for (int i = 0; i < children.size(); i++) {
            String tabLabel = children.get(i).getLabel();
            if (text.equals(tabLabel)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the child tabs.
     *
     * @return the list of child tab elements. Not null, may be empty.
     */
    public List<TabElement> getTabs() {
        List<TestBenchElement> children = getChildren();
        return children.stream().map(it -> it.wrap(TabElement.class)).toList();
    }

    /**
     * Returns the labels of all tab elements.
     *
     * @return a list of tab labels, one for every tab. Not null, may be
     *         empty.
     */
    public List<String> getTabLabels() {
        final List<TabElement> tabElements = getTabs();
        return tabElements.stream().map(TabElement::getLabel).toList();
    }
}
