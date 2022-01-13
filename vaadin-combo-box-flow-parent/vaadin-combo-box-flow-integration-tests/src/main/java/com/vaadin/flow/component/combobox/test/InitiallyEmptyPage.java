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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/initially-empty")
public class InitiallyEmptyPage extends Div {

    public InitiallyEmptyPage() {
        NativeButton addInsideDetachedContainerButton = new NativeButton(
                "add inside a detached container", e -> {
                    Div container = new Div();
                    add(container);
                    // Detach the container element with JS
                    container.getElement().executeJs(
                            "this.__parent = this.parentElement; this.remove();")
                            .then((res) -> {
                                // Add a combo box to the detached container
                                ComboBox<String> comboBox = new ComboBox<>();
                                comboBox.setItems("foo", "bar");
                                container.add(comboBox);

                                // Re-attach the container element with JS
                                container.getElement().executeJs("")
                                        .then((res2) -> {
                                            container.getElement().executeJs(
                                                    "this.__parent.appendChild(this);");
                                        });

                            });
                });
        addInsideDetachedContainerButton
                .setId("add-inside-detached-container-button");

        // This page must not add a ComboBox on the page initially so that the
        // "<vaadin-cmobo-box>" Web Component doesn't get prematurely finalized.
        // This ensures the related tests remain valid.
        add(addInsideDetachedContainerButton);
    }
}
