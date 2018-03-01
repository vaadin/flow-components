/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;

/**
 * Unit tests for the Notification.
 */
public class NotificationTest {

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
}
