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
import java.util.Optional;

import com.vaadin.flow.component.Component;

/**
 * Event object passed to {@link DashboardItemRemoveHandler} when a user
 * attempts to remove an item from the dashboard.
 * <p>
 * This event provides access to the item being removed, its parent section (if
 * any), and a method to proceed with the removal.
 *
 * @author Vaadin Ltd.
 * @see DashboardItemRemoveHandler
 * @see Dashboard#setItemRemoveHandler(DashboardItemRemoveHandler)
 */
public class DashboardItemRemoveEvent implements Serializable {

    private final Dashboard dashboard;
    private final Component item;
    private final DashboardSection section;

    /**
     * Creates a new item remove event.
     *
     * @param dashboard
     *            the dashboard containing the item
     * @param item
     *            the item being removed (widget or section)
     * @param section
     *            the parent section if the item is inside a section, or
     *            {@code null} if it's at the root level
     */
    DashboardItemRemoveEvent(Dashboard dashboard, Component item,
            DashboardSection section) {
        this.dashboard = dashboard;
        this.item = item;
        this.section = section;
    }

    /**
     * Returns the item being removed.
     *
     * @return the item being removed, either a {@link DashboardWidget} or a
     *         {@link DashboardSection}
     */
    public Component getItem() {
        return item;
    }

    /**
     * Returns the parent section if the item is inside a section.
     *
     * @return an {@link Optional} containing the parent section, or empty if
     *         the item is at the root level
     */
    public Optional<DashboardSection> getSection() {
        return Optional.ofNullable(section);
    }

    /**
     * Removes the item from the dashboard.
     * <p>
     * Call this method to proceed with removal after validation or user
     * confirmation. If this method is not called, the item will remain in the
     * dashboard.
     */
    public void removeItem() {
        dashboard.performRemoval(item);
    }
}
