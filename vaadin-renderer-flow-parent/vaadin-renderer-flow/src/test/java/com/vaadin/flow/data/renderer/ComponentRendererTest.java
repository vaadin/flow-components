/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

class ComponentRendererTest {

    private static class TestUIInternals extends UIInternals {

        private List<PendingJavaScriptInvocation> invocations = new ArrayList<>();

        public TestUIInternals(UI ui) {
            super(ui);
        }

        @Override
        public void addJavaScriptInvocation(
                PendingJavaScriptInvocation invocation) {
            invocations.add(invocation);
        }

    }

    private static class TestUI extends UI {

        private UIInternals internals;

        @Override
        public UIInternals getInternals() {
            if (internals == null) {
                internals = new TestUIInternals(this);
            }
            return internals;
        }
    }

    @Test
    void componentRenderer_parentAttachedBeforeChild() {
        UI ui = new TestUI();
        TestUIInternals internals = (TestUIInternals) ui.getInternals();

        ComponentRenderer<TestDiv, String> renderer = new ComponentRenderer<>(
                e -> (new TestDiv()));

        Element containerParent = new Element("div");
        Element container = new Element("div");

        KeyMapper<String> keyMapper = new KeyMapper<>();

        Rendering<String> rendering = renderer.render(container, keyMapper);
        // simulate a call from the grid to refresh data
        containerParent.getNode()
                .runWhenAttached(ui2 -> ui2.getInternals().getStateTree()
                        .beforeClientResponse(containerParent.getNode(),
                                context -> {
                                    ObjectNode value = JacksonUtils
                                            .createObjectNode();
                                    rendering.getDataGenerator().get()
                                            .generateData("item", value);
                                    Assertions.assertEquals(1,
                                            JacksonUtils.getKeys(value).size(),
                                            "generateData should add one element in the jsonobject");
                                }));

        // attach the parent (ex: grid) before the child (ex: column)
        attachElement(ui, containerParent);
        attachElement(ui, container);

        internals.getStateTree().runExecutionsBeforeClientResponse();

    }

    @Test
    void componentRenderer_childAttachedBeforeParent() {
        UI ui = new TestUI();
        TestUIInternals internals = (TestUIInternals) ui.getInternals();

        ComponentRenderer<TestDiv, String> renderer = new ComponentRenderer<>(
                e -> (new TestDiv()));

        Element containerParent = new Element("div");
        Element container = new Element("div");
        KeyMapper<String> keyMapper = new KeyMapper<>();

        Rendering<String> rendering = (Rendering<String>) renderer
                .render(container, keyMapper);

        containerParent.getNode()
                .runWhenAttached(ui2 -> ui2.getInternals().getStateTree()
                        .beforeClientResponse(containerParent.getNode(),
                                context -> {
                                    ObjectNode value = JacksonUtils
                                            .createObjectNode();
                                    rendering.getDataGenerator().get()
                                            .generateData("item", value);
                                    Assertions.assertEquals(1,
                                            JacksonUtils.getKeys(value).size(),
                                            "generateData should add one element in the jsonobject");
                                }));
        // attach the child (ex: container) before the parent (ex: grid)
        attachElement(ui, container);
        attachElement(ui, containerParent);

        internals.getStateTree().runExecutionsBeforeClientResponse();

    }

    @Test
    void nullValues() {
        ComponentRenderer<Component, String> renderer = new ComponentRenderer<>(
                e -> {
                    return null;
                });

        ValueProvider<String, String> keyMapper = s -> "foo";
        ComponentDataGenerator cdg = new ComponentDataGenerator<>(renderer,
                keyMapper);

        Component c = cdg.createComponent("foo");
        Assertions.assertNotNull(c,
                "Placeholder component should be generated for null values");
        Assertions.assertEquals(0, c.getElement().getChildCount());

        c = cdg.updateComponent(c, "foo");
        Assertions.assertNotNull(c,
                "Placeholder component should be generated for null values");
        Assertions.assertEquals(0, c.getElement().getChildCount());
    }

    private void attachElement(UI ui, Element contentTemplate) {
        ui.getElement().appendChild(contentTemplate);
    }

    @Test
    void componentFunction_invokedOnCreate() {
        AtomicInteger createInvocations = new AtomicInteger();
        ComponentRenderer<TestDiv, String> renderer = new ComponentRenderer<>(
                item -> {
                    createInvocations.incrementAndGet();
                    Assertions.assertEquals("New item", item);
                    return new TestDiv();
                });

        renderer.createComponent("New item");

        Assertions.assertEquals(1, createInvocations.get(),
                "The component creation function should have been invoked once");
    }

    @Test
    void componentFunction_noUpdateFunction_invokedOnUpdate() {
        AtomicInteger createInvocations = new AtomicInteger();
        TestDiv div = new TestDiv();
        ComponentRenderer<TestDiv, String> renderer = new ComponentRenderer<>(
                item -> {
                    createInvocations.incrementAndGet();
                    Assertions.assertEquals("New item", item);
                    return div;
                });

        Component updatedComponent = renderer.updateComponent(div, "New item");

        Assertions.assertEquals(1, createInvocations.get(),
                "The component creation function should have been invoked once");

        Assertions.assertEquals(div, updatedComponent,
                "The two components should be the same");
    }

    @Test
    void updateFunction_invokedOnUpdate() {
        AtomicInteger createInvocations = new AtomicInteger();
        AtomicInteger updateInvocations = new AtomicInteger();
        ComponentRenderer<TestDiv, String> renderer = new ComponentRenderer<>(
                item -> {
                    createInvocations.incrementAndGet();
                    Assertions.assertEquals("New item", item);
                    return new TestDiv();
                }, (component, item) -> {
                    updateInvocations.incrementAndGet();
                    Assertions.assertEquals("Updated item", item);
                    return component;
                });

        TestDiv div = renderer.createComponent("New item");
        Component updatedComponent = renderer.updateComponent(div,
                "Updated item");

        Assertions.assertEquals(1, createInvocations.get(),
                "The component creation function should have been invoked once");
        Assertions.assertEquals(1, updateInvocations.get(),
                "The component update function should have been invoked once");

        Assertions.assertEquals(div, updatedComponent,
                "The two components should be the same");
    }

}
