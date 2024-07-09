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

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-confirm-dialog")
public class ConfirmDialogElement extends TestBenchElement {

    @Override
    public SearchContext getContext() {
        return getPropertyElement("$", "dialog", "$", "overlay", "$",
                "content");
    }

    private TestBenchElement getButton(String buttonId, String slotName) {
        ElementQuery<TestBenchElement> query = $(TestBenchElement.class)
                .attribute("slot", slotName);
        if (query.exists()) {
            return query.first();
        }

        return $(ButtonElement.class).id(buttonId);
    }

    public TestBenchElement getConfirmButton() {
        return getButton("confirm", "confirm-button");
    }

    public TestBenchElement getRejectButton() {
        return getButton("reject", "reject-button");
    }

    public TestBenchElement getCancelButton() {
        return getButton("cancel", "cancel-button");
    }

    public String getMessageText() {
        return getPropertyString("message");
    }

    public String getHeaderText() {
        return getPropertyString("header");
    }
}
