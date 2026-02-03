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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * HasWidgets is an interface for components that can contain and manage
 * multiple {@link DashboardWidget} instances. It defines methods for adding,
 * removing, and accessing widgets within a component.
 *
 * @see DashboardWidget
 *
 * @author Vaadin Ltd
 */
public interface HasWidgets extends Serializable {

    /**
     * Returns the widgets in this component.
     *
     * @return The widgets in this component
     */
    List<DashboardWidget> getWidgets();

    /**
     * Adds the given widgets to this component.
     *
     * @param widgets
     *            the widgets to add, not {@code null}
     */
    default void add(DashboardWidget... widgets) {
        Objects.requireNonNull(widgets, "Widgets to add cannot be null.");
        add(Arrays.asList(widgets));
    }

    /**
     * Adds the given widgets to this component.
     *
     * @param widgets
     *            the widgets to add, not {@code null}
     */
    void add(Collection<DashboardWidget> widgets);

    /**
     * Adds the given widget as child of this component at the specific index.
     * <p>
     * In case the specified widget has already been added to another parent, it
     * will be removed from there and added to this one.
     *
     * @param index
     *            the index, where the widget will be added. The index must be
     *            non-negative and may not exceed the children count
     * @param widget
     *            the widget to add, not {@code null}
     */
    void addWidgetAtIndex(int index, DashboardWidget widget);

    /**
     * Adds the given widget immediately after the reference widget.
     * <p>
     * If the reference widget is located within a section, the new widget will
     * be added to the same section, positioned after the reference widget. If
     * the reference widget is at the root level of the dashboard, the new
     * widget will be added at the root level, positioned after the reference
     * widget.
     * <p>
     * In case the new widget has already been added to another parent, it will
     * be removed from there and added to the appropriate location.
     *
     * @param referenceWidget
     *            the widget after which the new widget will be added, not
     *            {@code null}
     * @param newWidget
     *            the widget to add, not {@code null}
     * @throws IllegalArgumentException
     *             if the reference widget is not found in this component or any
     *             of its sections
     */
    void addWidgetAfter(DashboardWidget referenceWidget,
            DashboardWidget newWidget);

    /**
     * Removes the given widgets from this component.
     *
     * @param widgets
     *            the widgets to remove, not {@code null}
     * @throws IllegalArgumentException
     *             if there is a widget whose non {@code null} parent is not
     *             this component
     */
    default void remove(DashboardWidget... widgets) {
        Objects.requireNonNull(widgets, "Widgets to remove cannot be null.");
        remove(Arrays.asList(widgets));
    }

    /**
     * Removes the given widgets from this component.
     *
     * @param widgets
     *            the widgets to remove, not {@code null}
     * @throws IllegalArgumentException
     *             if there is a widget whose non {@code null} parent is not
     *             this component
     */
    void remove(Collection<DashboardWidget> widgets);

    /**
     * Removes all widgets from this component.
     */
    void removeAll();
}
