package com.vaadin.addon.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.addon.charts.model.AbstractConfigurationObject;
import com.vaadin.addon.charts.model.Axis;

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
