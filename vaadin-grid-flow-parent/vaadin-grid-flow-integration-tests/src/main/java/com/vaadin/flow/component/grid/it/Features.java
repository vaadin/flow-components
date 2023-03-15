package com.vaadin.flow.component.grid.it;

import java.util.Arrays;
import java.util.List;

/**
 * Collection class to check for if feature was requested.
 */
public class Features {

    private List<String> featureList;

    public Features(String... features) {
        featureList = Arrays.stream(features).toList();
    }

    /**
     * Check if feature is requested.
     *
     * @param feature
     *            feature to check
     * @return {@code true} if requested or all features enabled
     */
    public boolean hasFeature(String feature) {
        if (allFeatures()) {
            return true;
        }
        return featureList.contains(feature);
    }

    /**
     * Check if all features enabled.
     *
     * @return {@code true} if enabled
     */
    public boolean allFeatures() {
        return featureList.contains("mixed");
    }
}
