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
package com.vaadin.flow.component.notification;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

/**
 * Unit tests for the Notification.
 */
@NotThreadSafe
class NotificationTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void createNotificationWithComponents_componentsArePartOfGetChildren() {
        Span span1 = new Span("Text 1");
        Span span2 = new Span("Text 2");
        Span span3 = new Span("Text 3");

        Notification notification = new Notification(span1, span2);

        List<Component> children = notification.getChildren()
                .collect(Collectors.toList());
        Assertions.assertEquals(2, children.size());
        Assertions.assertTrue(children.contains(span1));
        Assertions.assertTrue(children.contains(span2));

        notification.add(span3);
        children = notification.getChildren().collect(Collectors.toList());
        Assertions.assertEquals(3, children.size());
        Assertions.assertTrue(children.contains(span1));
        Assertions.assertTrue(children.contains(span2));
        Assertions.assertTrue(children.contains(span3));

        notification.remove(span2);
        children = notification.getChildren().collect(Collectors.toList());
        Assertions.assertEquals(2, children.size());
        Assertions.assertTrue(children.contains(span1),
                "Children should contain span1");
        Assertions.assertTrue(children.contains(span3),
                "Children should contain span3");

        span1.getElement().removeFromParent();
        children = notification.getChildren().collect(Collectors.toList());
        Assertions.assertEquals(1, children.size());
        Assertions.assertTrue(children.contains(span3),
                "Children should contain span3");

        notification.removeAll();
        children = notification.getChildren().collect(Collectors.toList());
        Assertions.assertEquals(0, children.size());
    }

    @Test
    void createNotificationWithComponentsInsideComponent_onlyRootComponentsAreReturned() {
        Div container1 = new Div();
        Div container2 = new Div(container1);

        Notification notification = new Notification(container2);
        List<Component> children = notification.getChildren()
                .collect(Collectors.toList());
        Assertions.assertEquals(1, children.size());
        Assertions.assertTrue(children.contains(container2),
                "Children should contain container2");
        Assertions.assertFalse(children.contains(container1),
                "Children should not contain container1");
    }

    @Test
    void constructorsWithNoDurationCreateNotCloseableNotifications() {
        final long constructorsWithoutDurationParameter = 3L;

        long constructorsWithNoIntParameter = Stream
                .of(Notification.class.getConstructors())
                .filter(constructor -> Stream
                        .of(constructor.getParameterTypes())
                        .noneMatch(int.class::equals))
                .count();
        Assertions.assertEquals(constructorsWithoutDurationParameter,
                constructorsWithNoIntParameter,
                "Unexpected number of constructors without duration parameter, please test that it has default duration and increment the number");

        Collection<Notification> notificationsToCheck = Arrays.asList(
                new Notification(), new Notification("test"),
                new Notification(new Span("one"), new Span("two")));

        Assertions.assertEquals(constructorsWithoutDurationParameter,
                notificationsToCheck.size(),
                "Not all of the Notification constructors without duration parameter are tested");

        final int expectedDuration = 0;
        for (Notification notification : notificationsToCheck) {
            Assertions.assertEquals(expectedDuration,
                    notification.getDuration(),
                    String.format(
                            "Each Notification object created with the constructor that has no 'duration' parameter should have duration set as '%d'",
                            expectedDuration));
        }
    }

    @Test
    void setOpened_noUiInstance() {
        ui.clearUI();
        Notification notification = new Notification();
        Assertions.assertThrows(IllegalStateException.class,
                () -> notification.setOpened(true));
    }

    @Test
    void defaultPositionValue() {
        Notification notification = new Notification();

        // default CTOR sets position explicitly (as any other CTOR)
        Assertions.assertEquals(Position.BOTTOM_START,
                notification.getPosition());

        // There is no API to reset position, so set position to null as a
        // property, the default client side value is bottom start
        notification.getElement().setProperty("position", null);

        Assertions.assertEquals(Position.BOTTOM_START,
                notification.getPosition());
    }

    @Test
    void showNotification_defaultPositionAndDurationValues() {
        Notification notification = Notification.show("foo");

        Assertions.assertEquals(Position.BOTTOM_START,
                notification.getPosition());
        Assertions.assertEquals(5000, notification.getDuration());
    }

    @Test
    void addComponent_setText_notificationHasText() {
        Notification notification = new Notification();

        notification.add(new Div());
        notification.setText("foo");

        Assertions.assertEquals("foo",
                notification.getElement().getProperty("text"));
    }

    @Test
    void setText_addComponent_notificationDoesNotHaveText() {
        Notification notification = new Notification();

        notification.setText("foo");
        notification.add(new Div());

        Assertions.assertEquals(null,
                notification.getElement().getProperty("text"));
    }

    @Test
    void setText_setTextNull_notificationDoesNotHaveText() {
        Notification notification = new Notification();

        notification.setText("foo");
        notification.setText(null);

        Assertions.assertEquals(null,
                notification.getElement().getProperty("text"));
    }

    @Test
    void addComponentAtIndex_negativeIndex() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> addDivAtIndex(-1));
    }

    @Test
    void addComponentAtIndex_indexIsBiggerThanChildrenCount() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> addDivAtIndex(1));
    }

    private void addDivAtIndex(int index) {
        Notification notification = new Notification();

        Div div = new Div();
        notification.addComponentAtIndex(index, div);
    }

    @Test
    void hasStyle() {
        Notification notification = new Notification();
        Assertions.assertTrue(notification instanceof HasStyle);
    }

    @Test
    void createNotification_closeOnParentDetach() {
        // Create a Notification manually and add it to a parent container
        Notification notification = new Notification();
        notification.open();
        Div parent = new Div(notification);
        // Add the parent to the UI
        ui.add(parent);

        // The notification was opened manually. Check that it's still open.
        Assertions.assertTrue(notification.isOpened());

        // Remove the parent container from the UI
        ui.remove(parent);
        // Auto-close happens in before client response
        ui.fakeClientCommunication();

        // The notification should have been closed on detach, even if it was
        // the parent that was removed
        Assertions.assertFalse(notification.isOpened());
        // The parent reference should not have changed
        Assertions.assertEquals(notification.getParent().get(), parent);
    }

    @Test
    void showNotificationInModal_closeAndDetachOnParentDetach() {
        // Create a modal parent container and add it to the UI
        Div parent = new Div();
        ui.add(parent);
        ui.getUI().setChildComponentModal(parent, true);

        // Use Notification.show() helper to create a notification.
        // It will be automatically added to the modal parent container (before
        // client response)
        Notification notification = Notification.show("foo");
        ui.fakeClientCommunication();

        // Check that the notification is opened and attached to the parent
        // container
        Assertions.assertTrue(notification.isOpened());
        Assertions.assertTrue(parent.getChildren().collect(Collectors.toList())
                .contains(notification));

        // Remove the modal parent container from the UI
        ui.remove(parent);
        // Auto-close happens in before client response
        ui.fakeClientCommunication();

        // The notification should have been closed on detach, even if it was
        // the parent that was removed
        Assertions.assertFalse(notification.isOpened());
        // The notification should have been automatically removed from the
        // parent container
        Assertions.assertFalse(parent.getChildren().collect(Collectors.toList())
                .contains(notification));
    }

    @Test
    void showNotification_addManually_dontDetachOnParentDetach() {
        // Use Notification.show() helper to create a notification.
        Notification notification = Notification.show("foo");

        // Manually add the notification to a parent
        Div parent = new Div(notification);
        ui.add(parent);
        // Flush
        ui.fakeClientCommunication();

        // Check that the notification is attached to the parent container
        Assertions.assertEquals(notification.getParent().get(), parent);

        // Remove the modal parent container from the UI
        ui.remove(parent);
        // Auto-removal happens in before client response
        ui.fakeClientCommunication();

        // Even though the notification was created using Notification.show(),
        // it got was manually added to the parent container so it should not
        // have been automatically removed from it.
        Assertions.assertEquals(notification.getParent().get(), parent);
    }

    @Test
    void unregisterOpenedChangeListenerOnEvent() {
        var notification = new Notification();

        var listenerInvokedCount = new AtomicInteger(0);
        notification.addOpenedChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });

        notification.open();
        notification.close();

        Assertions.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    void openedChangeListener_shouldWorkForAllConstructors() {
        var listenerInvokedCount = new AtomicInteger(0);

        var notifications = Stream.of(//
                new Notification(), //
                new Notification(new Span("text")), //
                new Notification("text"), //
                new Notification("text", 1000), //
                new Notification("text", 1000, Position.MIDDLE))
                .map(notification -> {
                    notification.addOpenedChangeListener(e -> {
                        listenerInvokedCount.incrementAndGet();
                    });
                    return notification;
                }).toList();

        notifications.forEach(notification -> {
            notification.open();
            notification.close();
        });

        // two invocations expected per notification - open & close
        int expectedInvocationsCount = 2 * notifications.size();
        Assertions.assertEquals(expectedInvocationsCount,
                listenerInvokedCount.get());
    }

    @Test
    void setText_notificationHasUnmodifiedText() {
        Notification notification = new Notification();
        notification.setText("foo > bar");

        Assertions.assertEquals("foo > bar",
                notification.getElement().getProperty("text"));
    }

    @Test
    void setAssertive_isAssertive() {
        Notification notification = new Notification();
        notification.setAssertive(true);
        Assertions.assertEquals(true, notification.isAssertive());
        Assertions.assertTrue(
                notification.getElement().getProperty("assertive", false));

        notification.setAssertive(false);
        Assertions.assertEquals(false, notification.isAssertive());
        Assertions.assertFalse(
                notification.getElement().getProperty("assertive", false));
    }
}
