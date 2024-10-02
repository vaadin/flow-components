/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class DashboardWidgetTest extends DashboardTestBase {

    @Test
    public void assertDefaultTitle() {
        DashboardWidget widget = new DashboardWidget();
        Assert.assertNull(widget.getTitle());
    }

    @Test
    public void setTitle_returnsCorrectTitle() {
        String valueToSet = "New title";
        DashboardWidget widget = new DashboardWidget();
        widget.setTitle(valueToSet);
        Assert.assertEquals(valueToSet, widget.getTitle());
    }

    @Test
    public void setTitleNull_returnsEmptyTitle() {
        DashboardWidget widget = new DashboardWidget();
        widget.setTitle("New title");
        widget.setTitle(null);
        Assert.assertEquals("", widget.getTitle());
    }

    @Test
    public void addWidgetToLayout_widgetIsAdded() {
        Div layout = new Div();
        getUi().add(layout);
        DashboardWidget widget = new DashboardWidget();
        layout.add(widget);
        fakeClientCommunication();
        Assert.assertTrue(layout.getChildren().anyMatch(widget::equals));
    }

    @Test
    public void removeWidgetFromLayout_widgetIsRemoved() {
        Div layout = new Div();
        getUi().add(layout);
        DashboardWidget widget = new DashboardWidget();
        layout.add(widget);
        fakeClientCommunication();
        layout.remove(widget);
        fakeClientCommunication();
        Assert.assertTrue(layout.getChildren().noneMatch(widget::equals));
    }

    @Test
    public void addWidgetToLayout_removeFromParent_widgetIsRemoved() {
        Div layout = new Div();
        getUi().add(layout);
        DashboardWidget widget = new DashboardWidget();
        layout.add(widget);
        fakeClientCommunication();
        widget.removeFromParent();
        fakeClientCommunication();
        Assert.assertTrue(layout.getChildren().noneMatch(widget::equals));
    }

    @Test
    public void addWidgetFromLayoutToAnotherLayout_widgetIsMoved() {
        Div parent = new Div();
        getUi().add(parent);
        DashboardWidget widget = new DashboardWidget();
        parent.add(widget);
        fakeClientCommunication();
        Div newParent = new Div();
        getUi().add(newParent);
        newParent.add(widget);
        fakeClientCommunication();
        Assert.assertTrue(parent.getChildren().noneMatch(widget::equals));
        Assert.assertTrue(newParent.getChildren().anyMatch(widget::equals));
    }

    @Test
    public void assertDefaultColspan() {
        DashboardWidget widget = new DashboardWidget();
        Assert.assertEquals(1, widget.getColspan());
    }

    @Test
    public void setValidColspan_returnsCorrectColspan() {
        int valueToSet = 2;
        DashboardWidget widget = new DashboardWidget();
        widget.setColspan(valueToSet);
        Assert.assertEquals(valueToSet, widget.getColspan());
    }

    @Test
    public void setInvalidColspan_throwsIllegalArgumentException() {
        DashboardWidget widget = new DashboardWidget();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> widget.setColspan(0));
    }

    @Test
    public void assertDefaultRowspan() {
        DashboardWidget widget = new DashboardWidget();
        Assert.assertEquals(1, widget.getRowspan());
    }

    @Test
    public void setValidRowspan_returnsCorrectRowspan() {
        int valueToSet = 2;
        DashboardWidget widget = new DashboardWidget();
        widget.setRowspan(valueToSet);
        Assert.assertEquals(valueToSet, widget.getRowspan());
    }

    @Test
    public void setInvalidRowspan_throwsIllegalArgumentException() {
        DashboardWidget widget = new DashboardWidget();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> widget.setRowspan(0));
    }

    @Test
    public void defaultContentIsNull() {
        DashboardWidget widget = new DashboardWidget();
        Assert.assertNull(widget.getContent());
    }

    @Test
    public void setContentToEmptyWidget_correctContentIsSet() {
        Div content = new Div();
        DashboardWidget widget = new DashboardWidget();
        widget.setContent(content);
        Assert.assertEquals(content, widget.getContent());
    }

    @Test
    public void setAnotherContentToNonEmptyWidget_correctContentIsSet() {
        DashboardWidget widget = new DashboardWidget();
        widget.setContent(new Div());
        Span newContent = new Span();
        widget.setContent(newContent);
        Assert.assertEquals(newContent, widget.getContent());
    }

    @Test
    public void setTheSameContentToNonEmptyWidget_correctContentIsSet() {
        Div content = new Div();
        DashboardWidget widget = new DashboardWidget();
        widget.setContent(content);
        widget.setContent(content);
        Assert.assertEquals(content, widget.getContent());
    }

    @Test
    public void setNullContentToNonEmptyWidget_contentIsRemoved() {
        DashboardWidget widget = new DashboardWidget();
        widget.setContent(new Div());
        widget.setContent(null);
        Assert.assertNull(widget.getContent());
    }

    @Test
    public void defaultHeaderIsNull() {
        DashboardWidget widget = new DashboardWidget();
        Assert.assertNull(widget.getHeaderComponent());
    }

    @Test
    public void setHeaderToEmptyWidget_correctHeaderIsSet() {
        Div header = new Div();
        DashboardWidget widget = new DashboardWidget();
        widget.setHeaderComponent(header);
        Assert.assertEquals(header, widget.getHeaderComponent());
    }

    @Test
    public void setAnotherHeaderToNonEmptyWidget_correctHeaderIsSet() {
        DashboardWidget widget = new DashboardWidget();
        widget.setHeaderComponent(new Div());
        Span newHeader = new Span();
        widget.setHeaderComponent(newHeader);
        Assert.assertEquals(newHeader, widget.getHeaderComponent());
    }

    @Test
    public void setTheSameHeaderToNonEmptyWidget_correctHeaderIsSet() {
        Div header = new Div();
        DashboardWidget widget = new DashboardWidget();
        widget.setHeaderComponent(header);
        widget.setHeaderComponent(header);
        Assert.assertEquals(header, widget.getHeaderComponent());
    }

    @Test
    public void setNullHeaderToNonEmptyWidget_headerIsRemoved() {
        DashboardWidget widget = new DashboardWidget();
        widget.setHeaderComponent(new Div());
        widget.setHeaderComponent(null);
        Assert.assertNull(widget.getHeaderComponent());
    }

    @Test
    public void setNullHeaderToWidgetWithContent_contentIsNotRemoved() {
        Div content = new Div();
        DashboardWidget widget = new DashboardWidget();
        widget.setContent(content);
        widget.setHeaderComponent(null);
        Assert.assertEquals(content, widget.getContent());
    }

    @Test
    public void setNullContentToWidgetWithHeader_headerIsNotRemoved() {
        Div header = new Div();
        DashboardWidget widget = new DashboardWidget();
        widget.setHeaderComponent(header);
        widget.setContent(null);
        Assert.assertEquals(header, widget.getHeaderComponent());
    }

    @Test
    public void setHeaderToWidgetWithContent_contentAndHeaderCorrectlyRetrieved() {
        Div content = new Div();
        Span header = new Span();
        DashboardWidget widget = new DashboardWidget();
        widget.setContent(content);
        widget.setHeaderComponent(header);
        Assert.assertEquals(content, widget.getContent());
        Assert.assertEquals(header, widget.getHeaderComponent());
    }

    @Test
    public void setContentToWidgetWithHeader_contentAndHeaderCorrectlyRetrieved() {
        Div content = new Div();
        Span header = new Span();
        DashboardWidget widget = new DashboardWidget();
        widget.setHeaderComponent(header);
        widget.setContent(content);
        Assert.assertEquals(content, widget.getContent());
        Assert.assertEquals(header, widget.getHeaderComponent());
    }

    @Test
    public void setWidgetVisibility_exceptionIsThrown() {
        DashboardWidget widget = new DashboardWidget();
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> widget.setVisible(false));
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> widget.setVisible(true));
    }

    @Test
    public void getWidgetVisibility_returnsTrue() {
        DashboardWidget widget = new DashboardWidget();
        Assert.assertTrue(widget.isVisible());
    }
}
