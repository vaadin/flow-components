
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Route("vaadin-combo-box/auto-focus-filter")
public class AutoFocusFilterPage extends Div {

    public AutoFocusFilterPage() {
        List<String> data = Arrays.asList("Option 2", "Option 3", "Option 4",
                "Option 5", "Another Option 2");

        ComboBox<String> comboBox = new ComboBox<>("Choose option");
        comboBox.setDataProvider((filter, offset, limit) -> {
            if (filter.isEmpty())
                return Stream.of("");
            return data.stream().filter(s -> s.contains(filter)).skip(offset)
                    .limit(limit);
        }, (filter) -> {
            if (filter.isEmpty())
                return 1;
            return (int) data.stream().filter(s -> s.contains(filter)).count();
        });

        comboBox.setAutofocus(true);
        this.add(comboBox);
    }
}
