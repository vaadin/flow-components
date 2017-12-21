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

/**
 * Server-side component for the <code>vaadin-notification</code> element.
 *
 * @author Vaadin Ltd
 */
@HtmlImport("frontend://flow-component-renderer.html")
public class Notification
        extends GeneratedVaadinNotification<Notification>
        implements HasComponents {

    private Element container;
    private final Element templateElement = new Element("template");
    /**
     * Enumeration of all available positions for Vertical Alignment
     */
    public enum VerticalAlign {
        TOP_STRETCH, TOP, MIDDLE, BOTTOM, BOTTOM_STRETCH
    }

    /**
     * Enumeration of all available positions for Horizontal Alignment
     */
    public enum HorizontalAlign {
        START, CENTER, END
    }

    /**
     * Default constructor. Create an empty notification with component support
     * and non-auto-closing
     * 
     * @see #Notification(Component...)
     */
    public Notification() {
        container = new Element("div", false);
        getElement().appendVirtualChild(container);
        getElement().getNode()
                .runWhenAttached(ui -> ui.beforeClientResponse(this,
                        () -> attachComponentTemplate(ui)));
        setAlignment(VerticalAlign.BOTTOM, HorizontalAlign.START);
        setDuration(0);
    }

    /**
     * Creates a Notification with the given String rendered as its HTML
     * content. The default duration for the notification is 4000 milliseconds
     * 
     * @param content
     *            the content of the Notification as HTML markup
     */
    public Notification(String content) {
        this(content, 4000, VerticalAlign.BOTTOM,
                HorizontalAlign.START);
    }

    /**
     * Creates a Notification with given String rendered as its HTML content and
     * given Integer rendered as its duration.
     * <p>
     * Set to {@code 0} or a negative number to disable the notification
     * auto-closing.
     * 
     * @param content
     *            the content of the Notification as HTML markup
     * @param duration
     *            the duration in milliseconds to show the notification
     */
    public Notification(String content, int duration) {
        this(content, duration, VerticalAlign.BOTTOM,
                HorizontalAlign.START);
    }

    /**
     * Creates a Notification with given content String, duration, vertical and
     * horizontal Alignment.
     * <P>
     * Set to {@code 0} or a negative number to disable the notification
     * auto-closing.
     * <P>
     * Horizontal alignment is skipped in case verticalAlign is set to
     * {@code top-stretch|middle|bottom-stretch}
     * 
     * @param content
     *            the content of the Notification as HTML markup
     * @param duration
     *            the duration in milliseconds to show the notification
     * @param vertical
     *            the vertical alignment of the notification. Valid values are
     *            {@code top-stretch|top|middle|bottom|bottom-stretch}
     * @param horizontal
     *            the horizontal alignment of the notification.Valid values are
     *            {@code start|center|end}
     */

    public Notification(String content, int duration, VerticalAlign vertical,
            HorizontalAlign horizontal) {
        getElement().appendChild(templateElement);
        setContent(content);
        setDuration((double) duration);
        setAlignment(vertical, horizontal);
    }

    /**
     * Creates a notification with given components inside.
     * <p>
     * Component support will NOT allow you to use setContent(String Content) in
     * the notification.
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
     * Set the content of the notification with given String
     * 
     * @param content
     */
    public void setContent(String content) {
        templateElement.setProperty("innerHTML", content);
    }

    /**
     * Set the vertical Alignment of the notification.
     * 
     * @param vertical
     *            the vertical alignment. Valid enumerate values are
     *            {@code top-stretch|top|middle|bottom|bottom-stretch}
     */
    public void setVerticalAlign(VerticalAlign vertical) {
        this.setVerticalAlign(
                vertical.toString().toLowerCase().replace('_', '-'));
    }

    /**
     * Set the horizontal Alignment of the notification.
     * <P>
     * Horizontal alignment is skipped in case verticalAlign is set to
     * {@code top-stretch|middle|bottom-stretch}
     * 
     * @param horizontal
     *            the horizontal alignment. Valid values are
     *            {@code start|center|end}
     */
    public void setHorizontalAlign(HorizontalAlign horizontal) {
        this.setHorizontalAlign(horizontal.toString().toLowerCase());
    }

    /**
     * Set vertical and Alignment of the notification.
     * <P>
     * Horizontal alignment is skipped in case verticalAlign is set to
     * {@code top-stretch|middle|bottom-stretch}
     * 
     * @param vertical
     *            the vertical alignment. Valid enumerate values are
     *            {@code top-stretch|top|middle|bottom|bottom-stretch}
     * @param horizontal
     *            the horizontal alignment. Valid values are
     *            {@code start|center|end}
     */
    public void setAlignment(VerticalAlign vertical,
            HorizontalAlign horizontal) {
        setVerticalAlign(vertical);
        setHorizontalAlign(horizontal);
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
        String template = "<template><flow-component-renderer appid=" + appId
                + " nodeid=" + nodeId
                + "></flow-component-renderer></template>";
        getElement().setProperty("innerHTML", template);
    }
}
