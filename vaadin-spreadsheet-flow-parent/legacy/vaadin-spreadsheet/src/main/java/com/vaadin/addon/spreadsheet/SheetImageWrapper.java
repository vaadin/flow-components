package com.vaadin.addon.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.poi.ss.usermodel.ClientAnchor;

import com.vaadin.addon.spreadsheet.client.OverlayInfo;
import com.vaadin.addon.spreadsheet.client.OverlayInfo.Type;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;

/**
 * SheetImageWrapper is an utility class of the Spreadsheet component. In
 * addition to the image resource, this wrapper contains the images visibility
 * state, position and size.
 * 
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class SheetImageWrapper extends SheetOverlayWrapper implements
        Serializable {

    private StreamResource resource;

    private final byte[] data;
    private final String MIMEType;

    public SheetImageWrapper(ClientAnchor anchor, String MIMEType, byte[] data) {
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
    public Resource getResource() {
        if (resource == null) {
            StreamSource streamSource = new StreamSource() {
                @Override
                public InputStream getStream() {
                    return new ByteArrayInputStream(data);
                }
            };
            resource = new StreamResource(streamSource, getId());
            resource.setMIMEType(MIMEType);
        }

        return resource;
    }

    @Override
    public Type getType() {
        return OverlayInfo.Type.IMAGE;
    }
}
