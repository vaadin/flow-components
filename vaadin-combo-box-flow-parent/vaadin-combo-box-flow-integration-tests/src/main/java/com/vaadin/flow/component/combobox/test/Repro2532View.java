/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/2532.
 *
 * Setting the ComboBox items as a FetchCallback inside a
 * CustomValueSetListener reportedly leaves the dropdown stuck in the loading
 * state when a custom value is committed (type + enter) before the fetch
 * finishes; reopening keeps showing the spinner instead of the "foo" item.
 */
@Route("repro-2532")
public class Repro2532View extends Div {

    public Repro2532View() {
        ComboBox<String> cb = new ComboBox<>();
        cb.getElement().setAttribute("id", "combo");
        setItems(cb);
        cb.addCustomValueSetListener(event -> setItems(cb));
        add(cb);
    }

    private void setItems(ComboBox<String> cb) {
        cb.setItems(query -> Stream.of("foo").skip(query.getOffset())
                .limit(query.getLimit()));
    }
}
