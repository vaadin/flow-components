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
package com.vaadin.flow.component.dialog;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.tests.MockUIExtension;

class DialogChildrenTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Dialog dialog;

    @BeforeEach
    void setup() {
        dialog = new Dialog();
        ui.add(dialog);
    }

    @Test
    void add_slottedAsContent() {
        var child = new Div();
        dialog.add(child);
        assertContent(child);
    }

    @Test
    void addMany_slottedInOrder() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(child);
        dialog.add(child2);
        assertContent(child, child2);
    }

    @Test
    void addCollection_slottedInOrder() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(List.of(child, child2));
        assertContent(child, child2);
    }

    @Test
    void addComponentAsFirst_slottedInOrder() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(child);
        dialog.addComponentAsFirst(child2);
        assertContent(child2, child);
    }

    @Test
    void remove_removedFromContent() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(child, child2);
        dialog.remove(child2);
        assertContent(child);
    }

    @Test
    void removeAll_contentEmpty() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(child, child2);
        dialog.removeAll();
        assertContent();
    }

    @Test
    void addComponentAtIndex_slottedInOrder() {
        var child = new Div();
        var child2 = new Div();
        var child3 = new Div();
        dialog.add(child, child2);
        dialog.addComponentAtIndex(1, child3);
        assertContent(child, child3, child2);
    }

    @Test
    void addTextNodes_slottedInOrder() {
        var child = new Text("text");
        var child2 = new Text("text2");
        dialog.add(child, child2);
        assertContent(child, child2);
    }

    @Test
    void addBeforeAttaching_contentSlotted() {
        dialog = new Dialog();
        var child = new Div();
        dialog.add(child);
        ui.add(dialog);
        assertContent(child);
    }

    @Test
    void selfRemoveChild_removedFromContent() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(child, child2);
        child.removeFromParent();
        assertContent(child2);
    }

    @Test
    void addSeparately_selfRemoveChild_removedFromContent() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(child);
        dialog.add(child2);
        child.removeFromParent();
        assertContent(child2);
    }

    @Test
    void headerAndFooterAddedAsSlottedChildren() {
        dialog.getHeader().add(new Div());
        dialog.getFooter().add(new Div());

        Assertions.assertEquals(2, dialog.getElement().getChildCount());
        Assertions.assertEquals(dialog.getElement(),
                dialog.getHeader().root.getParent());
        Assertions.assertEquals(dialog.getElement(),
                dialog.getFooter().root.getParent());
        Assertions.assertEquals("header-content",
                dialog.getHeader().root.getAttribute("slot"));
        Assertions.assertEquals("footer",
                dialog.getFooter().root.getAttribute("slot"));
    }

    @Test
    void headerAndFooter_excludedFromContent() {
        var content = new Div();
        dialog.add(content);
        dialog.getHeader().add(new Div());
        dialog.getFooter().add(new Div());

        // Only the main content is returned; the slotted header/footer wrappers
        // are excluded
        assertContent(content);
    }

    @Test
    void emptyHeader_wrapperDetached() {
        var child = new Div();
        dialog.getHeader().add(child);
        Assertions.assertEquals(dialog.getElement(),
                dialog.getHeader().root.getParent());

        dialog.getHeader().remove(child);
        Assertions.assertNull(dialog.getHeader().root.getParent());

        // Re-adding content re-attaches the wrapper to the dialog
        dialog.getHeader().add(new Div());
        Assertions.assertEquals(dialog.getElement(),
                dialog.getHeader().root.getParent());
    }

    @Test
    void headerPresent_getComponentCountExcludesHeaderFooter() {
        dialog.add(new Div());
        dialog.getHeader().add(new Div());
        dialog.getFooter().add(new Div());

        Assertions.assertEquals(1, dialog.getComponentCount());
    }

    @Test
    void headerPresent_addComponentAtIndex_contentOrderPreserved() {
        // Header content added first, so its wrapper precedes main content in
        // the element child list
        dialog.getHeader().add(new Div());

        var a = new Div();
        var b = new Div();
        var c = new Div();
        dialog.add(a);
        dialog.add(b);
        dialog.addComponentAtIndex(1, c);

        assertContent(a, c, b);
    }

    @Test
    void headerPresent_addComponentAtIndexOutOfRange_throws() {
        dialog.getHeader().add(new Div());
        dialog.add(new Div());

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> dialog.addComponentAtIndex(2, new Div()));
    }

    @Test
    void removeAll_doesNotRemoveHeaderOrFooter() {
        dialog.getHeader().add(new Div());
        dialog.getFooter().add(new Div());

        dialog.removeAll();

        Assertions.assertEquals(dialog.getElement(),
                dialog.getHeader().root.getParent());
        Assertions.assertEquals(dialog.getElement(),
                dialog.getFooter().root.getParent());
    }

    /**
     * Asserts that the given components are the dialog's main content, in
     * order: returned by {@link Dialog#getChildren()} and slotted as direct
     * light-DOM children of the dialog element (default slot, i.e. without a
     * {@code slot} attribute).
     */
    private void assertContent(Component... expected) {
        Assertions.assertEquals(List.of(expected),
                dialog.getChildren().collect(Collectors.toList()));
        for (Component component : expected) {
            Assertions.assertEquals(dialog.getElement(),
                    component.getElement().getParent());
            Assertions.assertFalse(component.getElement().hasAttribute("slot"));
        }
    }

}
