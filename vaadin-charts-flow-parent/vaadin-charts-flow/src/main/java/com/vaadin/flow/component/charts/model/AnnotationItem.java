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

public class AnnotationItem extends AbstractConfigurationObject {

    private List<AnnotationItemLabel> labels;

    public List<AnnotationItemLabel> getLabels() {
        if (labels == null) {
            labels = new ArrayList<>();
        }
        return labels;
    }

    public void setLabels(List<AnnotationItemLabel> labels) {
        this.labels = labels;
    }

    public void addLabel(AnnotationItemLabel label) {
        getLabels().add(label);
    }
}
