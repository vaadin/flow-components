
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.dom.Element;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Unit tests for the Notification.
 */
@NotThreadSafe
public class NotificationTest {

    private UI ui = new UI();

    @Before
    public void setUp() {
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

    @Test
    public void templateWarningSuppressed() {
        Notification notification = new Notification();

        Assert.assertTrue("Template warning is not suppressed", notification
                .getElement().hasAttribute("suppress-template-warning"));
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
    public void setText_notificationHasAddedComponents_innerHtmlIsTextValue() {
        Notification notification = new Notification();

        notification.add(new Div());
        notification.setText("foo");

        notification.open();

        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();

        Element templateElement = notification.getElement().getChildren()
                .findFirst().get();

        String innerHtml = templateElement.getProperty("innerHTML");
        Assert.assertEquals("foo", innerHtml);
    }

    @Test
    public void add_notificationHasText_innerHtmlIsTemplateValue() {
        Notification notification = new Notification();

        notification.setText("foo");
        notification.add(new Div());

        notification.open();

        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();

        Element templateElement = notification.getElement().getChildren()
                .findFirst().get();

        String innerHtml = templateElement.getProperty("innerHTML");
        Assert.assertThat(innerHtml,
                CoreMatchers.startsWith("<flow-component-renderer"));
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
}
