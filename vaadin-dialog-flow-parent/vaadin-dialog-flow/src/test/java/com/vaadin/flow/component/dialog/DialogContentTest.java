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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.tests.MockUIExtension;

class DialogContentTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Dialog dialog;

    @BeforeEach
    void setup() {
        dialog = new Dialog();
        ui.add(dialog);
    }

    private long wrapperCount(String slot) {
        return dialog.getElement().getChildren()
                .filter(element -> slot.equals(element.getAttribute("slot")))
                .count();
    }

    @Test
    void getContent_returnsSameInstance() {
        Assertions.assertSame(dialog.getContent(), dialog.getContent());
    }

    @Test
    void getContent_add_visibleThroughDialogGetChildren() {
        var content = new Div();
        dialog.getContent().add(content);
        Assertions.assertEquals(List.of(content),
                dialog.getChildren().toList());
    }

    @Test
    void dialogAdd_visibleThroughGetContent() {
        var content = new Div();
        dialog.add(content);
        Assertions.assertEquals(List.of(content),
                dialog.getContent().getChildren().toList());
    }

    @Test
    void getContent_getChildren_excludesHeaderAndFooterComponents() {
        dialog.getHeader().add(new Span());
        dialog.getFooter().add(new Span());
        var content = new Div();
        dialog.add(content);
        Assertions.assertEquals(List.of(content),
                dialog.getContent().getChildren().toList());
    }

    @Test
    void getContent_indexAndCountOperationsWork() {
        var first = new Div();
        var second = new Span();
        dialog.getHeader().add(new Span());
        dialog.add(first, second);
        Assertions.assertEquals(2, dialog.getContent().getComponentCount());
        Assertions.assertEquals(1, dialog.getContent().indexOf(second));
        Assertions.assertSame(first, dialog.getContent().getComponentAt(0));
    }

    @Test
    void getContent_addComponentAtIndex_ignoresWrappers() {
        dialog.getHeader().add(new Span());
        dialog.getFooter().add(new Span());
        var first = new Div();
        var second = new Div();
        dialog.add(first, second);

        var inserted = new Div();
        dialog.getContent().addComponentAtIndex(1, inserted);
        Assertions.assertEquals(List.of(first, inserted, second),
                dialog.getChildren().toList());
    }

    @Test
    void getContent_removeAll_keepsHeaderAndFooter() {
        dialog.getHeader().add(new Span());
        dialog.getFooter().add(new Span());
        dialog.add(new Div());
        dialog.getContent().removeAll();
        Assertions.assertEquals(0, dialog.getChildren().count());
        Assertions.assertEquals(1, wrapperCount("header-content"));
        Assertions.assertEquals(1, wrapperCount("footer"));
    }

    @Test
    void dialogAdd_headerChild_movesToContent_andDetachesEmptyWrapper() {
        var component = new Div();
        dialog.getHeader().add(component);
        Assertions.assertEquals(1, wrapperCount("header-content"));

        dialog.add(component);

        Assertions.assertEquals(List.of(component),
                dialog.getChildren().toList());
        Assertions.assertEquals(0, wrapperCount("header-content"));
    }

    @Test
    void getContentAdd_footerChild_movesToContent_andDetachesEmptyWrapper() {
        var component = new Div();
        dialog.getFooter().add(component);
        Assertions.assertEquals(1, wrapperCount("footer"));

        dialog.getContent().add(component);

        Assertions.assertEquals(List.of(component),
                dialog.getChildren().toList());
        Assertions.assertEquals(0, wrapperCount("footer"));
    }

    @Test
    void dialogAdd_headerChild_wrapperWithRemainingChildStaysAttached() {
        var moved = new Div();
        var remaining = new Div();
        dialog.getHeader().add(moved, remaining);

        dialog.add(moved);

        Assertions.assertEquals(1, wrapperCount("header-content"));
    }

    @Test
    void replace_headerChildAsReplacement_detachesEmptyWrapper() {
        var headerChild = new Div();
        dialog.getHeader().add(headerChild);
        var content = new Div();
        dialog.add(content);

        dialog.getContent().replace(content, headerChild);

        Assertions.assertEquals(List.of(headerChild),
                dialog.getChildren().toList());
        Assertions.assertEquals(0, wrapperCount("header-content"));
    }

    @Test
    void getContent_bindChildren_throwsUnsupported() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> dialog.getContent().bindChildren(null, null));
    }

    @Test
    void getContent_setEnabled_throws() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> dialog.getContent().setEnabled(false));
    }

    @Test
    void getContent_bindEnabled_throws() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> dialog.getContent().bindEnabled(null));
    }

    @Test
    void getContent_isEnabled_reflectsDialogState() {
        Assertions.assertTrue(dialog.getContent().isEnabled());
        dialog.setEnabled(false);
        Assertions.assertFalse(dialog.getContent().isEnabled());
    }
}
