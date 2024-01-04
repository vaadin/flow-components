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

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
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
@JsModule("./flow-component-renderer.js")
public class ComponentRenderer<COMPONENT extends Component, SOURCE>
        extends LitRenderer<SOURCE> {

    private Element container;
    private Element owner;
    private SerializableSupplier<COMPONENT> componentSupplier;
    private SerializableFunction<SOURCE, COMPONENT> componentFunction;
    private SerializableBiFunction<Component, SOURCE, Component> componentUpdateFunction;
    private SerializableBiConsumer<COMPONENT, SOURCE> itemConsumer;

    /**
     * Creates a new ComponentRenderer that uses the componentSupplier to
     * generate new {@link Component} instances, and the itemConsumer to set the
     * related items.
     * <p>
     * Some components may support several rendered components at once, so
     * different component instances should be created for each different item
     * for those components.
     *
     * @param componentSupplier
     *            a supplier that can generate new component instances
     * @param itemConsumer
     *            a setter for the corresponding item for the rendered component
     */
    public ComponentRenderer(SerializableSupplier<COMPONENT> componentSupplier,
            SerializableBiConsumer<COMPONENT, SOURCE> itemConsumer) {
        this.componentSupplier = componentSupplier;
        this.itemConsumer = itemConsumer;
    }

    /**
     * Creates a new ComponentRenderer that uses the componentSupplier to
     * generate new {@link Component} instances.
     * <p>
     * This constructor is a convenient way of providing components to a
     * template when the actual model item doesn't matter for the component
     * instance.
     * <p>
     * Some components may support several rendered components at once, so
     * different component instances should be created for each different item
     * for those components.
     *
     * @param componentSupplier
     *            a supplier that can generate new component instances
     */
    public ComponentRenderer(
            SerializableSupplier<COMPONENT> componentSupplier) {
        this(componentSupplier, null);
    }

    /**
     * Creates a new ComponentRenderer that uses the componentFunction to
     * generate new {@link Component} instances. The function takes a model item
     * and outputs a component instance.
     * <p>
     * Some components may support several rendered components at once, so
     * different component instances should be created for each different item
     * for those components.
     *
     * @param componentFunction
     *            a function that can generate new component instances
     * @see #ComponentRenderer(SerializableFunction, SerializableBiFunction)
     */
    public ComponentRenderer(
            SerializableFunction<SOURCE, COMPONENT> componentFunction) {
        this(componentFunction, null);
    }

    /**
     * Creates a new ComponentRenderer that uses the componentFunction to
     * generate new {@link Component} instances, and a componentUpdateFunction
     * to update existing {@link Component} instances.
     * <p>
     * The componentUpdateFunction can return a different component than the one
     * previously created. In those cases, the new instance is used, and the old
     * is discarded.
     * <p>
     * Some components may support several rendered components at once, so
     * different component instances should be created for each different item
     * for those components.
     *
     * @param componentFunction
     *            a function that can generate new component instances
     * @param componentUpdateFunction
     *            a function that can update the existing component instance for
     *            the item, or generate a new component based on the item
     *            update. When the function is <code>null</code>, the
     *            componentFunction is always used instead
     */
    public ComponentRenderer(
            SerializableFunction<SOURCE, COMPONENT> componentFunction,
            SerializableBiFunction<Component, SOURCE, Component> componentUpdateFunction) {
        this.componentFunction = componentFunction;
        this.componentUpdateFunction = componentUpdateFunction;
    }

    @Override
    protected String getTemplateExpression() {
        var appId = UI.getCurrent() != null
                ? UI.getCurrent().getInternals().getAppId()
                : "";

        return "${Vaadin.FlowComponentHost.getNode('" + appId
                + "', item.nodeid)}";
    }

    Element getOwner() {
        return owner;
    }

    @Override
    public Rendering<SOURCE> render(Element owner,
            DataKeyMapper<SOURCE> keyMapper, String rendererName) {
        this.owner = owner;
        this.container = new Element("div");
        this.container.addAttachListener(event -> {
            this.container.executeJs(
                    "Vaadin.FlowComponentHost.patchVirtualContainer(this)");
        });
        owner.appendVirtualChild(container);
        var rendering = super.render(owner, keyMapper, rendererName);

        return configureRendering(rendering, keyMapper);
    }

    /**
     * Configures the {@code Rendering} instance provided by {@link LitRenderer}
     * to make it create and update Components for items.
     *
     * @param rendering
     *            the rendering instance
     * @param keyMapper
     *            the key mapper
     * @return a rendering instance configured for the purposes of this renderer
     */
    Rendering<SOURCE> configureRendering(Rendering<SOURCE> rendering,
            DataKeyMapper<SOURCE> keyMapper) {
        return new Rendering<SOURCE>() {
            @Override
            public Optional<DataGenerator<SOURCE>> getDataGenerator() {
                var generator = new CompositeDataGenerator<SOURCE>();

                var componentDataGenerator = new ComponentDataGenerator<SOURCE>(
                        ComponentRenderer.this,
                        keyMapper == null ? null : keyMapper::key);
                componentDataGenerator.setContainer(container);
                componentDataGenerator.setNodeIdPropertyName(
                        getPropertyNamespace() + "nodeid");
                generator.addDataGenerator(componentDataGenerator);

                generator.addDataGenerator(rendering.getDataGenerator().get());
                return Optional.of(generator);
            }

            @Override
            public Registration getRegistration() {
                return rendering.getRegistration();
            }
        };
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
}
