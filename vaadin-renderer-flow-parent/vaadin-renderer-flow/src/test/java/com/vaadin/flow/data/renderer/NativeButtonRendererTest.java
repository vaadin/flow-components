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
package com.vaadin.flow.data.renderer;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.renderer.ClickableRenderer.ItemClickListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonObject;

public class NativeButtonRendererTest {

    @Test
    public void templateRenderered_containerIsDisabled_buttonIsDisabled() {
        NativeButtonRenderer<String> renderer = new NativeButtonRenderer<>(
                "Label");
        Element container = new Element("div");
        KeyMapper<String> keyMapper = new KeyMapper<>();
        Rendering<String> rendering = renderer.render(container, keyMapper);

        Assert.assertTrue("The DataGenerator should be present",
                rendering.getDataGenerator().isPresent());
        DataGenerator<String> dataGenerator = rendering.getDataGenerator()
                .get();

        JsonObject json = Json.createObject();
        dataGenerator.generateData("something", json);

        // Find the mapped key for "disabled" property
        var keyForDisabled = Arrays.stream(json.keys())
                .filter(key -> key.endsWith("disabled")).findFirst().get();
        Assert.assertFalse("The button shouldn't be disabled",
                json.getBoolean(keyForDisabled));

        mockDisabled(container);

        json = Json.createObject();
        dataGenerator.generateData("something", json);
        Assert.assertTrue("The button should be disabled",
                json.getBoolean(keyForDisabled));
    }

    @Test
    public void removeListenerInListener_shouldNotThrowException() {
        // Create a new renderer
        var renderer = new NativeButtonRenderer<String>("Label");

        // Create and add a listener that removes itself
        var registration = new AtomicReference<Registration>();
        ItemClickListener<String> listener = e -> registration.get().remove();
        registration.set(renderer.addItemClickListener(listener));

        // Invoke the listener
        invokeClickListeners(renderer);
    }

    private void invokeClickListeners(NativeButtonRenderer<String> renderer) {
        try {
            var clientCallablesField = LitRenderer.class
                    .getDeclaredField("clientCallables");
            clientCallablesField.setAccessible(true);
            var clientCallables = (Map<String, SerializableBiConsumer<String, JsonObject>>) clientCallablesField
                    .get(renderer);
            clientCallables.values()
                    .forEach(listener -> listener.accept("foo", null));
        } catch (NoSuchFieldException | IllegalArgumentException
                | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private void mockDisabled(Element container) {
        container.setEnabled(false);
        container.getChildren().forEach(child -> child.setEnabled(false));
    }

}
