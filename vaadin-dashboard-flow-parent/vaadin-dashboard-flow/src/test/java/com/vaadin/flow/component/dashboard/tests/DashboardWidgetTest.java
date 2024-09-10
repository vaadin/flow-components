/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.VaadinSession;

public class DashboardWidgetTest {

    private final UI ui = new UI();

    @Before
    public void setup() {
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void addWidgetToLayout_widgetIsAdded() {
        Div layout = new Div();
        ui.add(layout);
        DashboardWidget widget = new DashboardWidget();
        layout.add(widget);
        fakeClientCommunication();
        Assert.assertTrue(layout.getChildren().anyMatch(widget::equals));
    }

    @Test
    public void removeWidgetFromLayout_widgetIsRemoved() {
        Div layout = new Div();
        ui.add(layout);
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
        ui.add(layout);
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
        ui.add(parent);
        DashboardWidget widget = new DashboardWidget();
        parent.add(widget);
        fakeClientCommunication();
        Div newParent = new Div();
        ui.add(newParent);
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

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
