package com.vaadin.flow.component.textfield.testbench;

import java.util.Collections;

import com.vaadin.testbench.TestBenchElement;

class TextFieldElementHelper {
    static void setValue(TestBenchElement element, String value) {
        element.focus();
        element.setProperty("value", value);
        element.dispatchEvent("input",
                Collections.singletonMap("bubbles", true));
        element.dispatchEvent("change",
                Collections.singletonMap("bubbles", true));
        element.getCommandExecutor().executeScript("arguments[0].blur()", element);
    }
}
