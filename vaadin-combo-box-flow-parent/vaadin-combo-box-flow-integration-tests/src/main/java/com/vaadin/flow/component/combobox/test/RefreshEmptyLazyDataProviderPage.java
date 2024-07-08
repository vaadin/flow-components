/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

import java.util.stream.Stream;

@Route("vaadin-combo-box/refresh-empty-lazy-data-provider")
public class RefreshEmptyLazyDataProviderPage extends Div {
    public RefreshEmptyLazyDataProviderPage() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setDataProvider(
                new AbstractBackEndDataProvider<String, String>() {
                    @Override
                    protected Stream<String> fetchFromBackEnd(
                            Query<String, String> query) {
                        return Stream.of();
                    }

                    @Override
                    protected int sizeInBackEnd(Query<String, String> query) {
                        return 0;
                    }
                });

        NativeButton refreshDataProvider = new NativeButton("Refresh", e -> {
            comboBox.getDataProvider().refreshAll();
        });
        refreshDataProvider.setId("refresh-data-provider");

        add(comboBox, refreshDataProvider);
    }
}
