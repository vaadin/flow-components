/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox;

import com.vaadin.flow.component.HasClearButton;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Tag("vaadin-multi-select-combo-box")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta4")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/multi-select-combo-box", version = "23.1.0-beta4")
@JsModule("@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box.js")
@JsModule("@vaadin/polymer-legacy-adapter/template-renderer.js")
@JsModule("./flow-component-renderer.js")
@JsModule("./comboBoxConnector.js")
public class MultiSelectComboBox<TItem> extends ComboBoxBase<MultiSelectComboBox<TItem>, TItem, Set<TItem>>
        implements HasSize, HasValidation, HasHelper, HasTheme, HasLabel, HasClearButton {

    public MultiSelectComboBox() {
        this(50);
    }
    public MultiSelectComboBox(int pageSize) {
        super("selectedItems", Collections.emptySet(), JsonArray.class, MultiSelectComboBox::presentationToModel, MultiSelectComboBox::modelToPresentation);

        setPageSize(pageSize);
        setItems(new DataCommunicator.EmptyDataProvider<>());
    }

    private static <T> Set<T> presentationToModel(
            MultiSelectComboBox<T> multiSelectComboBox,
            JsonArray presentation) {

        DataKeyMapper<T> keyMapper = multiSelectComboBox.getKeyMapper();

        if (presentation == null || keyMapper == null) {
            return multiSelectComboBox.getEmptyValue();
        }

        Set<T> set = new HashSet<>();
        for (int i = 0; i < presentation.length(); i++) {
            String key = presentation.getObject(i).getString("key");
            set.add(keyMapper.get(key));
        }
        return set;
    }

    private static <T> JsonArray modelToPresentation(
            MultiSelectComboBox<T> multiSelectComboBox, Set<T> model) {
        JsonArray array = Json.createArray();
        if (model == null || model.isEmpty()) {
            return array;
        }

        model.stream().map(multiSelectComboBox::generateJson)
                .forEach(jsonObject -> array.set(array.length(), jsonObject));

        return array;
    }

    private JsonObject generateJson(TItem item) {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("key", getKeyMapper().key(item));
        getDataGenerator().generateData(item, jsonObject);
        return jsonObject;
    }

    @Override
    public void setValue(Set<TItem> value) {
        if(value == null) {
            value = Collections.emptySet();
        }
        super.setValue(value);
    }

    @Override
    protected boolean isSelected(TItem item) {
        if( item == null) return false;

        DataProvider<TItem, ?> dataProvider = getDataProvider();
        Object itemId = dataProvider.getId(item);

        return getValue().stream().anyMatch(selectedItem -> Objects.equals(itemId, dataProvider.getId(selectedItem)));
    }

    @Override
    protected void refreshValue() {
        Set<TItem> value = getValue();
        if (value == null || value.isEmpty()) {
            return;
        }
        JsonArray selectedItems = modelToPresentation(this, value);
        getElement().setPropertyJson("selectedItems", selectedItems);
    }
}
