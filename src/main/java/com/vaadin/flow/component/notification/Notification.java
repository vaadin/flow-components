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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.HtmlUtils;

/**
 * Server-side component for the <code>vaadin-notification</code> element.
 *
 * @author Vaadin Ltd
 */
@HtmlImport("frontend://flow-component-renderer.html")
public class Notification
        extends GeneratedVaadinNotification<Notification>
        implements HasComponents {

    private Element container = new Element("div", false);
    private final Element templateElement = new Element("template");

    /**
     * Enumeration of all available positions for notification component
     */
    public enum Position {
        TOP_STRETCH, TOP_START, TOP_CENTER, TOP_END, MIDDLE, BOTTOM_START, BOTTOM_CENTER, BOTTOM_END, BOTTOM_STRETCH
    }

    /**
     * Default constructor. Create an empty notification with component support
     * and non-auto-closing
     * <p>
     * Note: To mix text and child components in notification that also supports
     * child components, use the {@link Text} component for the textual parts.
     */
    public Notification() {
        getElement().appendChild(templateElement);
        getElement().appendVirtualChild(container);
        getElement().getNode()
                .runWhenAttached(ui -> ui.beforeClientResponse(this,
                        () -> attachComponentTemplate(ui)));
        setPosition(Position.BOTTOM_START);
        setDuration(0);
    }

    /**
     * Creates a Notification with the given String rendered as its HTML text.
     * The default duration for the notification is 4000 milliseconds
     * 
     * @param text
     *            the text of the Notification
     */
    public Notification(String text) {
        this(text, 4000, Position.BOTTOM_START);
    }

    /**
     * Creates a Notification with given String rendered as its HTML text and
     * given Integer rendered as its duration.
     * <p>
     * Set to {@code 0} or a negative number to disable the notification
     * auto-closing.
     * 
     * @param text
     *            the text of the Notification
     * @param duration
     *            the duration in milliseconds to show the notification
     */
    public Notification(String text, int duration) {
        this(text, duration, Position.BOTTOM_START);
    }

    /**
     * Creates a Notification with given text String, duration and position
     * <P>
     * Set to {@code 0} or a negative number to disable the notification
     * auto-closing.
     * 
     * @param text
     *            the text of the notification
     * @param duration
     *            the duration in milliseconds to show the notification
     * @param position
     *            the position of the notification. Valid enumerate values are
     *            TOP_STRETCH, TOP_START, TOP_CENTER, TOP_END, MIDDLE,
     *            BOTTOM_START, BOTTOM_CENTER, BOTTOM_END, BOTTOM_STRETCH
     */
    public Notification(String text, int duration, Position position) {
        getElement().appendChild(templateElement);
        getElement().appendVirtualChild(container);
        setText(text);
        setDuration((double) duration);
        setPosition(position);
    }

    /**
     * Creates a notification with given components inside.
     * <p>
     * Note: To mix text and child components in a component that also supports
     * child components, use the {@link Text} component for the textual parts.
     * 
     * @param components
     *            the components inside the notification
     * @see #add(Component...)
     */
    public Notification(Component... components) {
        this();
        add(components);
    }

    /**
     * Shows a notification in the current page with given text, duration and
     * position.
     * 
     * @param text
     *            the text of the Notification
     * @param duration
     *            the duration in milliseconds to show the notification
     * @param position
     *            the position of the notification. Valid enumerate values are
     *            TOP_STRETCH, TOP_START, TOP_CENTER, TOP_END, MIDDLE,
     *            BOTTOM_START, BOTTOM_CENTER, BOTTOM_END, BOTTOM_STRETCH
     * @return The notification
     */
    public static Notification show(String text, int duration,
            Position position) {
        Notification notification = new Notification(text, duration, position);
        notification.open();
        notification.addOpenedChangeListener(
                event -> {
                    if (notification.isOpened() == false) {
                        notification.getElement().removeFromParent();
                    }
                });
        return notification;
    }

    /**
     * Set the text of the notification with given String
     * <p>
     * NOTE: When mixing this method with {@link #Notification()} and
     * {@link #Notification(Component...)}. Method will remove all the
     * components from the notification.
     * 
     * @param text
     *            the text of the Notification
     */
    public void setText(String text) {
        removeAll();
        getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(this, () -> templateElement
                        .setProperty("innerHTML", HtmlUtils.escape(text))));
    }

    /**
     * Set position of the notification.
     * <P>
     * 
     * @param position
     *            the position of the notification. Valid enumerate values are
     *            {@code TOP_STRETCH, TOP_START, TOP_CENTER, TOP_END, MIDDLE, BOTTOM_START, BOTTOM_CENTER, BOTTOM_END, BOTTOM_STRETCH}
     */
    public void setPosition(Position position) {
        this.setPosition(position.toString().toLowerCase().replace('_', '-'));
    }

    /**
     * Opens the notification.
     */
    public void open() {
        setOpened(true);
    }

    /**
     * Closes the notification.
     */
    public void close() {
        setOpened(false);
    }

    /**
     * Adds the given components into this notification.
     * <p>
     * The elements in the DOM will not be children of the
     * {@code <vaadin-notification>} element, but will be inserted into an
     * overlay that is attached into the {@code <body>}.
     * <p>
     * NOTE: When mixing this method with {@link #Notification(String)},
     * {@link #Notification(String, int)} and
     * {@link #Notification(String, int, Position)} method will remove the text
     * content.
     *
     * @param components
     *            the components to add
     */
    @Override
    public void add(Component... components) {
        assert components != null;
        for (Component component : components) {
            assert component != null;
            container.appendChild(component.getElement());
        }
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, () -> attachComponentTemplate(ui)));
    }

    /**
     * Remove the given components from this notification.
     * 
     * @param components
     *            the components to remove
     */
    @Override
    public void remove(Component... components) {
        for (Component component : components) {
            assert component != null;
            if (container.equals(component.getElement().getParent())) {
                container.removeChild(component.getElement());
            } else {
                throw new IllegalArgumentException("The given component ("
                        + component + ") is not a child of this component");
            }
        }
    }

    /**
     * Remove all the components from this notification.
     */
    @Override
    public void removeAll() {
        container.removeAllChildren();
    }

    private void attachComponentTemplate(UI ui) {
        String appId = ui.getInternals().getAppId();
        int nodeId = container.getNode().getId();
        String template = "<flow-component-renderer appid=" + appId
                + " nodeid=" + nodeId
                + "></flow-component-renderer>";
        templateElement.setProperty("innerHTML", template);
    }

    /**
     * Opens or closes the notification.
     * <p>
     * Note: You don't need to add the component anywhere before opening it.
     * Since {@code <vaadin-notification>}'s location in the DOM doesn't really
     * matter, opening a notification will automatically add it to the
     * {@code <body>} if it's not yet attached anywhere.
     * 
     * @param opened
     *            {@code true} to open the notification, {@code false} to close
     *            it
     */
    @Override
    public void setOpened(boolean opened) {
        if (opened && !getElement().getNode().isAttached()
                && UI.getCurrent() != null) {
            UI.getCurrent().add(this);
        }
        super.setOpened(opened);
    }
}
