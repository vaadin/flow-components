/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.ui.iron.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.data.HasDataProvider;
import com.vaadin.data.provider.ArrayUpdater;
import com.vaadin.data.provider.ArrayUpdater.Update;
import com.vaadin.data.provider.DataCommunicator;
import com.vaadin.data.provider.DataGenerator;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.util.JsonUtils;
import com.vaadin.function.ValueProvider;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tag;
import com.vaadin.ui.common.ClientDelegate;
import com.vaadin.ui.common.Focusable;
import com.vaadin.ui.common.HasSize;
import com.vaadin.ui.common.HasStyle;
import com.vaadin.ui.common.HtmlImport;
import com.vaadin.ui.common.JavaScript;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.vaadin.ui.renderers.TemplateRenderer;
import com.vaadin.util.JsonSerializer;

import elemental.json.JsonObject;
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
 * For list renderered in grid layout (setting {@link #setGridLayout(boolean)}
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
 */
@Tag("iron-list")
@HtmlImport("frontend://bower_components/iron-list/iron-list.html")
@JavaScript("frontend://ironListConnector.js")
public class IronList<T> extends Component implements HasDataProvider<T>,
        HasStyle, HasSize, Focusable<IronList<T>> {

    private final class UpdateQueue implements Update {
        private List<Runnable> queue = new ArrayList<>();

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
            queue.add(() -> getElement().callFunction(name, arguments));
        }
    }

    private final ArrayUpdater arrayUpdater = UpdateQueue::new;
    private final Element template;
    private TemplateRenderer<T> renderer;

    private final DataGenerator<T> listDataGenerator = new DataGenerator<T>() {
        @Override
        public void generateData(T item, JsonObject jsonObject) {
            renderer.getValueProviders()
                    .forEach((property, provider) -> jsonObject.put(property,
                            JsonSerializer.toJson(provider.apply(item))));
        }
    };

    private final DataCommunicator<T> dataCommunicator = new DataCommunicator<>(
            listDataGenerator, arrayUpdater,
            data -> getElement().callFunction("$connector.updateData", data),
            getElement().getNode());

    /**
     * Creates an empty list.
     */
    public IronList() {
        template = new Element("template");
        getElement().appendChild(template);
        setRenderer(item -> String.valueOf(item));

        getElement().getNode()
                .runWhenAttached(ui -> ui.beforeClientResponse(this,
                        () -> ui.getPage().executeJavaScript(
                                "window.ironListConnector.initLazy($0)",
                                getElement())));
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
    public DataProvider<T, ?> getDataProvider() {
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
     * Note: {@link ComponentRenderer}s are not supported yet.
     * 
     * @param renderer
     *            a renderer for the items in the list, not <code>null</code>
     */
    public void setRenderer(TemplateRenderer<T> renderer) {
        Objects.requireNonNull(renderer, "The renderer must not be null");
        if (renderer instanceof ComponentRenderer) {
            throw new UnsupportedOperationException(
                    "ComponentRenderers are not supported yet");
        }
        this.renderer = renderer;

        /**
         * The placeholder is used by the client connector to create temporary
         * elements that are populated on demand (when the user scrolls to that
         * item).
         */
        template.setProperty("innerHTML", String.format(
        //@formatter:off
            "<span>"
                + "<template is='dom-if' if='[[item.__placeholder]]'>&nbsp;</template>"
                + "<template is='dom-if' if='[[!item.__placeholder]]'>%s</template>"
            + "</span>",
        //@formatter:on
                renderer.getTemplate()));
        getDataCommunicator().reset();
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

    @ClientDelegate
    private void setRequestedRange(int start, int length) {
        getDataCommunicator().setRequestedRange(start, length);
    }
}
