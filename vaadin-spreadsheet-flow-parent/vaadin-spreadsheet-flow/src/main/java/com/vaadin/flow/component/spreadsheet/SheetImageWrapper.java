/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import org.apache.poi.ss.usermodel.ClientAnchor;

import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo.Type;
import com.vaadin.flow.server.StreamResource;

/**
 * SheetImageWrapper is an utility class of the Spreadsheet component. In
 * addition to the image resource, this wrapper contains the images visibility
 * state, position and size.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class SheetImageWrapper extends SheetOverlayWrapper
        implements Serializable {

    private StreamResource resource;

    private final byte[] data;
    private final String MIMEType;

    public SheetImageWrapper(ClientAnchor anchor, String MIMEType,
            byte[] data) {
        super(anchor);
        this.MIMEType = MIMEType;
        this.data = data;
    }

    /**
     * Gets the resource containing this image
     *
     * @return Image resource
     */
    @Override
    public StreamResource getResource() {
        if (resource == null) {
            resource = new StreamResource(getId(),
                    () -> new ByteArrayInputStream(data));
            resource.setContentType(MIMEType);
        }

        return resource;
    }

    @Override
    public Type getType() {
        return OverlayInfo.Type.IMAGE;
    }
}
