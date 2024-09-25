/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.select.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.Route;

/*
 * https://github.com/vaadin/vaadin-select-flow/issues/43
 *
 * Test that setting a renderer after detach won't cause
 * an exception on the client side.
 *
 */
@Route(value = "vaadin-select/reattach-test")
public class TestReattachView extends Div {

    public TestReattachView() {
        final Select<String> select = new Select<>();
        final SerializableFunction<String, Span> spanProvider = Span::new;
        select.setRenderer(new ComponentRenderer<>(spanProvider));
        select.setItems("a", "b", "c");
        select.setValue("a");
        add(select);

        NativeButton remove = new NativeButton("Remove", event -> {
            remove(select);
            NativeButton add = new NativeButton("Add", ev -> {
                add(select);
                select.setRenderer(new ComponentRenderer<>(spanProvider));
            });
            add(add);
            remove(event.getSource());
        });
        add(remove);
    }
}
