package com.vaadin.flow.component.textfield.testbench;

import java.util.Collections;

import com.vaadin.testbench.TestBenchElement;

class TextFieldElementHelper {
    static void setValue(TestBenchElement element, String value) {
        element.setProperty("value", value);
        element.dispatchEvent("input",
                Collections.singletonMap("bubbles", true));

        element.getCommandExecutor().executeScript("""
                const event = new CustomEvent('keydown', { bubbles: true });
                event.key = 'Enter';
                event.code = 'Enter';
                event.keyCode = 13;
                arguments[0].dispatchEvent(event);
                """, element);

        element.dispatchEvent("change",
                Collections.singletonMap("bubbles", true));
    }
}
