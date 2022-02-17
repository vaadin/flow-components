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
package com.vaadin.flow.component.ironlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.ironlist.paging.PagelessDataCommunicator;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonValue;

/**
 * Component that encapsulates the functionality of the {@code <iron-list>}
 * webcomponent.
 * <p>
 * It supports {@link DataProvider}s to load data asynchronously and
 * {@link TemplateRenderer}s to render the markup for each item.
 * <p>
 * For this component to work properly, it needs to have a well defined
 * {@code height}. It can be an absolute height, like {@code 100px}, or a
 * relative height inside a container with well defined height.
 * <p>
 * For list rendered in grid layout (setting {@link #setGridLayout(boolean)}
 * with <code>true</code>), the {@code width} of the component also needs to be
 * well defined.
 *
 *
 * @author Vaadin Ltd.
 *
 * @param <T>
 *            the type of the items supported by the list
 * @see <a href=
 *      "https://www.webcomponents.org/element/PolymerElements/iron-list">iron-list
 *      webcomponent documentation</a>
 *
 * @deprecated since Vaadin 21, {@code IronList} is deprecated in favor of
 *             {@code VirtualList}
 */
@Tag("iron-list")
@NpmPackage(value = "@polymer/iron-list", version = "3.1.0")
@JsModule("@polymer/iron-list/iron-list.js")
@JsModule("./flow-component-renderer.js")
@JsModule("./ironListConnector.js")
@JsModule("./ironListStyles.js")
@Deprecated
public class IronList<T> extends Component implements HasDataProvider<T>,
        HasStyle, HasSize, Focusable<IronList<T>> {

    private final class UpdateQueue implements Update {
        private transient List<Runnable> queue = new ArrayList<>();

        private UpdateQueue(int size) {
            enqueue("$connector.updateSize", size);
        }

        @Override
        public void set(int start, List<JsonValue> items) {
            enqueue("$connector.set", start,
                    items.stream().collect(JsonUtils.asArray()));
        }

        @Override
        public void clear(int start, int length) {
            enqueue("$connector.clear", start, length);
        }

        @Override
        public void commit(int updateId) {
            getDataCommunicator().confirmUpdate(updateId);
            queue.forEach(Runnable::run);
            queue.clear();
        }

        private void enqueue(String name, Serializable... arguments) {
            queue.add(() -> getElement().callJsFunction(name, arguments));
        }
    }

    private final ArrayUpdater arrayUpdater = new ArrayUpdater() {
        @Override
        public Update startUpdate(int sizeChange) {
            return new UpdateQueue(sizeChange);
        }

        @Override
        public void initialize() {
            initConnector();
        }
    };

    private final Element template;
    private Renderer<T> renderer;
    private String originalTemplate;
    private boolean rendererChanged;
    private boolean templateUpdateRegistered;

    private final CompositeDataGenerator<T> dataGenerator = new CompositeDataGenerator<>();
    private Registration dataGeneratorRegistration;
    private transient T placeholderItem;

    private final DataCommunicator<T> dataCommunicator = new PagelessDataCommunicator<>(
            dataGenerator, arrayUpdater,
            data -> getElement().callJsFunction("$connector.updateData", data),
            getElement().getNode());

    /**
     * Creates an empty list.
     */
    public IronList() {
        dataGenerator.addDataGenerator(
                (item, jsonObject) -> renderer.getValueProviders()
                        .forEach((property, provider) -> jsonObject.put(
                                property,
                                JsonSerializer.toJson(provider.apply(item)))));

        template = new Element("template");
        getElement().appendChild(template);
        setRenderer(String::valueOf);
    }

    private void initConnector() {
        getUI().orElseThrow(() -> new IllegalStateException(
                "Connector can only be initialized for an attached IronList"))
                .getPage()
                .executeJs("window.Vaadin.Flow.ironListConnector.initLazy($0)",
                        getElement());
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        Objects.requireNonNull(dataProvider, "The dataProvider cannot be null");
        getDataCommunicator().setDataProvider(dataProvider, null);
    }

    /**
     * Returns the data provider of this list.
     *
     * @return the data provider of this list, not {@code null}
     */
    public DataProvider<T, ?> getDataProvider() { // NOSONAR
        return getDataCommunicator().getDataProvider();
    }

    /**
     * Returns the data communicator of this list.
     *
     * @return the data communicator, not {@code null}
     */
    public DataCommunicator<T> getDataCommunicator() {
        return dataCommunicator;
    }

    /**
     * Sets a renderer for the items in the list, by using a
     * {@link ValueProvider}. The String returned by the provider is used to
     * render each item.
     *
     * @param valueProvider
     *            a provider for the label string for each item in the list, not
     *            <code>null</code>
     */
    public void setRenderer(ValueProvider<T, String> valueProvider) {
        Objects.requireNonNull(valueProvider,
                "The valueProvider must not be null");
        this.setRenderer(TemplateRenderer.<T> of("[[item.label]]")
                .withProperty("label", valueProvider));
    }

    /**
     * Sets a renderer for the items in the list, by using a
     * {@link TemplateRenderer}. The template returned by the renderer is used
     * to render each item.
     * <p>
     * When set, a same renderer is used for the placeholder item. See
     * {@link #setPlaceholderItem(Object)} for details.
     *
     * @param renderer
     *            a renderer for the items in the list, not <code>null</code>
     */
    public void setRenderer(Renderer<T> renderer) {
        Objects.requireNonNull(renderer, "The renderer must not be null");

        if (dataGeneratorRegistration != null) {
            dataGeneratorRegistration.remove();
            dataGeneratorRegistration = null;
        }

        Rendering<T> rendering = renderer.render(getElement(),
                dataCommunicator.getKeyMapper(), template);
        if (rendering.getDataGenerator().isPresent()) {
            dataGeneratorRegistration = dataGenerator
                    .addDataGenerator(rendering.getDataGenerator().get());
        }

        this.renderer = renderer;

        rendererChanged = true;
        registerTemplateUpdate();

        getDataCommunicator().reset();
    }

    /**
     * Sets an item to be shown as placeholder in the list while the real data
     * in being fetched from the server.
     * <p>
     * Setting a placeholder item improves the user experience of the list while
     * scrolling, since the placeholder uses the same renderer set with
     * {@link #setRenderer(TemplateRenderer)}, maintaining the same height for
     * placeholders and actual items.
     * <p>
     * When no placeholder item is set (or when set to <code>null</code>), an
     * empty placeholder element is created with <code>100px</code> of width and
     * <code>18px</code> of height.
     * <p>
     * Note: when using {@link ComponentTemplateRenderer}s, the component used
     * for the placeholder is statically stamped in the list. It can not be
     * modified, nor receives any events.
     *
     * @param placeholderItem
     *            the item used as placeholder in the list, while the real data
     *            is being fetched from the server
     */
    public void setPlaceholderItem(T placeholderItem) {
        this.placeholderItem = placeholderItem;
        getElement().callJsFunction("$connector.setPlaceholderItem",
                JsonSerializer.toJson(placeholderItem));

        registerTemplateUpdate();
    }

    /**
     * Gets the placeholder item of this list, or <code>null</code> if none has
     * been set.
     *
     * @return the placeholder item
     */
    public T getPlaceholderItem() {
        return placeholderItem;
    }

    private void registerTemplateUpdate() {
        if (templateUpdateRegistered) {
            return;
        }
        templateUpdateRegistered = true;

        /*
         * The actual registration is done inside another beforeClientResponse
         * registration to make sure it runs last, after ComponentRenderer and
         * BasicRenderes have executed their rendering operations, which also
         * happen beforeClientResponse and might be registered after this.
         */
        runBeforeClientResponse(
                () -> runBeforeClientResponse(() -> updateTemplateInnerHtml()));
    }

    private void runBeforeClientResponse(Command command) {
        getElement().getNode()
                .runWhenAttached(ui -> ui.getInternals().getStateTree()
                        .beforeClientResponse(getElement().getNode(),
                                context -> command.execute()));
    }

    private void updateTemplateInnerHtml() {
        templateUpdateRegistered = false;
        if (rendererChanged) {
            originalTemplate = template.getProperty("innerHTML");
            rendererChanged = false;
        }

        String placeholderTemplate;
        if (placeholderItem == null) {
            /*
             * When a placeholderItem is not set, there should be still a
             * placeholder element with a non 0 size to avoid issues when
             * scrolling.
             */
            placeholderTemplate = "<div style='width:100px;height:18px'></div>";
        } else if (renderer instanceof ComponentRenderer) {
            @SuppressWarnings("unchecked")
            ComponentRenderer<?, T> componentRenderer = (ComponentRenderer<?, T>) renderer;
            Component component = componentRenderer
                    .createComponent(placeholderItem);
            component.getElement().setEnabled(isEnabled());
            placeholderTemplate = component.getElement().getOuterHTML();
        } else {
            placeholderTemplate = originalTemplate;
        }

        /**
         * The placeholder is used by the client connector to create temporary
         * elements that are populated on demand (when the user scrolls to that
         * item).
         */
        template.setProperty("innerHTML", String.format(
        //@formatter:off
            "<span>"
                + "<template is='dom-if' if='[[item.__placeholder]]'>%s</template>"
                + "<template is='dom-if' if='[[!item.__placeholder]]'>%s</template>"
            + "</span>",
        //@formatter:on
                placeholderTemplate, originalTemplate));
    }

    /**
     * Gets whether this list is rendered in a grid layout instead of a linear
     * list.
     *
     * @return <code>true</code> if the list renders itself as a grid,
     *         <code>false</code> otherwise
     */
    public boolean isGridLayout() {
        return getElement().getProperty("grid", false);
    }

    /**
     * Sets this list to be rendered as a grid. Note that for the grid layout to
     * work properly, the component needs to have a well defined {@code width}
     * and {@code height}.
     *
     * @param gridLayout
     *            <code>true</code> to make the list renders itself as a grid,
     *            <code>false</code> to make it render as a linear list
     */
    public void setGridLayout(boolean gridLayout) {
        getElement().setProperty("grid", gridLayout);
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        super.onEnabledStateChanged(enabled);

        /*
         * Rendered component's enabled state needs to be updated via rendering
         */
        setRenderer(renderer);
    }

    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void setRequestedRange(int start, int length) {
        getDataCommunicator().setRequestedRange(start, length);
    }
}
