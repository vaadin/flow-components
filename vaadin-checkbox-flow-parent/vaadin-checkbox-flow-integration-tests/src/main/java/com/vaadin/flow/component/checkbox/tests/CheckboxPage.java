
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test view for {@link Checkbox}.
 */
@Route("vaadin-checkbox/checkbox-test")
public class CheckboxPage extends Div {

    public static class Bean {
        private Boolean property;

        public Boolean isProperty() {
            return property;
        }

        public void setProperty(Boolean property) {
            this.property = property;
        }
    }

    /**
     * Creates a new instance.
     */
    public CheckboxPage() {
        add(createInitialValueTest(false, false),
                createInitialValueTest(true, false),
                createInitialValueTest(false, true),
                createInitialValueTest(true, true));
    }

    // https://github.com/vaadin/vaadin-checkbox-flow/issues/22
    private Component createInitialValueTest(boolean checked,
            boolean indeterminate) {
        int id = checked ? 1 : 0;
        id += indeterminate ? 2 : 0;

        Checkbox checkbox2 = new Checkbox("initial value cb");
        checkbox2.setValue(checked);
        checkbox2.setIndeterminate(indeterminate);
        checkbox2.setId("cb-" + id);

        Label valueLabel = new Label("Value: " + checkbox2.getValue());
        valueLabel.setId("value-label-" + id);

        Label indeterminateLabel = new Label(
                "Indeterminate: " + checkbox2.isIndeterminate());
        indeterminateLabel.setId("indeterminate-label-" + id);

        AtomicInteger checkedCounter = new AtomicInteger();

        checkbox2.addValueChangeListener(event -> {
            valueLabel.setText("Value: " + checkedCounter.incrementAndGet()
                    + " " + event.getValue());
        });

        AtomicInteger indeterminateCounter = new AtomicInteger();

        checkbox2.getElement().addPropertyChangeListener("indeterminate",
                event -> indeterminateLabel.setText("Indeterminate: "
                        + indeterminateCounter.incrementAndGet() + " "
                        + event.getValue()));

        return new Div(checkbox2, valueLabel, indeterminateLabel);
    }
}
