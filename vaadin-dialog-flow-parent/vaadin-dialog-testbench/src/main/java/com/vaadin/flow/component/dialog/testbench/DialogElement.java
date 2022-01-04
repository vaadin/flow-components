/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.dialog.testbench;

import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-dialog&gt;</code>
 * element.
 */
@Element("vaadin-dialog")
public class DialogElement extends TestBenchElement {

    /**
     * Checks whether the dialog is shown.
     *
     * @return <code>true</code> if the dialog is shown, <code>false</code>
     *         otherwise
     */
    public boolean isOpen() {
        try {
            return getPropertyBoolean("opened");
        } catch (StaleElementReferenceException e) {
            // The element is no longer even attached to the DOM
            // -> it's not open
            return false;
        }
    }

    @Override
    public SearchContext getContext() {
        // Find child elements inside the overlay, not the dialog element
        return getOverlay();
    }

    /**
     * Gets the overlay element connected to the dialog.
     * <p>
     * The overlay contains the content of the dialog but is not a child element
     * of the dialog element.
     *
     * @return the overlay element
     */
    private TestBenchElement getOverlay() {
        try {
            return getPropertyElement("$", "overlay", "content");
        } catch (JavascriptException e) {
            // Overlay content can be the overlay itself or the shadowRoot of
            // the content part
            // https://github.com/vaadin/vaadin-overlay/blob/master/src/vaadin-overlay.html#L837-L841
            // return shadowRoot is not supported in WebDriver and doesn't work
            // in Firefox
            return getPropertyElement("$", "overlay", "$", "content");
        }
    }

}
