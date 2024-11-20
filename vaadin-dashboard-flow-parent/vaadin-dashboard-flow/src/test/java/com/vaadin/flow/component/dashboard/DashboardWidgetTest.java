/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class DashboardWidgetTest extends DashboardTestBase {

    @Test
    public void assertDefaultTitle() {
        DashboardWidget widget = getNewWidget();
        Assert.assertNull(widget.getTitle());
    }

    @Test
    public void setTitle_returnsCorrectTitle() {
        String valueToSet = "New title";
        DashboardWidget widget = getNewWidget();
        widget.setTitle(valueToSet);
        Assert.assertEquals(valueToSet, widget.getTitle());
    }

    @Test
    public void setTitleInConstructor_returnsCorrectTitle() {
        var valueToSet = "New title";
        var widget = new DashboardWidget(valueToSet);
        widget.setFeatureFlagEnabled(true);
        widget.setTitle(valueToSet);
        Assert.assertEquals(valueToSet, widget.getTitle());
    }

    @Test
    public void setTitleNull_returnsEmptyTitle() {
        DashboardWidget widget = getNewWidget();
        widget.setTitle("New title");
        widget.setTitle(null);
        Assert.assertEquals("", widget.getTitle());
    }

    @Test
    public void addWidgetToLayout_widgetIsAdded() {
        Div layout = new Div();
        getUi().add(layout);
        DashboardWidget widget = getNewWidget();
        layout.add(widget);
        fakeClientCommunication();
        Assert.assertTrue(layout.getChildren().anyMatch(widget::equals));
    }

    @Test
    public void removeWidgetFromLayout_widgetIsRemoved() {
        Div layout = new Div();
        getUi().add(layout);
        DashboardWidget widget = getNewWidget();
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
        DashboardWidget widget = getNewWidget();
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
        DashboardWidget widget = getNewWidget();
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
        DashboardWidget widget = getNewWidget();
        Assert.assertEquals(1, widget.getColspan());
    }

    @Test
    public void setValidColspan_returnsCorrectColspan() {
        int valueToSet = 2;
        DashboardWidget widget = getNewWidget();
        widget.setColspan(valueToSet);
        Assert.assertEquals(valueToSet, widget.getColspan());
    }

    @Test
    public void setInvalidColspan_throwsIllegalArgumentException() {
        DashboardWidget widget = getNewWidget();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> widget.setColspan(0));
    }

    @Test
    public void assertDefaultRowspan() {
        DashboardWidget widget = getNewWidget();
        Assert.assertEquals(1, widget.getRowspan());
    }

    @Test
    public void setValidRowspan_returnsCorrectRowspan() {
        int valueToSet = 2;
        DashboardWidget widget = getNewWidget();
        widget.setRowspan(valueToSet);
        Assert.assertEquals(valueToSet, widget.getRowspan());
    }

    @Test
    public void setInvalidRowspan_throwsIllegalArgumentException() {
        DashboardWidget widget = getNewWidget();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> widget.setRowspan(0));
    }

    @Test
    public void defaultContentIsNull() {
        DashboardWidget widget = getNewWidget();
        Assert.assertNull(widget.getContent());
    }

    @Test
    public void setContentToEmptyWidget_correctContentIsSet() {
        Div content = new Div();
        DashboardWidget widget = getNewWidget();
        widget.setContent(content);
        Assert.assertEquals(content, widget.getContent());
    }

    @Test
    public void setContentInConstructor_correctContentIsSet() {
        var content = new Div();
        var widget = new DashboardWidget(content);
        widget.setFeatureFlagEnabled(true);
        Assert.assertEquals(content, widget.getContent());
    }

    @Test
    public void setTitleAndContentInConstructor_correctValuesAreSet() {
        var title = "New title";
        var content = new Div();
        var widget = new DashboardWidget(title, content);
        widget.setFeatureFlagEnabled(true);
        Assert.assertEquals(title, widget.getTitle());
        Assert.assertEquals(content, widget.getContent());
    }

    @Test
    public void setAnotherContentToNonEmptyWidget_correctContentIsSet() {
        DashboardWidget widget = getNewWidget();
        widget.setContent(new Div());
        Span newContent = new Span();
        widget.setContent(newContent);
        Assert.assertEquals(newContent, widget.getContent());
    }

    @Test
    public void setTheSameContentToNonEmptyWidget_correctContentIsSet() {
        Div content = new Div();
        DashboardWidget widget = getNewWidget();
        widget.setContent(content);
        widget.setContent(content);
        Assert.assertEquals(content, widget.getContent());
    }

    @Test
    public void setNullContentToNonEmptyWidget_contentIsRemoved() {
        DashboardWidget widget = getNewWidget();
        widget.setContent(new Div());
        widget.setContent(null);
        Assert.assertNull(widget.getContent());
    }

    @Test
    public void defaultHeaderIsNull() {
        DashboardWidget widget = getNewWidget();
        Assert.assertNull(widget.getHeaderContent());
    }

    @Test
    public void setHeaderToEmptyWidget_correctHeaderIsSet() {
        Div header = new Div();
        DashboardWidget widget = getNewWidget();
        widget.setHeaderContent(header);
        Assert.assertEquals(header, widget.getHeaderContent());
    }

    @Test
    public void setAnotherHeaderToNonEmptyWidget_correctHeaderIsSet() {
        DashboardWidget widget = getNewWidget();
        widget.setHeaderContent(new Div());
        Span newHeader = new Span();
        widget.setHeaderContent(newHeader);
        Assert.assertEquals(newHeader, widget.getHeaderContent());
    }

    @Test
    public void setTheSameHeaderToNonEmptyWidget_correctHeaderIsSet() {
        Div header = new Div();
        DashboardWidget widget = getNewWidget();
        widget.setHeaderContent(header);
        widget.setHeaderContent(header);
        Assert.assertEquals(header, widget.getHeaderContent());
    }

    @Test
    public void setNullHeaderToNonEmptyWidget_headerIsRemoved() {
        DashboardWidget widget = getNewWidget();
        widget.setHeaderContent(new Div());
        widget.setHeaderContent(null);
        Assert.assertNull(widget.getHeaderContent());
    }

    @Test
    public void setNullHeaderToWidgetWithContent_contentIsNotRemoved() {
        Div content = new Div();
        DashboardWidget widget = getNewWidget();
        widget.setContent(content);
        widget.setHeaderContent(null);
        Assert.assertEquals(content, widget.getContent());
    }

    @Test
    public void setNullContentToWidgetWithHeader_headerIsNotRemoved() {
        Div header = new Div();
        DashboardWidget widget = getNewWidget();
        widget.setHeaderContent(header);
        widget.setContent(null);
        Assert.assertEquals(header, widget.getHeaderContent());
    }

    @Test
    public void setHeaderToWidgetWithContent_contentAndHeaderCorrectlyRetrieved() {
        Div content = new Div();
        Span header = new Span();
        DashboardWidget widget = getNewWidget();
        widget.setContent(content);
        widget.setHeaderContent(header);
        Assert.assertEquals(content, widget.getContent());
        Assert.assertEquals(header, widget.getHeaderContent());
    }

    @Test
    public void setContentToWidgetWithHeader_contentAndHeaderCorrectlyRetrieved() {
        Div content = new Div();
        Span header = new Span();
        DashboardWidget widget = getNewWidget();
        widget.setHeaderContent(header);
        widget.setContent(content);
        Assert.assertEquals(content, widget.getContent());
        Assert.assertEquals(header, widget.getHeaderContent());
    }

    @Test
    public void setWidgetVisibility_exceptionIsThrown() {
        DashboardWidget widget = getNewWidget();
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> widget.setVisible(false));
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> widget.setVisible(true));
    }

    @Test
    public void getWidgetVisibility_returnsTrue() {
        DashboardWidget widget = getNewWidget();
        Assert.assertTrue(widget.isVisible());
    }
}
