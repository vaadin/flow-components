/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.avatar.testbench;

import org.openqa.selenium.NoSuchElementException;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-avatar-group&gt;</code>
 * element.
 */
@Element("vaadin-avatar-group")
public class AvatarGroupElement extends TestBenchElement {

    /**
     * Gets the avatar element for the avatar with the given index.
     *
     * @param index
     *            the index to look for in the avatar group
     * @return the first avatar element which matches the given index
     * @throws NoSuchElementException
     *             if no match was found
     */
    public AvatarElement getAvatarElement(int index)
            throws NoSuchElementException {
        return $(AvatarElement.class).get(index);
    }

    /**
     * Gets the aria label that is pronounced by the screen reader.
     *
     * @return the aria label
     */
    public String getAriaLabel() {
        return getAttribute("aria-label");
    }

}
