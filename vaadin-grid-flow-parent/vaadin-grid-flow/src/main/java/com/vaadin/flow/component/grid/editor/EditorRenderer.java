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
package com.vaadin.flow.component.grid.editor;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.ExecutionContext;

import elemental.json.JsonObject;

/**
 * Renderer and DataGenerator used by {@link Column} to control the state of the
 * editor components.
 * <p>
 * Components are created during the {@link #generateData(Object, JsonObject)}
 * calls, and the proper data is sent to the client-side to be rendered.
 *
 * @author Vaadin Ltd.
 *
 * @param <T>
 *            the type of the object being processed
 *
 * @see Column#setEditorComponent(Component)
 * @see Column#setEditorComponent(SerializableFunction)
 */
public class EditorRenderer<T> extends Renderer<T> implements DataGenerator<T> {

    private final Editor<T> editor;
    private final String columnInternalId;
    private Element editorContainer;

    private SerializableFunction<T, ? extends Component> componentFunction;

    private Component component;

    // the flow-component-renderer needs something to load when the component is
    // null
    private Component emptyComponent;

    /**
     * Creates a new renderer for a specific column.
     *
     * @param editor
     *            the Grid's editor
     * @param columnInternalId
     *            the internal Id of the column that uses this data generator
     */
    public EditorRenderer(Editor<T> editor, String columnInternalId) {
        this.editor = editor;
        this.columnInternalId = columnInternalId;
    }

    /**
     * Sets the function that creates components to be used as editors for the
     * column. When set to <code>null</code>, an empty component is used
     * instead.
     *
     * @param componentFunction
     *            the function that generates editor components
     */
    public void setComponentFunction(
            SerializableFunction<T, ? extends Component> componentFunction) {
        this.componentFunction = componentFunction;
    }

    @Override
    public void generateData(T item, JsonObject jsonObject) {
        if (editor.isOpen() && component != null) {
            int nodeId = getComponentNodeId(component);
            jsonObject.put("_" + columnInternalId + "_editor", nodeId);
        }
    }

    private void buildComponent(T item) {
        if (componentFunction != null) {
            setComponent(componentFunction.apply(item));
        } else {
            setComponent(null);
        }
    }

    private void setComponent(Component newComponent) {
        if (component != null) {
            if (component.equals(newComponent)) {
                return;
            }
            if (component.getElement().getParent().equals(editorContainer)) {
                editorContainer.removeChild(component.getElement());
            }
        }

        if (newComponent == null) {
            newComponent = getOrCreateEmptyComponent();
        }

        // the component needs to be attached in order to have a nodeId
        editorContainer.appendChild(newComponent.getElement());
        component = newComponent;
    }

    private Component getOrCreateEmptyComponent() {
        if (emptyComponent == null) {
            emptyComponent = new Span();
        }
        return emptyComponent;
    }

    @Override
    public void refreshData(T item) {
        if (editor.isOpen()) {
            buildComponent(item);
        }
    }

    @Override
    public Rendering<T> render(Element container, DataKeyMapper<T> keyMapper,
            Element contentTemplate) {

        /*
         * The virtual container is needed as the parent of all editor
         * components. Editor components need a parent in order to have a proper
         * nodeId, and the nodeId is needed by the <flow-component-renderer> in
         * the client-side.
         */
        editorContainer = createEditorContainer();
        container.appendVirtualChild(editorContainer);

        // Run editor renderer setup
        runBeforeClientResponse(container,
                context -> setupEditorRenderer(container, context));

        // Also run editor renderer setup whenever the component gets attached
        container.addAttachListener(event -> {
            runBeforeClientResponse(container,
                    context -> setupEditorRenderer(container, context));
        });

        return new EditorRendering(contentTemplate);
    }

    private void setupEditorRenderer(Element container,
            ExecutionContext context) {
        String appId = context.getUI().getInternals().getAppId();
        String editorTemplate = String.format(
                "<flow-component-renderer appid='%s' nodeid='${model.item._%s_editor}'></flow-component-renderer>",
                appId, columnInternalId);

        //@formatter:off
        container.executeJs("const originalRender = this.renderer;" +
            // Patch the container's renderer function to handle the editor
            "this.renderer = (root, container, model) => {" +
                "const editingChanged = root.__editing !== model.item._editing;" +
                "root.__editing = model.item._editing;" +

                // If the editing state changed, the root needs to be cleared
                "if (editingChanged) {" +
                    "delete root._$litPart$; root.innerHTML = ''" +
                "}" +

                // If editing, render the editor, otherwise use the original renderer
                "if (root.__editing) { root.innerHTML = `" + editorTemplate + "` }" +
                "else if (!originalRender) { root.textContent = model.item." + columnInternalId + " }" +
                "else { originalRender(root, container, model); }" +
            "};");
        //@formatter:on

        // clear the path property, since we are using an explicit renderer
        container.removeProperty("path");
    }

    private void runBeforeClientResponse(Element container,
            SerializableConsumer<ExecutionContext> execution) {
        container.getNode()
                .runWhenAttached(ui -> ui.getInternals().getStateTree()
                        .beforeClientResponse(container.getNode(), execution));
    }

    // Package-protected for testing

    int getComponentNodeId(Component component) {
        return component.getElement().getNode().getId();
    }

    Element createEditorContainer() {
        return ElementFactory.createDiv();
    }

    private class EditorRendering implements Rendering<T> {

        private final Element contentTemplate;

        public EditorRendering(Element contentTemplate) {
            this.contentTemplate = contentTemplate;
        }

        @Override
        public Optional<DataGenerator<T>> getDataGenerator() {
            return Optional.of(EditorRenderer.this);
        }

        @Override
        public Element getTemplateElement() {
            return contentTemplate;
        }
    }
}
