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
package com.vaadin.flow.component.shared.internal;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.internal.JsonSerializer;

import elemental.json.JsonObject;
import elemental.json.JsonType;

/**
 * An internal controller for managing the i18n properties of a component. Not
 * intended to be used publicly.
 *
 * @param <C>
 *            The type of the component that uses this controller.
 * @param <I>
 *            The type of the i18n properties object.
 */
@JsModule("./i18n-controller.js")
public class I18nController<C extends Component, I> implements Serializable {
    private final C component;
    private final AtomicBoolean updatePending = new AtomicBoolean(false);

    private I i18n;

    /**
     * Creates a new i18n controller for the given component.
     * 
     * @param component
     *            the component instance
     */
    public I18nController(C component) {
        this.component = component;

        component.addAttachListener(event -> requestUpdate());
    }

    /**
     * Gets the i18n properties object.
     * 
     * @return the i18n properties object
     */
    public I getI18n() {
        return i18n;
    }

    /**
     * Sets the i18n properties object.
     * 
     * @param i18n
     *            the i18n properties object
     */
    public void setI18n(I i18n) {
        Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
        this.i18n = i18n;
        requestUpdate();
    }

    private void requestUpdate() {
        if (updatePending.compareAndSet(false, true)) {
            component.getElement().getNode()
                    .runWhenAttached(ui -> ui.beforeClientResponse(component,
                            context -> performUpdate()));
        }
    }

    private void performUpdate() {
        if (i18n != null) {
            JsonObject i18nJson = (JsonObject) JsonSerializer.toJson(i18n);
            deeplyRemoveNullValuesFromJsonObject(i18nJson);
            component.getElement().executeJs(
                    "window.Vaadin.Flow.i18nController.updateI18n($0, $1)",
                    component.getElement(), i18nJson);
        }
        updatePending.set(false);
    }

    private void deeplyRemoveNullValuesFromJsonObject(JsonObject jsonObject) {
        for (String key : jsonObject.keys()) {
            if (jsonObject.get(key).getType() == JsonType.OBJECT) {
                deeplyRemoveNullValuesFromJsonObject(jsonObject.get(key));
            } else if (jsonObject.get(key).getType() == JsonType.NULL) {
                jsonObject.remove(key);
            }
        }
    }
}
