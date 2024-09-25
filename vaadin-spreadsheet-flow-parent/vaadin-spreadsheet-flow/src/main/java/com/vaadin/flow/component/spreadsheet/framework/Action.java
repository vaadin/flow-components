/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.framework;

import java.io.Serializable;

import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.shared.Registration;

/**
 * Implements the action framework. This class contains subinterfaces for action
 * handling and listing, and for action handler registrations and
 * unregistration.
 *
 * @author Vaadin Ltd.
 * @since 3.0
 */
@SuppressWarnings("serial")
public class Action implements Serializable {

    /**
     * Action title.
     */
    private String caption;

    /**
     * Action icon.
     */
    private Icon icon = null;

    /**
     * Constructs a new action with the given caption.
     *
     * @param caption
     *            the caption for the new action.
     */
    public Action(String caption) {
        this.caption = caption;
    }

    /**
     * Constructs a new action with the given caption string and icon.
     *
     * @param caption
     *            the caption for the new action.
     * @param icon
     *            the icon for the new action.
     */
    public Action(String caption, Icon icon) {
        this.caption = caption;
        this.icon = icon;
    }

    /**
     * Returns the action's caption.
     *
     * @return the action's caption as a <code>String</code>.
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Returns the action's icon.
     *
     * @return the action's Icon.
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * An Action that implements this interface can be added to an
     * Action.Notifier (or NotifierProxy) via the <code>addAction()</code>
     * -method, which in many cases is easier than implementing the
     * Action.Handler interface.
     *
     */
    @FunctionalInterface
    public interface Listener extends Serializable {
        public void handleAction(Object sender, Object target);
    }

    /**
     * Action.Containers implementing this support an easier way of adding
     * single Actions than the more involved Action.Handler. The added actions
     * must be Action.Listeners, thus handling the action themselves.
     *
     */
    public interface Notifier extends Container {
        public <T extends Action & Listener> void addAction(T action);

        public <T extends Action & Listener> void removeAction(T action);
    }

    public interface ShortcutNotifier extends Serializable {
        /**
         * Add a shortcut listener and return a registration object for
         * unregistering it.
         *
         * @param shortcut
         *            listener to add
         * @return registration for unregistering the listener
         * @since 8.0
         */
        public Registration addShortcutListener(ShortcutEventListener shortcut);

        /**
         * @deprecated As of 8.0, replaced by {@link Registration#remove()} in
         *             the registration object returned from
         *             {@link #addShortcutListener(ShortcutEventListener)}.
         */
        @Deprecated
        public void removeShortcutListener(ShortcutEventListener shortcut);
    }

    /**
     * Interface implemented by classes who wish to handle actions.
     *
     * @author Vaadin Ltd.
     * @since 3.0
     */
    public interface Handler extends Serializable {

        /**
         * Gets the list of actions applicable to this handler.
         *
         * @param target
         *            the target handler to list actions for. For item
         *            containers this is the item id.
         * @param sender
         *            the party that would be sending the actions. Most of this
         *            is the action container.
         * @return the list of Action
         */
        public Action[] getActions(Object target, Object sender);

        /**
         * Handles an action for the given target. The handler method may just
         * discard the action if it's not suitable.
         *
         * @param action
         *            the action to be handled.
         * @param sender
         *            the sender of the action. This is most often the action
         *            container.
         * @param target
         *            the target of the action. For item containers this is the
         *            item id.
         */
        public void handleAction(Action action, Object sender, Object target);
    }

    /**
     * Interface implemented by all components where actions can be registered.
     * This means that the components lets others to register as action handlers
     * to it. When the component receives an action targeting its contents it
     * should loop all action handlers registered to it and let them handle the
     * action.
     *
     * @author Vaadin Ltd.
     * @since 3.0
     */
    public interface Container extends Serializable {

        /**
         * Registers a new action handler for this container.
         *
         * @param actionHandler
         *            the new handler to be added.
         */
        public void addActionHandler(Handler actionHandler);

        /**
         * Removes a previously registered action handler for the contents of
         * this container.
         *
         * @param actionHandler
         *            the handler to be removed.
         */
        public void removeActionHandler(Handler actionHandler);
    }

    /**
     * Sets the caption.
     *
     * @param caption
     *            the caption to set.
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * Sets the icon. TODO: Marked private since the feature is not yet
     * implemented.
     *
     * @param icon
     *            the icon to set.
     */
    private void setIcon(Icon icon) {
        this.icon = icon;

    }

}
