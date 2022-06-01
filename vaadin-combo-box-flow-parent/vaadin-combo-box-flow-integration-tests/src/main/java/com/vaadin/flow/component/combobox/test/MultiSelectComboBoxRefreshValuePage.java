package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("vaadin-multi-select-combo-box/refresh-value")
public class MultiSelectComboBoxRefreshValuePage extends Div {
    public MultiSelectComboBoxRefreshValuePage() {
        MultiSelectComboBox<TestBean> comboBox = new MultiSelectComboBox<>(
                "Items");
        List<TestBean> items = IntStream.range(0, 100)
                .mapToObj(i -> new TestBean("Item " + (i + 1)))
                .collect(Collectors.toList());
        comboBox.setItems(items);
        comboBox.setItemLabelGenerator(TestBean::getName);
        // Make component wider, so that we can fit multiple chips
        comboBox.setWidth("500px");

        NativeButton changeItemLabelGenerator = new NativeButton(
                "Change item label generator", e -> {
                    comboBox.setItemLabelGenerator(
                            item -> "Custom " + item.getName());
                });
        changeItemLabelGenerator.setId("change-item-label-generator");

        NativeButton changeItemData = new NativeButton("Change item data",
                e -> {
                    items.forEach(
                            item -> item.setName("Updated " + item.getName()));
                    comboBox.getDataProvider().refreshAll();
                });
        changeItemData.setId("change-item-data");

        add(comboBox);
        add(new Div(changeItemLabelGenerator, changeItemData));
    }

    private static class TestBean {
        private String name;

        public TestBean(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
