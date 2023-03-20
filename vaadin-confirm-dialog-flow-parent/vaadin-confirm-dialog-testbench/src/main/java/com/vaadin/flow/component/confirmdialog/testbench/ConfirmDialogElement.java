/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.confirmdialog.testbench;

/*
 * #%L
 * Vaadin Confirm Dialog Testbench API
 * %%
 * Copyright (C) 2018 - 2020 Vaadin Ltd
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
