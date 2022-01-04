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
