/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.internal.OverlayClassListProxy;
import com.vaadin.flow.dom.ClassList;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementDetachEvent;
import com.vaadin.flow.dom.ElementDetachListener;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.router.NavigationTrigger;
import com.vaadin.flow.shared.Registration;

/**
 * Notifications are used to provide feedback to the user. They communicate
 * information about activities, processes, and events in the application.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-notification")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha3")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/notification", version = "24.5.0-alpha3")
@JsModule("@vaadin/notification/src/vaadin-notification.js")
@JsModule("./flow-component-renderer.js")
public class Notification extends Component implements HasComponents, HasStyle,
        HasThemeVariant<NotificationVariant> {

    private static final int DEFAULT_DURATION = 5000;
    private static final Position DEFAULT_POSITION = Position.BOTTOM_START;
    private static final String OPENED_PROPERTY = "opened";
    private static final String OPENED_CHANGED_EVENT = "opened-changed";

    private boolean autoAddedToTheUi = false;

    private Registration afterProgrammaticNavigationListenerRegistration;

    /**
     * Enumeration of all available positions for notification component
     */
    public enum Position {
        TOP_STRETCH, TOP_START, TOP_CENTER, TOP_END, MIDDLE, BOTTOM_START, BOTTOM_CENTER, BOTTOM_END, BOTTOM_STRETCH;

        private final String clientName;

        Position() {
            this.clientName = name().toLowerCase(Locale.ENGLISH).replace('_',
                    '-');
        }

        /**
         * Gets name that is used in the client side representation of the
         * component.
         *
         * @return the name used in the client side representation of the
         *         component.
         */
        public String getClientName() {
            return clientName;
        }

        /**
         * Creates {@link Position} from the client side representation property
         * name
         *
         * @param clientName
         *            the client side representation of the property
         * @return corresponding {@link Position}
         */
        static Position fromClientName(String clientName) {
            return clientName == null ? null
                    : Position.valueOf(clientName.replace('-', '_')
                            .toUpperCase(Locale.ENGLISH));
        }
    }

    /**
     * Assigns a renderer function to the notification.
     *
     * If the Web Component has {@code text} property defined, it will be used
     * as the text content of the notification.
     *
     * Otherwise, the child nodes of this.container will be included in the
     * notification.
     */
    private void configureRenderer() {
        String appId = UI.getCurrent() != null
                ? UI.getCurrent().getInternals().getAppId()
                : "ROOT";

        //@formatter:off
        getElement().executeJs(
            "this.renderer = (root) => {" +
            "  if (this.text) {" +
            "    root.textContent = this.text;" +
            "  } else {" +
            "    Vaadin.FlowComponentHost.setChildNodes($0, this.virtualChildNodeIds, root)" +
            "  }" +
            "}", appId);
        //@formatter:on
    }

    /**
     * Default constructor. Create an empty notification with component support
     * and non-auto-closing
     * <p>
     * Note: To mix text and child components in notification that also supports
     * child components, use the {@link Text} component for the textual parts.
     */
    public Notification() {
        initBaseElementsAndListeners();
        setPosition(DEFAULT_POSITION);
        setDuration(0);
    }

    /**
     * Creates a Notification with the given String rendered as its HTML text,
     * that does not close automatically.
     *
     * @param text
     *            the text of the Notification
     */
    public Notification(String text) {
        this(text, 0, DEFAULT_POSITION);
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
        this(text, duration, DEFAULT_POSITION);
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
        initBaseElementsAndListeners();
        setText(text);
        setDuration(duration);
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

    private void initBaseElementsAndListeners() {
        getElement().addPropertyChangeListener(OPENED_PROPERTY,
                event -> fireEvent(
                        new OpenedChangeEvent(this, event.isUserOriginated())));

        getElement().addEventListener(OPENED_CHANGED_EVENT,
                event -> removeAutoAdded());
    }

    /**
     * Removes the notification from its parent if it was added automatically.
     */
    private void removeAutoAdded() {
        if (autoAddedToTheUi && !isOpened()) {
            autoAddedToTheUi = false;
            getElement().removeFromParent();
        }
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
     * @return the notification
     */
    public static Notification show(String text, int duration,
            Position position) {
        Notification notification = new Notification(text, duration, position);
        notification.open();
        return notification;
    }

    /**
     * Shows a notification in the current page with given text.
     * <p>
     * This is the convenience method for {@link #show(String, int, Position)}
     * which uses default web-component values for duration (which is 5000 ms)
     * and position ({@literal Position.BOTTOM_START}).
     *
     *
     * @param text
     *            the text of the Notification
     * @return the notification
     */
    public static Notification show(String text) {
        return show(text, DEFAULT_DURATION, DEFAULT_POSITION);
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
        this.getElement().setProperty("text", text);
        this.getElement().callJsFunction("requestContentUpdate");
    }

    /**
     * Set position of the notification.
     * <P>
     *
     * @param position
     *            the position of the notification. Valid enumerate values are
     *            {@code TOP_STRETCH, TOP_START, TOP_CENTER, TOP_END, MIDDLE, BOTTOM_START, BOTTOM_CENTER, BOTTOM_END, BOTTOM_STRETCH},
     *            not {@code null}
     */
    public void setPosition(Position position) {
        String positionName = position.getClientName();
        getElement().setProperty("position",
                positionName == null ? "" : positionName);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Alignment of the notification in the viewport Valid values are
     * {@code top-stretch|top-start|top-center|top-end|middle|bottom-start|bottom-center|bottom-end|bottom-stretch}
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     * <p>
     * The default position value is {@literal Position.BOTTOM_START}.
     *
     * @return the {@link Position} property from the webcomponent
     */
    public Position getPosition() {
        String position = getElement().getProperty("position");
        return Optional.ofNullable(position).map(Position::fromClientName)
                .orElse(DEFAULT_POSITION);
    }

    /**
     * Opens the notification.
     */
    public void open() {
        setOpened(true);
    }

    /**
     * Closes the notification.
     * <p>
     * Note: This method also removes the notification component from the DOM
     * after closing it, unless you have added the component manually.
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
    public void add(Collection<Component> components) {
        HasComponents.super.add(components);

        configureComponentRenderer();
    }

    /**
     * Adds the given component into this notification at the given index.
     * <p>
     * The element in the DOM will not be child of the
     * {@code <vaadin-notification>} element, but will be inserted into an
     * overlay that is attached into the {@code <body>}.
     * <p>
     * NOTE: When mixing this method with {@link #Notification(String)},
     * {@link #Notification(String, int)} and
     * {@link #Notification(String, int, Position)} method will remove the text
     * content.
     *
     * @param index
     *            the index, where the component will be added.
     * @param component
     *            the component to add
     */
    @Override
    public void addComponentAtIndex(int index, Component component) {
        HasComponents.super.addComponentAtIndex(index, component);

        configureComponentRenderer();
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
    public void setOpened(boolean opened) {
        UI ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException("UI instance is not available. "
                    + "It means that you are calling this method "
                    + "out of a normal workflow where it's always implicitly set. "
                    + "That may happen if you call the method from the custom thread without "
                    + "'UI::access' or from tests without proper initialization.");
        }
        StateTree.ExecutionRegistration addToUiRegistration = ui
                .beforeClientResponse(ui, context -> {
                    if (isOpened()
                            && getElement().getNode().getParent() == null) {
                        ui.addToModalComponent(this);
                        autoAddedToTheUi = true;
                    }
                    if (afterProgrammaticNavigationListenerRegistration != null) {
                        afterProgrammaticNavigationListenerRegistration
                                .remove();
                    }
                });
        if (ui.getSession() != null) {
            afterProgrammaticNavigationListenerRegistration = ui
                    .addAfterNavigationListener(event -> {
                        if (event.getLocationChangeEvent()
                                .getTrigger() == NavigationTrigger.PROGRAMMATIC) {
                            addToUiRegistration.remove();
                            afterProgrammaticNavigationListenerRegistration
                                    .remove();
                        }
                    });
        }
        getElement().setProperty(OPENED_PROPERTY, opened);
    }

    /**
     * True if the notification is currently displayed.
     * <p>
     * This property is synchronized automatically from client side when an
     * {@code opened-changed} event happens.
     *
     * @return the {@code opened} property from the webcomponent
     */
    @Synchronize(property = "opened", value = "opened-changed")
    public boolean isOpened() {
        return getElement().getProperty(OPENED_PROPERTY, false);
    }

    /**
     * {@code opened-changed} event is sent when the notification opened state
     * changes.
     */
    public static class OpenedChangeEvent extends ComponentEvent<Notification> {
        private final boolean opened;

        public OpenedChangeEvent(Notification source, boolean fromClient) {
            super(source, fromClient);
            this.opened = source.isOpened();
        }

        public boolean isOpened() {
            return opened;
        }
    }

    /**
     * Adds a listener for {@code opened-changed} events fired by the
     * webcomponent.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent> listener) {
        return addListener(OpenedChangeEvent.class, listener);
    }

    /**
     * The duration in milliseconds to show the notification. Set to {@code 0}
     * or a negative number to disable the notification auto-closing.
     *
     * @param duration
     *            the value to set
     */
    public void setDuration(int duration) {
        getElement().setProperty("duration", duration);
    }

    /**
     * <p>
     * The duration in milliseconds to show the notification. Set to {@code 0}
     * or a negative number to disable the notification auto-closing.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code duration} property from the webcomponent
     */
    public int getDuration() {
        return getElement().getProperty("duration", 0);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: To listen for opening the notification, you should use
     * {@link #addOpenedChangeListener(ComponentEventListener)}.
     */
    @Override
    public Registration addAttachListener(
            ComponentEventListener<AttachEvent> listener) {
        return super.addAttachListener(listener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: To listen for closing the notification, you should use
     * {@link #addOpenedChangeListener(ComponentEventListener)}, as the
     * component is not necessarily removed from the DOM when closing.
     */
    @Override
    public Registration addDetachListener(
            ComponentEventListener<DetachEvent> listener) {
        return super.addDetachListener(listener);
    }

    private void configureComponentRenderer() {
        this.getElement().removeProperty("text");
        updateVirtualChildNodeIds();
    }

    private Map<Element, Registration> childDetachListenerMap = new HashMap<>();
    // Must not use lambda here as that would break serialization. See
    // https://github.com/vaadin/flow-components/issues/5597
    private ElementDetachListener childDetachListener = new ElementDetachListener() {
        @Override
        public void onDetach(ElementDetachEvent e) {
            var child = e.getSource();
            var childDetachedFromContainer = !getElement().getChildren()
                    .anyMatch(containerChild -> Objects.equals(child,
                            containerChild));

            if (childDetachedFromContainer) {
                // The child was removed from the notification

                // Remove the registration for the child detach listener
                childDetachListenerMap.get(child).remove();
                childDetachListenerMap.remove(child);

                configureComponentRenderer();
            }
        }
    };

    /**
     * Updates the virtualChildNodeIds property of the notification element.
     * <p>
     * This method is called whenever the notification's child components
     * change.
     * <p>
     * Also calls {@code requestContentUpdate} on the notification element to
     * trigger the content update.
     */
    private void updateVirtualChildNodeIds() {
        // Add detach listeners (child may be removed with removeFromParent())
        getElement().getChildren().forEach(child -> {
            if (!childDetachListenerMap.containsKey(child)) {
                childDetachListenerMap.put(child,
                        child.addDetachListener(childDetachListener));
            }
        });

        this.getElement().setPropertyList("virtualChildNodeIds",
                getElement().getChildren()
                        .map(element -> element.getNode().getId())
                        .collect(Collectors.toList()));

        this.getElement().callJsFunction("requestContentUpdate");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        getElement().executeJs(
                "Vaadin.FlowComponentHost.patchVirtualContainer(this)");
        configureRenderer();
        updateVirtualChildNodeIds();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // When reloading a page using preserve on refresh, the notification
        // should keep its opened state. To prevent it from auto-closing, delay
        // the auto-closing logic to before client response, which is not called
        // when reloading the page. This also prevents an exception when trying
        // to remove an auto-added notification from its parent.
        detachEvent.getUI().beforeClientResponse(this, executionContext -> {
            // Close the notification, and remove it from its parent if it was
            // auto-added. This ensures that the notification doesn't re-open
            // itself when its parent, for example a dialog, gets attached
            // again.
            setOpened(false);
            removeAutoAdded();
        });
    }

    /**
     * Sets the CSS class names of the notification overlay element. This method
     * overwrites any previous set class names.
     *
     * @param className
     *            a space-separated string of class names to set, or
     *            <code>null</code> to remove all class names
     */
    @Override
    public void setClassName(String className) {
        getClassNames().clear();
        if (className != null) {
            addClassNames(className.split(" "));
        }
    }

    @Override
    public ClassList getClassNames() {
        return new OverlayClassListProxy(this);
    }

    /**
     * @throws UnsupportedOperationException
     *             Notification does not support adding styles to card element
     */
    @Override
    public Style getStyle() {
        throw new UnsupportedOperationException(
                "Notification does not support adding styles to card element");
    }
}
