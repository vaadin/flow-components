package com.vaadin.flow.component.textfield.testbench;

import com.vaadin.testbench.TestBenchElement;

class TextFieldElementHelper {
    static void setValue(TestBenchElement element, String value) {
        element.getCommandExecutor().executeScript(
                "arguments[0].inputElement.value = arguments[1]", element,
                value);
        element.getCommandExecutor().executeScript(
                "arguments[0].inputElement.dispatchEvent(new CustomEvent('input', { bubbles: true, composed: true}))",
                element);
        element.getCommandExecutor().executeScript(
                "arguments[0].inputElement.dispatchEvent(new CustomEvent('change', { bubbles: true }))",
                element);
    }
}
