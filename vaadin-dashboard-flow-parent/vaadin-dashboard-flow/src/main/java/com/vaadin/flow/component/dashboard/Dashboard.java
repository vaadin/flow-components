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
package com.vaadin.flow.component.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

/**
 * The Dashboard component.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-dashboard")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha7")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
// @NpmPackage(value = "@vaadin/dashboard", version = "24.5.0-alpha7")
@JsModule("@vaadin/dashboard/src/vaadin-dashboard.js")
public class Dashboard<T extends DashboardWidget> extends Component
        implements HasSize {

    private List<T> widgets = new ArrayList<>();

    /**
     * Default constructor.
     */
    public Dashboard() {

        getElement().addEventListener("dashboard-dragend", e -> {
            fireEvent(new DashboardDragEndEvent<>(this,
                    new ArrayList<>(widgets)));
        });
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addDragendListener(
            ComponentEventListener<DashboardDragEndEvent<T>> listener) {
        return addListener(DashboardDragEndEvent.class,
                (ComponentEventListener) Objects.requireNonNull(listener));
    }

    public void setWigets(T... widgetsToAdd) {
        widgets.clear();
        widgets.addAll(List.of(widgetsToAdd));

        widgetsUpdated();
    }

    private void widgetsUpdated() {
        getElement().removeAllChildren();

        for (T widget : widgets) {
            var widgetElement = widget.getElement();
            widgetElement.addEventListener("order-changed", e -> {
                widgets.set(
                        (int) e.getEventData().getNumber("event.detail.value"),
                        widget);
            }).addEventData("event.detail.value").debounce(Integer.MAX_VALUE);

            getElement().appendChild(widgetElement);
        }
    }

}
