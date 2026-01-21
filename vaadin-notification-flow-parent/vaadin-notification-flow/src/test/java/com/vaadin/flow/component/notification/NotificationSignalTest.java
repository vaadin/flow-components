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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.signals.BindingActiveException;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class NotificationSignalTest extends AbstractSignalsUnitTest {

    private Notification notification;
    private ValueSignal<String> textSignal;
    private Signal<String> computedSignal;

    @Before
    public void setup() {
        UI.setCurrent(new UI());
        textSignal = new ValueSignal<>("foo");
        computedSignal = Signal.computed(() -> textSignal.value() + " bar");
    }

    @After
    public void tearDown() {
        if (notification != null) {
            notification.close();
            if (notification.isAttached()) {
                notification.removeFromParent();
            }
        }
        UI.setCurrent(null);
    }

    @Test
    public void textSignalCtor() {
        notification = new Notification(textSignal);
        UI.getCurrent().add(notification);
        assertTextSignalBindingActive();
        Assert.assertEquals(0, notification.getDuration(), 0);
        Assert.assertEquals("bottom-start",
                notification.getPosition().getClientName());
    }

    @Test
    public void textSignalAndDurationCtor() {
        notification = new Notification(textSignal, 4000);
        UI.getCurrent().add(notification);
        assertTextSignalBindingActive();
        Assert.assertEquals(4000, notification.getDuration(), 0);
        Assert.assertEquals("bottom-start",
                notification.getPosition().getClientName());
    }

    @Test
    public void textSignalAndDurationAndPositionCtor() {
        notification = new Notification(textSignal, 10000, Position.TOP_END);
        UI.getCurrent().add(notification);
        assertTextSignalBindingActive();
        Assert.assertEquals(10000, notification.getDuration(), 0);
        Assert.assertEquals("top-end",
                notification.getPosition().getClientName());
    }

    @Test
    public void textSignalAndDurationAndPositionAndAssertiveCtor() {
        notification = new Notification(textSignal, 10000, Position.TOP_END,
                true);
        UI.getCurrent().add(notification);
        assertTextSignalBindingActive();
        Assert.assertEquals(10000, notification.getDuration(), 0);
        Assert.assertEquals("top-end",
                notification.getPosition().getClientName());
        Assert.assertTrue(notification.isAssertive());
    }

    @Test(expected = BindingActiveException.class)
    public void textSignalAndSetText_error() {
        notification = new Notification(textSignal);
        notification.setText("bar");
    }

    @Test
    public void textSignal_removeBinding() {
        notification = new Notification(textSignal);
        UI.getCurrent().add(notification);

        notification.bindText(null);
        assertTextSignalBindingInactive();

        notification.setText("bar");
        Assert.assertEquals("bar", getNotificationText());

        notification.setText(null);
        Assert.assertEquals("", getNotificationText());
    }

    @Test
    public void textSignal_notOpened() {
        notification = new Notification(textSignal);
        UI.getCurrent().add(notification);
        assertTextSignalBindingActive();
    }

    @Test
    public void textSignal_openedAndClosed() {
        notification = new Notification(textSignal);
        UI.getCurrent().add(notification);
        notification.open();
        assertTextSignalBindingActive();

        notification.close();
        assertTextSignalBindingActive();
    }

    @Test
    public void textComputedSignalCtor() {
        notification = new Notification(computedSignal);
        UI.getCurrent().add(notification);
        Assert.assertEquals("foo bar", getNotificationText());
        textSignal.value("bar");
        Assert.assertEquals("bar bar", getNotificationText());
    }

    @Test(expected = BindingActiveException.class)
    public void textComputedSignalCtor_bindText() {
        notification = new Notification(computedSignal);
        notification.bindText(textSignal);
    }

    @Test
    public void textComputedSignalCtor_removeBindingAndBindText() {
        notification = new Notification(computedSignal);
        UI.getCurrent().add(notification);

        notification.bindText(null);
        notification.bindText(textSignal);
        assertTextSignalBindingActive();

        notification.bindText(null);
        assertTextSignalBindingInactive();
    }

    @Test
    public void textSignalConstructors_usesTextSupport() {
        // Test single parameter constructor
        notification = new Notification(textSignal);
        textSignal.value("text signal");
        UI.getCurrent().add(notification);
        Assert.assertEquals("text signal", notification.getTextSupport().get());
        Assert.assertThrows(BindingActiveException.class,
                () -> notification.setText("error"));
        Assert.assertThrows(BindingActiveException.class,
                () -> notification.bindText(computedSignal));

        // Test with duration
        notification = new Notification(textSignal, 4000);
        textSignal.value("text signal, duration");
        UI.getCurrent().add(notification);
        Assert.assertEquals("text signal, duration",
                notification.getTextSupport().get());
        Assert.assertThrows(BindingActiveException.class,
                () -> notification.setText("error"));
        Assert.assertThrows(BindingActiveException.class,
                () -> notification.bindText(computedSignal));

        // Test with duration and position
        notification = new Notification(textSignal, 4000, Position.TOP_END);
        textSignal.value("text signal, duration, position");
        UI.getCurrent().add(notification);
        Assert.assertEquals("text signal, duration, position",
                notification.getTextSupport().get());
        Assert.assertThrows(BindingActiveException.class,
                () -> notification.setText("error"));
        Assert.assertThrows(BindingActiveException.class,
                () -> notification.bindText(computedSignal));

        // Test with all parameters
        notification = new Notification(textSignal, 4000, Position.TOP_END,
                true);
        textSignal.value("text signal, duration, position, assertive");
        UI.getCurrent().add(notification);
        Assert.assertEquals("text signal, duration, position, assertive",
                notification.getTextSupport().get());
        Assert.assertThrows(BindingActiveException.class,
                () -> notification.setText("error"));
        Assert.assertThrows(BindingActiveException.class,
                () -> notification.bindText(computedSignal));
    }

    @Test
    public void setText_usesTextSupport() {
        notification = new Notification();
        UI.getCurrent().add(notification);
        notification.setText("initial");

        // Verify textSupport is used by checking it's the same instance
        Assert.assertSame(notification.getTextSupport().get(),
                notification.getElement().getProperty("text", ""));
    }

    @Test
    public void bindText_usesTextSupport() {
        notification = new Notification();
        UI.getCurrent().add(notification);

        textSignal.value("test");
        notification.bindText(textSignal);

        // Verify textSupport is used by checking binding is active
        Assert.assertEquals("test", notification.getTextSupport().get());

        // Change the signal value and verify it propagates through textSupport
        textSignal.value("updated");
        Assert.assertEquals("updated", notification.getTextSupport().get());
    }

    @Test
    public void setText_updatesTextSupport() {
        notification = new Notification();
        UI.getCurrent().add(notification);

        // Set initial value
        notification.setText("initial");

        // Verify textSupport holds the value
        Assert.assertEquals("initial", notification.getTextSupport().get());

        // Update value
        notification.setText("updated");
        Assert.assertEquals("updated", notification.getTextSupport().get());
    }

    @Test
    public void bindText_null_removesBindingFromTextSupport() {
        notification = new Notification(textSignal);
        UI.getCurrent().add(notification);

        // Verify binding is active initially
        assertTextSignalBindingActive();

        // Remove binding
        notification.bindText(null);
        textSignal.value("updated");

        // Verify textSupport no longer has active binding
        Assert.assertEquals("bar", notification.getTextSupport().get());
        textSignal.value("should not update");
        Assert.assertEquals("bar", notification.getTextSupport().get());
    }

    private void assertTextSignalBindingActive() {
        textSignal.value("foo");
        Assert.assertEquals("foo", getNotificationText());
        textSignal.value("bar");
        Assert.assertEquals("bar", getNotificationText());
    }

    private void assertTextSignalBindingInactive() {
        var currentText = getNotificationText();
        textSignal.value(currentText + " with change");
        Assert.assertEquals(currentText, getNotificationText());
    }

    private String getNotificationText() {
        return notification.getElement().getProperty("text", "");
    }
}
