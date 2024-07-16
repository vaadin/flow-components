/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.menubar.testbench;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-menu-bar&gt;</code>
 * element.
 */
@Element("vaadin-menu-bar")
public class MenuBarElement extends TestBenchElement {

    /**
     * Gets the button elements wrapping the root level items. This does not
     * include the overflowing items which are rendered in a sub menu, nor the
     * overflow button which opens the sub menu.
     *
     * @return the button elements in the menu bar
     */
    public List<TestBenchElement> getButtons() {
        return $("vaadin-menu-bar-button").all().stream().filter(
                element -> !isOverflowButton(element) && isVisible(element))
                .collect(Collectors.toList());
    }

    /**
     * Gets the button which opens the sub menu of overflowing items, or
     * {@code null} if the overflow button is not visible.
     *
     * @return the button which opens the sub menu of overflowing items
     */
    public TestBenchElement getOverflowButton() {
        TestBenchElement overflowButton = $("[part~=overflow-button]").first();
        if (overflowButton == null || overflowButton.hasAttribute("hidden")) {
            return null;
        }
        return overflowButton;
    }

    private boolean isOverflowButton(TestBenchElement element) {
        return element.getAttribute("part").contains("overflow-button");
    }

    private boolean isVisible(TestBenchElement element) {
        return (boolean) executeScript(
                "return arguments[0].style.visibility !== 'hidden'", element);
    }

}
