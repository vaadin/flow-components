/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.avatar.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-avatar&gt;</code>
 * element.
 */
@Element("vaadin-avatar")
public class AvatarElement extends TestBenchElement {

    @Override
    public boolean isEnabled() {
        return !getPropertyBoolean("disabled");
    }

    /**
     * Gets the title displayed as a tooltip.
     *
     * @return the tooltip text
     */
    public String getTitle() {
        return getAttribute("title");
    }

    /**
     * Gets the abbreviation displayed in the avatar.
     *
     * @return the abbreviation
     */
    public String getAbbr() {
        return getPropertyString("abbr");
    }

}
