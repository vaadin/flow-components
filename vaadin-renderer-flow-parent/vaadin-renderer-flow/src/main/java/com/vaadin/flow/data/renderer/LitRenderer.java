
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
package com.vaadin.flow.data.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.data.provider.DataGenerator;
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

/**
 * LitRenderer is a {@link Renderer} that uses a Lit-based template literal to
 * render given model objects in the components that support the JS renderer
 * functions API. Mainly it's intended for use with {@code Grid},
 * {@code ComboBox} and {@code VirtualList}, but is not limited to these.
 *
 * @author Vaadin Ltd
 * @since 22.0.
 *
 * @param <SOURCE>
 *            the type of the model object used inside the template expression
 *
 * @see #of(String)
 * @see <a href=
 *      "https://lit.dev/docs/templates/overview/">https://lit.dev/docs/templates/overview/</a>
 * @see <a href=
 *      "https://cdn.vaadin.com/vaadin-web-components/20.0.0/#/elements/vaadin-combo-box"><code>&lt;vaadin-combo-box&gt;.renderer</code></a>
 */
@JsModule("./lit-renderer.ts")
public class LitRenderer<SOURCE> extends Renderer<SOURCE> {
    private final String templateExpression;

    private final String DEFAULT_RENDERER_NAME = "renderer";

    private final String propertyNamespace;

    private final Map<String, ValueProvider<SOURCE, ?>> valueProviders = new HashMap<>();
    private final Map<String, SerializableBiConsumer<SOURCE, JsonArray>> clientCallables = new HashMap<>();

    private final String ALPHANUMERIC_REGEX = "^[a-zA-Z0-9]+$";

    private LitRenderer(String templateExpression) {
        this.templateExpression = templateExpression;

        // Generate a unique (in scope of the UI) namespace for the renderer
        // properties.
        int litRendererCount = UI.getCurrent().getElement()
                .getProperty("__litRendererCount", 0);
        UI.getCurrent().getElement().setProperty("__litRendererCount",
                litRendererCount + 1);
        propertyNamespace = "lr_" + litRendererCount + "_";
    }

    /**
     * Creates a new LitRenderer based on the provided template expression. The
     * expression accepts content that is allowed inside JS template literals,
     * and works with the Lit data binding syntax.
     * <p>
     * The template expression has access to:
     * <ul>
     * <li>{@code item} the model item being rendered</li>
     * <li>{@code index} the index of the current item (when rendering a
     * list)</li>
     * <li>{@code item.property} any property of the model item exposed via
     * {@link #withProperty(String, ValueProvider)}</li>
     * <li>any function exposed via
     * {@link #withFunction(String, SerializableConsumer)}</li>
     * </ul>
     * <p>
     * Examples:
     *
     * <pre>
     * {@code
     * // Prints the `name` property of a person
     * LitRenderer.<Person> of("<div>Name: ${item.name}</div>")
     *          .withProperty("name", Person::getName);
     *
     * // Prints the index of the item inside a repeating list
     * LitRenderer.of("${index}");
     * }
     * </pre>
     *
     * @param <SOURCE>
     *            the type of the input object used inside the template
     *
     * @param templateExpression
     *            the template expression used to render items, not
     *            <code>null</code>
     * @return an initialized LitRenderer
     * @see LitRenderer#withProperty(String, ValueProvider)
     * @see LitRenderer#withFunction(String, SerializableConsumer)
     */
    public static <SOURCE> LitRenderer<SOURCE> of(String templateExpression) {
        Objects.requireNonNull(templateExpression);
        return new LitRenderer<>(templateExpression);
    }

    /**
     * @deprecated LitRenderer doesn't support <template> elements. Don't use.
     */
    @Deprecated
    @Override
    public Rendering<SOURCE> render(Element container,
            DataKeyMapper<SOURCE> keyMapper, Element contentTemplate) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated LitRenderer doesn't support getting the event handlers. Don't
     *             use.
     */
    @Deprecated
    @Override
    public Map<String, SerializableConsumer<SOURCE>> getEventHandlers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets up rendering of model objects inside a given {@param container}
     * element. The model objects are rendered using the Lit template literal
     * provided when creating this LitRenderer instance, and the Vaadin-default
     * JS renderer function name.
     *
     * @param container
     *            the DOM element that supports setting a renderer function
     * @param keyMapper
     *            mapper used internally to fetch items by key and to provide
     *            keys for given items. It is required when either functions or
     *            {@link DataGenerator} are supported
     * @return the context of the rendering, that can be used by the components
     *         to provide extra customization
     */
    @Override
    public Rendering<SOURCE> render(Element container,
            DataKeyMapper<SOURCE> keyMapper) {
        return this.render(container, keyMapper, DEFAULT_RENDERER_NAME);
    }

    /**
     * Sets up rendering of model objects inside a given {@param container}
     * element. The model objects are rendered using the Lit template literal
     * provided when creating this LitRenderer instance, and a given
     * {@param rendererName} JS renderer function.
     *
     * @param container
     *            the DOM element that supports setting a renderer function
     * @param keyMapper
     *            mapper used internally to fetch items by key and to provide
     *            keys for given items. It is required when either functions or
     *            {@link DataGenerator} are supported
     * @param rendererName
     *            name of the renderer function the container element accepts
     * @return the context of the rendering, that can be used by the components
     *         to provide extra customization
     */
    public Rendering<SOURCE> render(Element container,
            DataKeyMapper<SOURCE> keyMapper, String rendererName) {
        DataGenerator<SOURCE> dataGenerator = createDataGenerator();
        Registration registration = createJsRendererFunction(container,
                keyMapper, rendererName);

        return new Rendering<SOURCE>() {
            @Override
            public Optional<DataGenerator<SOURCE>> getDataGenerator() {
                return Optional.of(dataGenerator);
            }

            @Override
            public Element getTemplateElement() {
                return null;
            }

            @Override
            public Registration getRegistration() {
                return registration;
            }
        };
    }

    private void setElementRenderer(Element container, String rendererName,
            String templateExpression, ReturnChannelRegistration returnChannel,
            JsonArray clientCallablesArray, String propertyNamespace) {
        container.executeJs(
                "window.Vaadin.setLitRenderer(this, $0, $1, $2, $3, $4)",
                rendererName, templateExpression, returnChannel,
                clientCallablesArray, propertyNamespace);
    }

    private Registration createJsRendererFunction(Element container,
            DataKeyMapper<SOURCE> keyMapper, String rendererName) {
        ReturnChannelRegistration returnChannel = container.getNode()
                .getFeature(ReturnChannelMap.class)
                .registerChannel(arguments -> {
                    // Invoked when the client calls one of the client callables
                    String handlerName = arguments.getString(0);
                    String itemKey = arguments.getString(1);
                    JsonArray args = arguments.getArray(2);

                    SerializableBiConsumer<SOURCE, JsonArray> handler = clientCallables
                            .get(handlerName);
                    SOURCE item = keyMapper.get(itemKey);

                    handler.accept(item, args);
                });

        JsonArray clientCallablesArray = JsonUtils
                .listToJson(new ArrayList<>(clientCallables.keySet()));

        List<Registration> registrations = new ArrayList<>();

        // Since the renderer is set manually on the client-side, an attach
        // listener for the host component is required so that the renderer gets
        // applied even when the host component gets a new Web Component
        // instance (for example on detach + reattach).
        //
        // The attach listener needs to be released when the Renderer instance
        // is no longer used so the registration is cleared by the renderer
        // registration.
        registrations.add(container.addAttachListener(e -> {
            setElementRenderer(container, rendererName, templateExpression,
                    returnChannel, clientCallablesArray, propertyNamespace);
        }));
        // Call once initially
        setElementRenderer(container, rendererName, templateExpression,
                returnChannel, clientCallablesArray, propertyNamespace);

        // Get the renderer function cleared when the LitRenderer is
        // unregistered
        registrations.add(() -> container.executeJs(
                "window.Vaadin.unsetLitRenderer(this, $0, $1)", rendererName,
                propertyNamespace));

        return () -> registrations.forEach(Registration::remove);
    }

    private DataGenerator<SOURCE> createDataGenerator() {
        return (item, jsonObject) -> {
            valueProviders.forEach((key, provider) -> {
                jsonObject.put(
                        // Prefix the property name with a LitRenderer
                        // instance specific namespace to avoid property
                        // name clashes.
                        // Fixes https://github.com/vaadin/flow/issues/8629
                        // in LitRenderer
                        propertyNamespace + key,
                        JsonSerializer.toJson(provider.apply(item)));
            });
        };
    }

    /**
     * Makes a property available to the template expression. Each property is
     * referenced inside the template by using the {@code ${item.property}}
     * syntax.
     * <p>
     * Examples:
     *
     * <pre>
     * {@code
     * // Regular property
     * LitRenderer.<Person> of("<div>Name: ${item.name}</div>")
     *          .withProperty("name", Person::getName);
     *
     * // Property that uses a bean. Note that in this case the entire "Address" object will be sent to the template.
     * // Note that even properties of the bean which are not used in the template are sent to the client, so use
     * // this feature with caution.
     * LitRenderer.<Person> of("<span>Street: ${item.address.street}</span>")
     *          .withProperty("address", Person::getAddress);
     *
     * // In this case only the street field inside the Address object is sent
     * LitRenderer.<Person> of("<span>Street: ${item.street}</span>")
     *          .withProperty("street", person -> person.getAddress().getStreet());
     * }
     * </pre>
     *
     * Any types supported by the {@link JsonSerializer} are valid types for the
     * LitRenderer.
     *
     * @param property
     *            the name of the property used inside the template expression,
     *            not <code>null</code>
     *
     * @param provider
     *            a {@link ValueProvider} that provides the actual value for the
     *            property, not <code>null</code>
     * @return this instance for method chaining
     */
    public LitRenderer<SOURCE> withProperty(String property,
            ValueProvider<SOURCE, ?> provider) {
        Objects.requireNonNull(property);
        Objects.requireNonNull(provider);
        valueProviders.put(property, provider);
        return this;
    }

    /**
     * Adds an event handler that can be called from within the template
     * expression.
     * <p>
     * Examples:
     *
     * <pre>
     * {@code
     * // Standard event
     * LitRenderer.of("<button @click=${handleClick}>Click me</button>")
     *          .withEventHandler("handleClick", object -> doSomething());
     * }
     * </pre>
     *
     * The name of the event handler used in the template expression should be
     * the name used at the eventHandlerName parameter. This name must be a
     * valid JavaScript function name.
     *
     * @param eventHandlerName
     *            the name of the event handler used inside the template
     *            expression, must be alphanumeric and not <code>null</code>,
     *            must not be one of the JavaScript reserved words
     *            (https://www.w3schools.com/js/js_reserved.asp)
     * @param handler
     *            the handler executed when the event handler is called, not
     *            <code>null</code>
     * @return this instance for method chaining
     * @see <a href=
     *      "https://lit.dev/docs/templates/expressions/#event-listener-expressions">https://lit.dev/docs/templates/expressions/#event-listener-expressions</a>
     *
     * @deprecated Use {@link #withFunction(String, SerializableConsumer)}
     *             instead.
     */
    public LitRenderer<SOURCE> withEventHandler(String eventHandlerName,
            SerializableConsumer<SOURCE> handler) {
        return withFunction(eventHandlerName, handler);
    }

    /**
     * Adds a function that can be called from within the template expression.
     * <p>
     * Examples:
     *
     * <pre>
     * {@code
     * // Standard event
     * LitRenderer.of("<button @click=${handleClick}>Click me</button>")
     *          .withFunction("handleClick", object -> doSomething());
     * }
     * </pre>
     *
     * The name of the function used in the template expression should be the
     * name used at the functionName parameter. This name must be a valid
     * JavaScript function name.
     *
     * @param functionName
     *            the name of the function used inside the template expression,
     *            must be alphanumeric and not <code>null</code>, must not be
     *            one of the JavaScript reserved words
     *            (https://www.w3schools.com/js/js_reserved.asp)
     * @param handler
     *            the handler executed when the function is called, not
     *            <code>null</code>
     * @return this instance for method chaining
     * @see <a href=
     *      "https://lit.dev/docs/templates/expressions/#event-listener-expressions">https://lit.dev/docs/templates/expressions/#event-listener-expressions</a>
     */
    public LitRenderer<SOURCE> withFunction(String functionName,
            SerializableConsumer<SOURCE> handler) {
        return withFunction(functionName,
                (item, ignore) -> handler.accept(item));
    }

    /**
     * Adds a function that can be called from within the template expression.
     * The function accepts arguments that can be consumed by the given handler.
     *
     * <p>
     * Examples:
     *
     * <pre>
     * {@code
     * // Standard event
     * LitRenderer.of("<button @click=${handleClick}>Click me</button>")
     *          .withFunction("handleClick", item -> doSomething());
     *
     * // Function invocation with arguments
     * LitRenderer.of("<input @keypress=${(e) => handleKeyPress(e.key)}>")
     *          .withFunction("handleKeyPress", (item, args) -> {
     *              System.out.println("Pressed key: " + args.getString(0));
     *          });
     * }
     * </pre>
     *
     * The name of the function used in the template expression should be the
     * name used at the functionName parameter. This name must be a valid
     * Javascript function name.
     *
     * @param functionName
     *            the name of the function used inside the template expression,
     *            must be alphanumeric and not <code>null</code>, must not be
     *            one of the JavaScript reserved words
     *            (https://www.w3schools.com/js/js_reserved.asp)
     * @param handler
     *            the handler executed when the function is called, not
     *            <code>null</code>
     * @return this instance for method chaining
     * @see <a href=
     *      "https://lit.dev/docs/templates/expressions/#event-listener-expressions">https://lit.dev/docs/templates/expressions/#event-listener-expressions</a>
     */
    public LitRenderer<SOURCE> withFunction(String functionName,
            SerializableBiConsumer<SOURCE, JsonArray> handler) {
        Objects.requireNonNull(functionName);
        Objects.requireNonNull(handler);

        if (!Pattern.matches(ALPHANUMERIC_REGEX, functionName)) {
            throw new IllegalArgumentException(
                    "Function name must be alphanumeric");
        }
        clientCallables.put(functionName, handler);
        return this;
    }

    /**
     * Gets the property mapped to {@link ValueProvider}s in this renderer. The
     * returned map is immutable.
     *
     * @return the mapped properties, never <code>null</code>
     */
    @Override
    public Map<String, ValueProvider<SOURCE, ?>> getValueProviders() {
        return Collections.unmodifiableMap(valueProviders);
    }
}
