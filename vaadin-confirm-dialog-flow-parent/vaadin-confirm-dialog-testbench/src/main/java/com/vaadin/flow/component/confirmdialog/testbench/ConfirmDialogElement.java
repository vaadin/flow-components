package com.vaadin.flow.component.confirmdialog.testbench;

/*
 * #%L
 * Vaadin Confirm Dialog Testbench API
 * %%
 * Copyright (C) 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-confirm-dialog")
public class ConfirmDialogElement extends TestBenchElement {

    private TestBenchElement getOverlayContext() {
        return $("vaadin-dialog-overlay").onPage().last().$(TestBenchElement.class).id("content");
    }

    private TestBenchElement getButton(String buttonId, String slotName) {
        ElementQuery<TestBenchElement> query = getOverlayContext().$(TestBenchElement.class)
                .attribute("slot", slotName);
        if (query.exists()) {
            return query.first();
        }

        return getOverlayContext().$(ButtonElement.class).id(buttonId);
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
