/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

@Route("refresh-data-provider")
public class RefreshDataProviderPage extends Div {

    private List<String> nameList = new ArrayList<>();
    private ListDataProvider<String> provider = DataProvider
            .ofCollection(nameList);

    public RefreshDataProviderPage() {
        NativeButton update = new NativeButton("Update");
        update.addClickListener(event -> addNames());
        update.setId("update");

        ComboBox<String> nameSelection = new ComboBox<>("Name");
        nameSelection.setRenderer(new TextRenderer<>());
        nameSelection.setDataProvider(provider);
        add(update, nameSelection);
    }

    private void addNames() {
        nameList.add("foo");
        nameList.add("bar");
        provider.refreshAll();
    }
}
