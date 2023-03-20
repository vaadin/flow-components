
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/detach-reattach")
public class DetachReattachPage extends Div {

    public DetachReattachPage() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems("foo", "bar");

        NativeButton detach = new NativeButton("detach", e -> remove(comboBox));
        detach.setId("detach");

        NativeButton attach = new NativeButton("attach", e -> add(comboBox));
        attach.setId("attach");

        NativeButton attachDetach = new NativeButton("attach-detach", e -> {
            add(comboBox);
            remove(comboBox);
        });
        attachDetach.setId("attach-detach");

        NativeButton setValue = new NativeButton("set value foo",
                e -> comboBox.setValue("foo"));
        setValue.setId("set-value");

        Div valueChanges = new Div();
        valueChanges.setId("value-changes");
        comboBox.addValueChangeListener(e -> {
            valueChanges.add(new Paragraph(e.getValue()));
        });

        add(comboBox, detach, attach, attachDetach, setValue, valueChanges);
    }
}
