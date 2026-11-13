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

import java.io.Serializable;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/initially-empty")
public class InitiallyEmptyPage extends Div {

    @JsDefinition
    interface ContainerJs extends Serializable {

        @JsExpression("this.__parent = this.parentElement; this.remove();")
        PendingJavaScriptResult detach();

        @JsExpression("return null")
        PendingJavaScriptResult roundTrip();

        @JsExpression("this.__parent.appendChild(this);")
        void reattach();
    }

    public InitiallyEmptyPage() {
        NativeButton addInsideDetachedContainerButton = new NativeButton(
                "add inside a detached container", e -> {
                    Div container = new Div();
                    add(container);
                    // Detach the container element with JS
                    container.getElement().executeJs(ContainerJs.class).detach()
                            .then((res) -> {
                                // Add a combo box to the detached container
                                ComboBox<String> comboBox = new ComboBox<>();
                                comboBox.setItems("foo", "bar");
                                container.add(comboBox);

                                // Re-attach the container element with JS
                                container.getElement()
                                        .executeJs(ContainerJs.class)
                                        .roundTrip().then((res2) -> {
                                            container.getElement()
                                                    .executeJs(
                                                            ContainerJs.class)
                                                    .reattach();
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
