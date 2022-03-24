package com.vaadin.flow.component.confirmdialog.testbench;

/*
 * #%L
 * Vaadin Confirm Dialog Testbench API
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import org.openqa.selenium.SearchContext;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-confirm-dialog")
public class ConfirmDialogElement extends TestBenchElement {

    @Override
    public SearchContext getContext() {
        return getPropertyElement("$", "dialog", "$", "overlay");
    }

    public TestBenchElement getConfirmButton() {
        return getPropertyElement("_confirmButton");
    }

    public TestBenchElement getRejectButton() {
        return getPropertyElement("_rejectButton");
    }

    public TestBenchElement getCancelButton() {
        return getPropertyElement("_cancelButton");
    }

    public String getMessageText() {
        return getPropertyString("message");
    }

    public String getHeaderText() {
        return getPropertyString("header");
    }
}
