/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.io.Serializable;

/**
 * Handler for intercepting item removal from the dashboard.
 * <p>
 * When a handler is set via {@link Dashboard#setItemRemoveHandler}, automatic
 * removal is disabled. The handler must explicitly call
 * {@link DashboardItemRemoveEvent#removeItem()} to proceed with removal.
 * <p>
 * Example usage with a confirmation dialog:
 *
 * <pre>
 * dashboard.setItemRemoveHandler(event -&gt; {
 *     ConfirmDialog dialog = new ConfirmDialog();
 *     dialog.setText("Remove this widget?");
 *     dialog.addConfirmListener(e -&gt; event.removeItem());
 *     dialog.open();
 * });
 * </pre>
 *
 * @author Vaadin Ltd.
 * @see Dashboard#setItemRemoveHandler(DashboardItemRemoveHandler)
 * @see DashboardItemRemoveEvent
 */
@FunctionalInterface
public interface DashboardItemRemoveHandler extends Serializable {

    /**
     * Called before an item is removed from the dashboard by user interaction.
     * <p>
     * When this handler is invoked, the removal has been prevented on the
     * client side. To proceed with the removal, call
     * {@link DashboardItemRemoveEvent#removeItem()}. To prevent removal, simply
     * do not call that method.
     *
     * @param event
     *            the event containing the item to be removed and its context
     */
    void onItemRemove(DashboardItemRemoveEvent event);
}
