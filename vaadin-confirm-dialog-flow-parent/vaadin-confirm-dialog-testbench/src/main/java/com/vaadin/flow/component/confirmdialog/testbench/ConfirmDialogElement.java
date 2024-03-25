/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.confirmdialog.testbench;

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
