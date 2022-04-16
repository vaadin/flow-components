package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Alternative AxisContainer to allow multiple axes
 */
public class AxisList<T extends Axis> extends AbstractConfigurationObject {

    private List<T> axesList = new ArrayList<>();

    /**
     * @return the number of axes in the list
     */
    public int getNumberOfAxes() {
        return axesList.size();
    }

    /**
     * Finds the axis at the given index
     *
     * @param index
     *            The index of the axis
     * @return The axis at the given index
     */
    public T getAxis(int index) {
        return axesList.get(index);
    }

    /**
     * @return The list of axes. Used only for serialization.
     */
    public List<T> getAxes() {
        return axesList;
    }

    /**
     * Adds a new axis to the list
     *
     * @param axis
     *            The axis to add
     */
    public void addAxis(T axis) {
        axesList.add(axis);
        updateIndexes();
    }

    private void updateIndexes() {
        for (int i = 0; i < axesList.size(); i++) {
            Axis axis = axesList.get(i);
            axis.setAxisIndex(i);
        }
    }

    /**
     * Removes an axis from the list
     *
     * @param axis
     *            The axis to remove
     */
    public void removeAxis(T axis) {
        axesList.remove(axis);
        updateIndexes();
    }

    public int indexOf(Axis axis) {
        return axesList.indexOf(axis);
    }

    public boolean contains(Axis axis) {
        return axesList.contains(axis);
    }
}
