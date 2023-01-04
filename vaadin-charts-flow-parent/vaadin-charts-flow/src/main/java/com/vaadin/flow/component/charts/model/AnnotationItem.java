/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for labels on the chart
 */
public class AnnotationItem extends AbstractConfigurationObject {

    private List<AnnotationItemLabel> labels;

    /**
     * @see #setLabels(AnnotationItemLabel...)
     * @return Labels
     */
    public List<AnnotationItemLabel> getLabels() {
        if (labels == null) {
            labels = new ArrayList<>();
        }
        return labels;
    }

    /**
     * Sets labels that can be positioned anywhere in the chart area.
     *
     * @param labels
     */
    public void setLabels(AnnotationItemLabel... labels) {
        clearLabels();
        addLabels(labels);
    }

    /**
     * Adds multiple labels
     *
     * @see #setLabels(AnnotationItemLabel...)
     * @param labels
     */
    public void addLabels(AnnotationItemLabel... labels) {
        for (AnnotationItemLabel label : labels) {
            addLabel(label);
        }
    }

    /**
     * Adds a single label
     *
     * @see #setLabels(AnnotationItemLabel...)
     * @param label
     */
    public void addLabel(AnnotationItemLabel label) {
        getLabels().add(label);
    }

    /**
     * Clears all labels
     *
     * @see #setLabels(AnnotationItemLabel...)
     */
    public void clearLabels() {
        getLabels().clear();
    }
}
