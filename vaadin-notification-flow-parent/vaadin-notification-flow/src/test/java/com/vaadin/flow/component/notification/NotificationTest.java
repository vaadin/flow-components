/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.notification;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.internal.UIInternals;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinSession;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Unit tests for the Notification.
 */
@NotThreadSafe
public class NotificationTest {

    private UI ui;

    @Before
    public void setup() {
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui = new UI();
        ui.getInternals().setSession(session);

        UI.setCurrent(ui);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void createNotificationWithComponents_componentsArePartOfGetChildren() {
        Label label1 = new Label("Label 1");
        Label label2 = new Label("Label 2");
        Label label3 = new Label("Label 3");

        Notification notification = new Notification(label1, label2);

        List<Component> children = notification.getChildren()
                .collect(Collectors.toList());
        Assert.assertEquals(2, children.size());
        Assert.assertThat(children, CoreMatchers.hasItems(label1, label2));

        notification.add(label3);
        children = notification.getChildren().collect(Collectors.toList());
        Assert.assertEquals(3, children.size());
        Assert.assertThat(children,
                CoreMatchers.hasItems(label1, label2, label3));

        notification.remove(label2);
        children = notification.getChildren().collect(Collectors.toList());
        Assert.assertEquals(2, children.size());
        Assert.assertThat(children, CoreMatchers.hasItems(label1, label3));

        label1.getElement().removeFromParent();
        children = notification.getChildren().collect(Collectors.toList());
        Assert.assertEquals(1, children.size());
        Assert.assertThat(children, CoreMatchers.hasItems(label3));

        notification.removeAll();
        children = notification.getChildren().collect(Collectors.toList());
        Assert.assertEquals(0, children.size());
    }

    @Test
    public void createNotificationWithComponentsInsideComponent_onlyRootComponentsAreReturned() {
        Div container1 = new Div();
        Div container2 = new Div(container1);

        Notification notification = new Notification(container2);
        List<Component> children = notification.getChildren()
                .collect(Collectors.toList());
        Assert.assertEquals(1, children.size());
        Assert.assertThat(children, CoreMatchers.hasItems(container2));
        Assert.assertThat(children,
                CoreMatchers.not(CoreMatchers.hasItem(container1)));
    }

    @Test
    public void constructorsWithNoDurationCreateNotCloseableNotifications() {
        final long constructorsWithoutDurationParameter = 3L;

        long constructorsWithNoIntParameter = Stream
                .of(Notification.class.getConstructors())
                .filter(constructor -> Stream
                        .of(constructor.getParameterTypes())
                        .noneMatch(int.class::equals))
                .count();
        Assert.assertEquals(
                "Unexpected number of constructors without duration parameter, please test that it has default duration and increment the number",
                constructorsWithoutDurationParameter,
                constructorsWithNoIntParameter);

        Collection<Notification> notificationsToCheck = Arrays.asList(
                new Notification(), new Notification("test"),
                new Notification(new Label("one"), new Label("two")));

        Assert.assertEquals(
                "Not all of the Notification constructors without duration parameter are tested",
                constructorsWithoutDurationParameter,
                notificationsToCheck.size());

        final int expectedDuration = 0;
        for (Notification notification : notificationsToCheck) {
            Assert.assertEquals(String.format(
                    "Each Notification object created with the constructor that has no 'duration' parameter should have duration set as '%d'",
                    expectedDuration), expectedDuration,
                    notification.getDuration());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void setOpened_noUiInstance() {
        UI.setCurrent(null);
        Notification notification = new Notification();
        notification.setOpened(true);
    }

    @Test
    public void defaultPositionValue() {
        Notification notification = new Notification();

        // default CTOR sets position explicitly (as any other CTOR)
        Assert.assertEquals(Position.BOTTOM_START, notification.getPosition());

        // There is no API to reset position, so set position to null as a
        // property, the default client side value is bottom start
        notification.getElement().setProperty("position", null);

        Assert.assertEquals(Position.BOTTOM_START, notification.getPosition());
    }

    @Test
    public void showNotification_defaultPositionAndDurationValues() {
        Notification notification = Notification.show("foo");

        Assert.assertEquals(Position.BOTTOM_START, notification.getPosition());
        Assert.assertEquals(5000, notification.getDuration());
    }

    @Test
    public void addComponent_setText_notificationHasText() {
        Notification notification = new Notification();

        notification.add(new Div());
        notification.setText("foo");

        Assert.assertEquals("foo",
                notification.getElement().getProperty("text"));
    }

    @Test
    public void setText_addComponent_notificationDoesNotHaveText() {
        Notification notification = new Notification();

        notification.setText("foo");
        notification.add(new Div());

        Assert.assertEquals(null,
                notification.getElement().getProperty("text"));
    }

    @Test
    public void setText_setTextNull_notificationDoesNotHaveText() {
        Notification notification = new Notification();

        notification.setText("foo");
        notification.setText(null);

        Assert.assertEquals(null,
                notification.getElement().getProperty("text"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addComponentAtIndex_negativeIndex() {
        addDivAtIndex(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addComponentAtIndex_indexIsBiggerThanChildrenCount() {
        addDivAtIndex(1);
    }

    private void addDivAtIndex(int index) {
        Notification notification = new Notification();

        Div div = new Div();
        notification.addComponentAtIndex(index, div);
    }

    @Test
    public void hasStyle() {
        Notification notification = new Notification();
        Assert.assertTrue(notification instanceof HasStyle);
    }

    @Test
    public void createNotification_closeOnParentDetach() {
        // Create a Notification manually and add it to a parent container
        Notification notification = new Notification();
        notification.open();
        Div parent = new Div(notification);
        // Add the parent to the UI
        ui.add(parent);

        // The notification was opened manually. Check that it's still open.
        Assert.assertTrue(notification.isOpened());

        // Remove the parent container from the UI
        ui.remove(parent);
        // Auto-close happens in before client response
        flushBeforeClientResponse();

        // The notification should have been closed on detach, even if it was
        // the parent that was removed
        Assert.assertFalse(notification.isOpened());
        // The parent reference should not have changed
        Assert.assertEquals(notification.getParent().get(), parent);
    }

    @Test
    public void showNotificationInModal_closeAndDetachOnParentDetach() {
        // Create a modal parent container and add it to the UI
        Div parent = new Div();
        ui.add(parent);
        ui.setChildComponentModal(parent, true);

        // Use Notification.show() helper to create a notification.
        // It will be automatically added to the modal parent container (before
        // client response)
        Notification notification = Notification.show("foo");
        flushBeforeClientResponse();

        // Check that the notification is opened and attached to the parent
        // container
        Assert.assertTrue(notification.isOpened());
        Assert.assertTrue(parent.getChildren().collect(Collectors.toList())
                .contains(notification));

        // Remove the modal parent container from the UI
        ui.remove(parent);
        // Auto-close happens in before client response
        flushBeforeClientResponse();

        // The notification should have been closed on detach, even if it was
        // the parent that was removed
        Assert.assertFalse(notification.isOpened());
        // The notification should have been automatically removed from the
        // parent container
        Assert.assertFalse(parent.getChildren().collect(Collectors.toList())
                .contains(notification));
    }

    @Test
    public void showNotification_addManually_dontDetachOnParentDetach() {
        // Use Notification.show() helper to create a notification.
        Notification notification = Notification.show("foo");

        // Manually add the notification to a parent
        Div parent = new Div(notification);
        ui.add(parent);
        // Flush
        flushBeforeClientResponse();

        // Check that the notification is attached to the parent container
        Assert.assertEquals(notification.getParent().get(), parent);

        // Remove the modal parent container from the UI
        ui.remove(parent);
        // Auto-removal happens in before client response
        flushBeforeClientResponse();

        // Even though the notification was created using Notification.show(),
        // it got was manually added to the parent container so it should not
        // have been automatically removed from it.
        Assert.assertEquals(notification.getParent().get(), parent);
    }

    private void flushBeforeClientResponse() {
        UIInternals internals = ui.getInternals();
        internals.getStateTree().runExecutionsBeforeClientResponse();
    }
}
