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
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/range-data-provider")
public class RangeDataProviderPage extends Div {
    public RangeDataProviderPage() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(generateItems(1000));
        add(comboBox);
    }

    private List<String> generateItems(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> "Item " + i).collect(Collectors.toList());
    }}
