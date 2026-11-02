/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer;

import java.util.List;

import com.vaadin.experimental.Feature;
import com.vaadin.experimental.FeatureFlagProvider;

/**
 * Provides the PDF Viewer component feature flag, gating the experimental
 * {@link PdfViewer} component.
 *
 * @author Vaadin Ltd
 * @since 25.4
 */
public class PdfViewerFeatureFlagProvider implements FeatureFlagProvider {

    /**
     * The id of the PDF Viewer component feature flag.
     */
    public static final String FEATURE_FLAG_ID = "pdfViewerComponent";

    /**
     * The PDF Viewer component feature flag. When enabled, allows use of the
     * experimental {@link PdfViewer} component.
     */
    public static final Feature PDF_VIEWER_COMPONENT = new Feature(
            "PDF Viewer component", // title
            FEATURE_FLAG_ID, // id
            "https://vaadin.com/docs/latest/components/pdf-viewer", // moreInfoLink
            true, // requiresServerRestart
            "com.vaadin.flow.component.pdfviewer.PdfViewer"); // componentClassName

    @Override
    public List<Feature> getFeatures() {
        return List.of(PDF_VIEWER_COMPONENT);
    }
}
