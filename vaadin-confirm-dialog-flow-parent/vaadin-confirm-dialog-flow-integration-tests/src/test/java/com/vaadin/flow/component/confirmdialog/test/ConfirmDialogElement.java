package com.vaadin.flow.component.confirmdialog.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-confirm-dialog")
public class ConfirmDialogElement extends TestBenchElement {

    private TestBenchElement getOverlayContext() {
        return $("vaadin-dialog-overlay").onPage().first().$(TestBenchElement.class).id("content");
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
}
