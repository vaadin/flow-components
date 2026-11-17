/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.tests.MockUIExtension;

class PdfViewerToolbarActionsTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final PdfViewer viewer = new PdfViewer();

    @Test
    void getToolbarActionsNotCalled_noChildElements() {
        Assertions.assertEquals(0, viewer.getElement().getChildCount());
    }

    @Test
    void getToolbarActions_sameInstanceReturned() {
        Assertions.assertSame(viewer.getToolbarActions(),
                viewer.getToolbarActions());
    }

    @Test
    void getToolbarActions_containerAddedToSlot() {
        var root = viewer.getToolbarActions().getElement();
        Assertions.assertEquals(viewer.getElement(), root.getParent());
        Assertions.assertEquals("toolbar-actions", root.getAttribute("slot"));
        Assertions.assertEquals("contents", root.getStyle().get("display"));
    }

    @Test
    void addToToolbarActions_componentsAddedToContainer() {
        var first = new Span();
        var second = new Span();
        viewer.getToolbarActions().add(first, second);

        var root = viewer.getToolbarActions().getElement();
        Assertions.assertEquals(root, first.getElement().getParent());
        Assertions.assertEquals(root, second.getElement().getParent());
    }
}
