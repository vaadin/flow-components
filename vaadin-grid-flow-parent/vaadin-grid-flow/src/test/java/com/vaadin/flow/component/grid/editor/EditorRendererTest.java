/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.component.grid.Person;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;

public class EditorRendererTest {

    private EditorRenderer<Person> renderer;
    private Editor<Person> editor;
    private Element container;
    private Element editorContainer;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        editor = Mockito.mock(Editor.class);
        renderer = Mockito.spy(new EditorRenderer<>(editor, "col"));
        container = new Element("div");
        editorContainer = new Element("div");
        Mockito.when(renderer.createEditorContainer())
                .thenReturn(editorContainer);
    }

    @Test
    public void setComponentFunction_editorIsOpen_componentIsRendered() {
        Span span = new Span();
        renderer.setComponentFunction(item -> span);
        Mockito.when(editor.isOpen()).thenReturn(true);
        Mockito.when(renderer.getComponentNodeId(span)).thenReturn(42);

        Person item = new Person("Special Person", 42);

        renderer.render(container, null);
        renderer.refreshData(item);
        ObjectNode object = JacksonUtils.createObjectNode();
        renderer.generateData(item, object);

        Assert.assertEquals(42, object.get("_col_editor").intValue());
        Mockito.verify(renderer, Mockito.times(1)).getComponentNodeId(span);

        Assert.assertEquals(1, editorContainer.getChildCount());
        Assert.assertEquals(span,
                editorContainer.getChild(0).getComponent().get());
    }

    @Test
    public void setComponentFunction_editorIsClosed_nothingIsRendered() {
        renderer.setComponentFunction(item -> new Span());
        Mockito.when(editor.isOpen()).thenReturn(false);

        Person item = new Person("Special Person", 42);

        renderer.render(container, null);
        renderer.refreshData(item);
        ObjectNode object = JacksonUtils.createObjectNode();
        renderer.generateData(item, object);

        Assert.assertFalse(object.has("_col_editor"));
        Assert.assertEquals(0, editorContainer.getChildCount());
    }

    @Test
    public void setComponentFunction_functionReturnsNull_emptyIsRendered() {
        renderer.setComponentFunction(item -> null);
        Mockito.when(editor.isOpen()).thenReturn(true);

        Person item = new Person("Special Person", 42);

        renderer.render(container, null);
        renderer.refreshData(item);
        ObjectNode object = JacksonUtils.createObjectNode();
        renderer.generateData(item, object);

        Assert.assertTrue(object.has("_col_editor"));
        Assert.assertEquals(1, editorContainer.getChildCount());
        Assert.assertNull(editorContainer.getChild(0).getProperty("innerHTML"));
    }
}
