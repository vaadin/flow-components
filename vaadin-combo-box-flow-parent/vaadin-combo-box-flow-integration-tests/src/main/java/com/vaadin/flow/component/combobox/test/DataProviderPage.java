/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.bean.TestItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/data-provider")
public class DataProviderPage extends Div {
    static final String COMBO_BOX_WITH_GET_ID_ID = "comboBoxWithGetId";
    static final String COMBO_BOX_WITHOUT_GET_ID_ID = "comboBoxWithoutGetId";
    static final String SET_VALUE_USING_GET_ID_BUTTON_ID = "setValueUsingGetIdButton";
    static final String SET_VALUE_USING_REFERENCE_BUTTON_ID = "setValueUsingReferenceButton";
    static final String SET_VALUE_USING_EQUALS_BUTTON_ID = "setValueUsingEqualsButton";

    private transient List<TestItem> list = Arrays.asList(
            new TestItem(1, "a", "First"), new TestItem(2, "b", "Second"),
            new TestItem(3, "c", "Third"));

    public DataProviderPage() {
        createDataProviderWithGetId();
        createDataProviderWithoutGetId();
        createRefreshWithSmallerDataSet();
    }

    private void createDataProviderWithGetId() {
        ComboBox<TestItem> comboBoxWithGetId = new ComboBox<>();
        comboBoxWithGetId.setId(COMBO_BOX_WITH_GET_ID_ID);
        comboBoxWithGetId.setDataProvider(new ListDataProvider<TestItem>(list) {
            @Override
            public Object getId(TestItem item) {
                return item.getId();
            }
        });
        add(comboBoxWithGetId);

        NativeButton setValueUsingIdButton = new NativeButton(
                "Set Value Using Id",
                event -> comboBoxWithGetId.setValue(new TestItem(2)));
        setValueUsingIdButton.setId(SET_VALUE_USING_GET_ID_BUTTON_ID);
        add(setValueUsingIdButton);
    }

    private void createDataProviderWithoutGetId() {
        add(new Hr());
        ComboBox<TestItem> comboBoxWithoutGetId = new ComboBox<>();
        comboBoxWithoutGetId.setId(COMBO_BOX_WITHOUT_GET_ID_ID);
        comboBoxWithoutGetId.setItems(list);
        add(comboBoxWithoutGetId);

        NativeButton setValueUsingReferenceButton = new NativeButton(
                "Set Value Using Reference",
                event -> comboBoxWithoutGetId.setValue(list.get(1)));
        setValueUsingReferenceButton.setId(SET_VALUE_USING_REFERENCE_BUTTON_ID);
        add(setValueUsingReferenceButton);

        NativeButton setValueUsingEqualsButton = new NativeButton(
                "Set Value Using Equals", event -> comboBoxWithoutGetId
                        .setValue(new TestItem(4, "c", "")));
        setValueUsingEqualsButton.setId(SET_VALUE_USING_EQUALS_BUTTON_ID);
        add(setValueUsingEqualsButton);
    }

    private void createRefreshWithSmallerDataSet() {
        add(new Hr());

        ComboBox<String> cb = new ComboBox<>();
        cb.setId("combo-box-with-reduce-data-set");

        List<String> items = new ArrayList<>();
        items.add("foo");
        items.add("bar");
        DataProvider<String, String> dp = DataProvider.fromFilteringCallbacks(
                q -> items.stream().skip(q.getOffset()).limit(q.getLimit()),
                q -> items.size());
        cb.setDataProvider(dp);

        NativeButton refreshAllWithSmallerDataSetButton = new NativeButton(
                "Refresh all with smaller data set", event -> {
                    items.remove("foo");
                    dp.refreshAll();
                });
        refreshAllWithSmallerDataSetButton
                .setId("refresh-all-with-smaller-data-set");

        add(cb, refreshAllWithSmallerDataSetButton);
    }
}
