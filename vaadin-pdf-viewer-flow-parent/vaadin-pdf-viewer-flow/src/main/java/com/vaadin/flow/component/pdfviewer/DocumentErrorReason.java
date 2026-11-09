/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer;

/**
 * The reasons why {@link PdfViewer} could not load a document, see
 * {@link PdfViewer.DocumentErrorEvent}.
 *
 * @author Vaadin Ltd
 * @since 25.4
 */
public enum DocumentErrorReason {

    /**
     * The document could not be fetched, for example because the URL does not
     * exist or the server responded with an error.
     */
    NETWORK("network"),

    /**
     * The data is not a valid PDF document.
     */
    INVALID("invalid"),

    /**
     * The document needs a password to open. Password-protected documents are
     * not supported.
     */
    PASSWORD("password");

    private final String clientValue;

    DocumentErrorReason(String clientValue) {
        this.clientValue = clientValue;
    }

    /**
     * Gets the reason that the client reports with the given value.
     *
     * @param clientValue
     *            the value reported by the client
     * @return the reason, or {@code null} if no reason has the value
     */
    static DocumentErrorReason fromClientValue(String clientValue) {
        for (DocumentErrorReason reason : values()) {
            if (reason.clientValue.equals(clientValue)) {
                return reason;
            }
        }
        return null;
    }
}
