/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/refresh-empty-lazy-data-provider")
public class RefreshEmptyLazyDataProviderPage extends Div {
    public RefreshEmptyLazyDataProviderPage() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(new AbstractBackEndDataProvider<>() {
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
