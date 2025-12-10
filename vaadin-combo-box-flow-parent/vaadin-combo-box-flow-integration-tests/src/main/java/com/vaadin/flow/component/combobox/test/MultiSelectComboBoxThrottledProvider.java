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

import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

@Route("multi-select-combo-box-throttled-provider")
public class MultiSelectComboBoxThrottledProvider extends Div {

    public MultiSelectComboBoxThrottledProvider() {
        setSizeFull();
        var comboBox = new MultiSelectComboBox<String>();
        comboBox.setItems(DataProvider.fromFilteringCallbacks(query -> {
            int offset = query.getOffset();
            int limit = query.getLimit();
            int end = Math.min(offset + limit, 1000);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return IntStream.range(offset, end)
                    .mapToObj(i -> "Item " + (i + 1));
        }, query -> 1000));
        add(comboBox);
    }
}
