package com.vaadin.flow.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.internal.nodefeature.ReturnChannelMap;
import com.vaadin.flow.internal.nodefeature.ReturnChannelRegistration;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonArray;

@JsModule("./lit-renderer.ts")
public class LitRenderer<T> implements Renderer<T> {
    private String templateExpression;

    private final String DEFAULT_RENDERER_NAME = "renderer";

    private final String propertyNamespace;

    private Map<String, ValueProvider<T, ?>> valueProviders = new HashMap<>();
    private Map<String, SerializableBiConsumer<T, JsonArray>> clientCallables = new HashMap<>();

    private LitRenderer(String templateExpression) {
        this.templateExpression = templateExpression;

        // Generate a unique (in scope of the UI) namespace for the renderer
        // properties.
        int litRendererCount = UI.getCurrent().getElement().getProperty("__litRendererCount", 0);
        UI.getCurrent().getElement().setProperty("__litRendererCount", litRendererCount + 1);
        propertyNamespace = "lr_" + litRendererCount + "_";
    }

    public static <T> LitRenderer<T> of(String templateExpression) {
        return new LitRenderer<>(templateExpression);
    }

    private void setElementRenderer(Element container, String rendererName, String templateExpression, ReturnChannelRegistration returnChannel,
    JsonArray clientCallablesArray, String propertyNamespace) {
        container.executeJs(
            "window.Vaadin.setLitRenderer(this, $0, $1, $2, $3, $4)",
            rendererName, templateExpression, returnChannel,
            clientCallablesArray, propertyNamespace);
    }

    public Registration prepare(Element container, DataKeyMapper<T> keyMapper, CompositeDataGenerator<T> hostDataDenerator) {
        return prepare(container, keyMapper, hostDataDenerator, DEFAULT_RENDERER_NAME);
    }

    public Registration prepare(Element container, DataKeyMapper<T> keyMapper, CompositeDataGenerator<T> hostDataDenerator,
            String rendererName) {
                ReturnChannelRegistration returnChannel = container.getNode()
                .getFeature(ReturnChannelMap.class)
                .registerChannel(arguments -> {
                    // Invoked when the client calls one of the client callables
                    String handlerName = arguments.getString(0);
                    String itemKey = arguments.getString(1);
                    JsonArray args = arguments.getArray(2);

                    SerializableBiConsumer<T, JsonArray> handler = clientCallables
                            .get(handlerName);
                    T item = keyMapper.get(itemKey);

                    handler.accept(item, args);
                });

        JsonArray clientCallablesArray = JsonUtils.listToJson(
                clientCallables.keySet().stream().collect(Collectors.toList()));

        List<Registration> registrations = new ArrayList<>();

        // Since the renderer is set manually on the client-side, an attach listener
        // for the host component is required so that the renderer gets applied even when the
        // host component is detached and reattached (crearing a new Web Component instance).
        // The listener needs to be released when the Renderer instance is no longer used so
        // the registration should get cleared by the renderer registration.
        registrations.add(container.addAttachListener(e -> {
            setElementRenderer(container, rendererName, templateExpression, returnChannel,
                clientCallablesArray, propertyNamespace);
        }));

        setElementRenderer(container, rendererName, templateExpression, returnChannel,
                clientCallablesArray, propertyNamespace);

        // Get the renderer function cleared when the LitRenderer is unregistered
        registrations.add(() -> container.executeJs("window.Vaadin.unsetLitRenderer(this, $0, $1)",
            rendererName, propertyNamespace));

        if (hostDataDenerator != null && !valueProviders.isEmpty()) {
            CompositeDataGenerator<T> composite = new CompositeDataGenerator<>();

            valueProviders.forEach((key, provider) -> composite
                .addDataGenerator((item, jsonObject) -> jsonObject.put(
                        // Prefix the property name with a LitRenderer instance specific
                        // namespace to avoid property name clashes.
                        // Fixes https://github.com/vaadin/flow/issues/8629 in LitRenderer
                        propertyNamespace + key,
                        JsonSerializer.toJson(provider.apply(item)))));

            registrations.add(hostDataDenerator.addDataGenerator(composite));
        }

        return () -> registrations.forEach(Registration::remove);
    }

    public LitRenderer<T> withProperty(String property,
            ValueProvider<T, ?> provider) {
        valueProviders.put(property, provider);
        return this;
    }

    public LitRenderer<T> withFunction(String functionName,
            SerializableConsumer<T> handler) {
        return withFunction(functionName,
                (item, ignore) -> handler.accept(item));
    }

    public LitRenderer<T> withFunction(String functionName,
            SerializableBiConsumer<T, JsonArray> handler) {
        // TODO validate functionName
        clientCallables.put(functionName, handler);
        return this;
    }
}
