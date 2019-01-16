/**
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the license along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 */
package com.vaadin.flow.component.button.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-button&gt;</code>
 * element.
 */
@Element("vaadin-button")
public class ButtonElement extends TestBenchElement {
    @Override
    public String getText() {
        // The default implementation seems to use innerText, which adds a lot
        // of whitespace in Edge
        return getPropertyString("textContent");
    }
}
