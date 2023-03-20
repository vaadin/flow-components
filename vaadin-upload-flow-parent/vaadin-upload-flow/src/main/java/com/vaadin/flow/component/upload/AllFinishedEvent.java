/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.upload;

import com.vaadin.flow.component.ComponentEvent;

/**
 * AllFinishedEvent is sent when the upload has processed all the files in its
 * upload queue, regardless of whether all the receptions were successful or
 * not.
 *
 * @author Vaadin Ltd.
 */
public class AllFinishedEvent extends ComponentEvent<Upload> {

    /**
     * Create an instance of the event.
     *
     * @param source
     *            the source of the file
     */
    public AllFinishedEvent(Upload source) {
        super(source, false);
    }

}
