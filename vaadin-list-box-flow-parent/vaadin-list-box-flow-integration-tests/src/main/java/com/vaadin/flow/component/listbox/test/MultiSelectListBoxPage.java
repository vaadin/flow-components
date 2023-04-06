
package com.vaadin.flow.component.listbox.test;

import java.util.HashSet;
import java.util.Set;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.router.Route;

@Route("vaadin-list-box/multi-select")
public class MultiSelectListBoxPage extends Div {

    public MultiSelectListBoxPage() {
        MultiSelectListBox<String> listbox = new MultiSelectListBox<>();
        listbox.setItems("foo", "bar", "baz", "qux");

        Span fromClientSpan = new Span();
        fromClientSpan.setId("fromClient");

        Div valueChanges = new Div();
        valueChanges.add(new Text("value:"));
        valueChanges.setId("valueChanges");

        listbox.addValueChangeListener(e -> {
            valueChanges.add(new Paragraph(formatValue(e.getValue())));
            fromClientSpan.setText(e.isFromClient() + "");
        });

        Set<String> valueToSet = new HashSet<>();
        valueToSet.add("bar");
        valueToSet.add("qux");
        NativeButton setValueButton = new NativeButton("set value bar qux",
                e -> listbox.setValue(valueToSet));
        setValueButton.setId("setValue");

        add(listbox, setValueButton,
                new Div(new Span("fromClient: "), fromClientSpan),
                valueChanges);
    }

    private String formatValue(Set<String> value) {
        return String.join(", ", value);
    }

}
