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
import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.shared.Registration;

/**
 * Base class for all renderers that support arbitrary {@link Component}s.
 * <p>
 * Components that support renderers should use the appropriate method from this
 * class to provide component rendering: {@link #render(Element, DataKeyMapper)}
 * for components that uses {@code <template>}, and
 * {@link #createComponent(Object)} for components that use light-dom.
 *
 * @author Vaadin Ltd
 *
 * @param <COMPONENT>
 *            the type of the output component
 * @param <SOURCE>
 *            the type of the input model object
 */
public class ComponentRenderer<COMPONENT extends Component, SOURCE>
        extends LitRenderer<SOURCE> {

    private Element container;
    private SerializableSupplier<COMPONENT> componentSupplier;
    private SerializableFunction<SOURCE, COMPONENT> componentFunction;
    private SerializableBiFunction<Component, SOURCE, Component> componentUpdateFunction;
    private SerializableBiConsumer<COMPONENT, SOURCE> itemConsumer;

    public ComponentRenderer(
            SerializableFunction<SOURCE, COMPONENT> componentFunction,
            SerializableBiFunction<Component, SOURCE, Component> componentUpdateFunction) {
        super("<flow-component-renderer nodeid=${item.nodeid} appid='"
                + UI.getCurrent().getInternals().getAppId()
                + "'></flow-component-renderer>");

        this.componentFunction = componentFunction;
        this.componentUpdateFunction = componentUpdateFunction;

        withProperty("nodeid", item -> {
            var component = createComponent(item);
            // TODO: Clean up (Use ComponentDataGenerator)
            container.appendVirtualChild(component.getElement());
            return component.getElement().getNode().getId();
        });
    }

    public ComponentRenderer(
            SerializableFunction<SOURCE, COMPONENT> componentFunction) {
        this(componentFunction, null);
    }

    public ComponentRenderer(SerializableSupplier<COMPONENT> componentSupplier,
            SerializableBiConsumer<COMPONENT, SOURCE> itemConsumer) {
        super("<flow-component-renderer nodeid=${item.nodeid} appid='"
                + UI.getCurrent().getInternals().getAppId()
                + "'></flow-component-renderer>");

        this.componentSupplier = componentSupplier;
        this.itemConsumer = itemConsumer;

        withProperty("nodeid", item -> {
            var component = createComponent(item);
            // TODO: Clean up (Use ComponentDataGenerator)
            container.appendVirtualChild(component.getElement());
            return component.getElement().getNode().getId();
        });
    }

    public ComponentRenderer(
            SerializableSupplier<COMPONENT> componentSupplier) {
        this(componentSupplier, null);
    }

    protected ComponentRenderer() {
        super("");
    }

    protected Element getContainer() {
        return container;
    }

    @Override
    public Rendering<SOURCE> render(Element container,
            DataKeyMapper<SOURCE> keyMapper, String rendererName) {
        this.container = container;
        var rendering = super.render(container, keyMapper, rendererName);

        return new Rendering<SOURCE>() {
            @Override
            public Optional<DataGenerator<SOURCE>> getDataGenerator() {
                var generator = new CompositeDataGenerator<SOURCE>();
                var componentDataGenerator = new ComponentDataGenerator<SOURCE>(
                        ComponentRenderer.this,
                        keyMapper == null ? null : keyMapper::key);
                generator.addDataGenerator(componentDataGenerator);
                generator.addDataGenerator(rendering.getDataGenerator().get());
                return Optional.of(generator);
            }

            @Override
            public Element getTemplateElement() {
                return null;
            }

            @Override
            public Registration getRegistration() {
                List<Registration> registrations = new ArrayList<>();
                registrations.add(rendering.getRegistration());
                // registrations.add(detachListenerRegistration);
                // registrations.add(() -> clearComponents());
                return () -> registrations.forEach(Registration::remove);
            }
        };

    }

    /**
     * Called when the item is updated. By default, a new {@link Component} is
     * created (via {@link #createComponent(Object)}) when the item is updated,
     * but setting a update function via the
     * {@link #ComponentRenderer(SerializableFunction, SerializableBiFunction)}
     * can change the behavior.
     *
     * @param currentComponent
     *            the current component used to render the item, not
     *            <code>null</code>
     * @param item
     *            the updated item
     * @return the component that should be used to render the updated item. The
     *         same instance can be returned, or a totally new one, but not
     *         <code>null</code>.
     */
    public Component updateComponent(Component currentComponent, SOURCE item) {
        if (componentUpdateFunction != null) {
            return componentUpdateFunction.apply(currentComponent, item);
        }
        return createComponent(item);
    }

    /**
     * Creates a component for a given object model item. Subclasses can
     * override this method to provide specific behavior.
     *
     * @param item
     *            the model item, possibly <code>null</code>
     * @return a component instance representing the provided item
     */
    public COMPONENT createComponent(SOURCE item) {
        if (componentFunction != null) {
            return componentFunction.apply(item);
        }
        COMPONENT component = componentSupplier.get();
        if (itemConsumer != null) {
            itemConsumer.accept(component, item);
        }
        return component;
    }

}
