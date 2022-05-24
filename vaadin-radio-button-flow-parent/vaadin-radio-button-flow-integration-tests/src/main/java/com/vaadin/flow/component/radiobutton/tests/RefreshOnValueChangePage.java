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
package com.vaadin.flow.component.radiobutton.tests;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.dataview.RadioButtonGroupListDataView;
import com.vaadin.flow.router.Route;

@Route("vaadin-radio-button/refresh-on-value-change")
public class RefreshOnValueChangePage extends Div {

    public RefreshOnValueChangePage() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setLabel("Label");

        List<String> items = new LinkedList<>(Arrays.asList("foo", "bar"));
        RadioButtonGroupListDataView<String> dataView = group.setItems(items);

        group.addValueChangeListener(e -> dataView.refreshAll());

        add(group);
    }
}
