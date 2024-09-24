/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.io.Serializable;
import java.util.List;

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
    void add(DashboardWidget... widgets);

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
     * Removes the given widgets from this component.
     *
     * @param widgets
     *            the widgets to remove, not {@code null}
     * @throws IllegalArgumentException
     *             if there is a widget whose non {@code null} parent is not
     *             this component
     */
    void remove(DashboardWidget... widgets);

    /**
     * Removes all widgets from this component.
     */
    void removeAll();
}