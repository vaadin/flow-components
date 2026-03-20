/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

class DashboardWidgetTest extends DashboardTestBase {

    @Test
    void assertDefaultTitle() {
        DashboardWidget widget = getNewWidget();
        Assertions.assertNull(widget.getTitle());
    }

    @Test
    void setTitle_returnsCorrectTitle() {
        String valueToSet = "New title";
        DashboardWidget widget = getNewWidget();
        widget.setTitle(valueToSet);
        Assertions.assertEquals(valueToSet, widget.getTitle());
    }

    @Test
    void setTitleInConstructor_returnsCorrectTitle() {
        var valueToSet = "New title";
        var widget = new DashboardWidget(valueToSet);
        widget.setTitle(valueToSet);
        Assertions.assertEquals(valueToSet, widget.getTitle());
    }

    @Test
    void setTitleNull_returnsEmptyTitle() {
        DashboardWidget widget = getNewWidget();
        widget.setTitle("New title");
        widget.setTitle(null);
        Assertions.assertEquals("", widget.getTitle());
    }

    @Test
    void addWidgetToLayout_widgetIsAdded() {
        Div layout = new Div();
        ui.add(layout);
        DashboardWidget widget = getNewWidget();
        layout.add(widget);
        ui.fakeClientCommunication();
        Assertions.assertTrue(layout.getChildren().anyMatch(widget::equals));
    }

    @Test
    void removeWidgetFromLayout_widgetIsRemoved() {
        Div layout = new Div();
        ui.add(layout);
        DashboardWidget widget = getNewWidget();
        layout.add(widget);
        ui.fakeClientCommunication();
        layout.remove(widget);
        ui.fakeClientCommunication();
        Assertions.assertTrue(layout.getChildren().noneMatch(widget::equals));
    }

    @Test
    void addWidgetToLayout_removeFromParent_widgetIsRemoved() {
        Div layout = new Div();
        ui.add(layout);
        DashboardWidget widget = getNewWidget();
        layout.add(widget);
        ui.fakeClientCommunication();
        widget.removeFromParent();
        ui.fakeClientCommunication();
        Assertions.assertTrue(layout.getChildren().noneMatch(widget::equals));
    }

    @Test
    void addWidgetFromLayoutToAnotherLayout_widgetIsMoved() {
        Div parent = new Div();
        ui.add(parent);
        DashboardWidget widget = getNewWidget();
        parent.add(widget);
        ui.fakeClientCommunication();
        Div newParent = new Div();
        ui.add(newParent);
        newParent.add(widget);
        ui.fakeClientCommunication();
        Assertions.assertTrue(parent.getChildren().noneMatch(widget::equals));
        Assertions.assertTrue(newParent.getChildren().anyMatch(widget::equals));
    }

    @Test
    void assertDefaultColspan() {
        DashboardWidget widget = getNewWidget();
        Assertions.assertEquals(1, widget.getColspan());
    }

    @Test
    void setValidColspan_returnsCorrectColspan() {
        int valueToSet = 2;
        DashboardWidget widget = getNewWidget();
        widget.setColspan(valueToSet);
        Assertions.assertEquals(valueToSet, widget.getColspan());
    }

    @Test
    void setInvalidColspan_throwsIllegalArgumentException() {
        DashboardWidget widget = getNewWidget();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> widget.setColspan(0));
    }

    @Test
    void assertDefaultRowspan() {
        DashboardWidget widget = getNewWidget();
        Assertions.assertEquals(1, widget.getRowspan());
    }

    @Test
    void setValidRowspan_returnsCorrectRowspan() {
        int valueToSet = 2;
        DashboardWidget widget = getNewWidget();
        widget.setRowspan(valueToSet);
        Assertions.assertEquals(valueToSet, widget.getRowspan());
    }

    @Test
    void setInvalidRowspan_throwsIllegalArgumentException() {
        DashboardWidget widget = getNewWidget();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> widget.setRowspan(0));
    }

    @Test
    void defaultContentIsNull() {
        DashboardWidget widget = getNewWidget();
        Assertions.assertNull(widget.getContent());
    }

    @Test
    void setContentToEmptyWidget_correctContentIsSet() {
        Div content = new Div();
        DashboardWidget widget = getNewWidget();
        widget.setContent(content);
        Assertions.assertEquals(content, widget.getContent());
    }

    @Test
    void setContentInConstructor_correctContentIsSet() {
        var content = new Div();
        var widget = new DashboardWidget(content);
        Assertions.assertEquals(content, widget.getContent());
    }

    @Test
    void setTitleAndContentInConstructor_correctValuesAreSet() {
        var title = "New title";
        var content = new Div();
        var widget = new DashboardWidget(title, content);
        Assertions.assertEquals(title, widget.getTitle());
        Assertions.assertEquals(content, widget.getContent());
    }

    @Test
    void setAnotherContentToNonEmptyWidget_correctContentIsSet() {
        DashboardWidget widget = getNewWidget();
        widget.setContent(new Div());
        Span newContent = new Span();
        widget.setContent(newContent);
        Assertions.assertEquals(newContent, widget.getContent());
    }

    @Test
    void setTheSameContentToNonEmptyWidget_correctContentIsSet() {
        Div content = new Div();
        DashboardWidget widget = getNewWidget();
        widget.setContent(content);
        widget.setContent(content);
        Assertions.assertEquals(content, widget.getContent());
    }

    @Test
    void setNullContentToNonEmptyWidget_contentIsRemoved() {
        DashboardWidget widget = getNewWidget();
        widget.setContent(new Div());
        widget.setContent(null);
        Assertions.assertNull(widget.getContent());
    }

    @Test
    void defaultHeaderIsNull() {
        DashboardWidget widget = getNewWidget();
        Assertions.assertNull(widget.getHeaderContent());
    }

    @Test
    void setHeaderToEmptyWidget_correctHeaderIsSet() {
        Div header = new Div();
        DashboardWidget widget = getNewWidget();
        widget.setHeaderContent(header);
        Assertions.assertEquals(header, widget.getHeaderContent());
    }

    @Test
    void setAnotherHeaderToNonEmptyWidget_correctHeaderIsSet() {
        DashboardWidget widget = getNewWidget();
        widget.setHeaderContent(new Div());
        Span newHeader = new Span();
        widget.setHeaderContent(newHeader);
        Assertions.assertEquals(newHeader, widget.getHeaderContent());
    }

    @Test
    void setTheSameHeaderToNonEmptyWidget_correctHeaderIsSet() {
        Div header = new Div();
        DashboardWidget widget = getNewWidget();
        widget.setHeaderContent(header);
        widget.setHeaderContent(header);
        Assertions.assertEquals(header, widget.getHeaderContent());
    }

    @Test
    void setNullHeaderToNonEmptyWidget_headerIsRemoved() {
        DashboardWidget widget = getNewWidget();
        widget.setHeaderContent(new Div());
        widget.setHeaderContent(null);
        Assertions.assertNull(widget.getHeaderContent());
    }

    @Test
    void setNullHeaderToWidgetWithContent_contentIsNotRemoved() {
        Div content = new Div();
        DashboardWidget widget = getNewWidget();
        widget.setContent(content);
        widget.setHeaderContent(null);
        Assertions.assertEquals(content, widget.getContent());
    }

    @Test
    void setNullContentToWidgetWithHeader_headerIsNotRemoved() {
        Div header = new Div();
        DashboardWidget widget = getNewWidget();
        widget.setHeaderContent(header);
        widget.setContent(null);
        Assertions.assertEquals(header, widget.getHeaderContent());
    }

    @Test
    void setHeaderToWidgetWithContent_contentAndHeaderCorrectlyRetrieved() {
        Div content = new Div();
        Span header = new Span();
        DashboardWidget widget = getNewWidget();
        widget.setContent(content);
        widget.setHeaderContent(header);
        Assertions.assertEquals(content, widget.getContent());
        Assertions.assertEquals(header, widget.getHeaderContent());
    }

    @Test
    void setContentToWidgetWithHeader_contentAndHeaderCorrectlyRetrieved() {
        Div content = new Div();
        Span header = new Span();
        DashboardWidget widget = getNewWidget();
        widget.setHeaderContent(header);
        widget.setContent(content);
        Assertions.assertEquals(content, widget.getContent());
        Assertions.assertEquals(header, widget.getHeaderContent());
    }

    @Test
    void setWidgetVisibility_exceptionIsThrown() {
        DashboardWidget widget = getNewWidget();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> widget.setVisible(false));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> widget.setVisible(true));
    }

    @Test
    void getWidgetVisibility_returnsTrue() {
        DashboardWidget widget = getNewWidget();
        Assertions.assertTrue(widget.isVisible());
    }
}
