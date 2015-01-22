package com.vaadin.addon.spreadsheet.client;

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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;

public class SheetImage extends Widget {

    public static final String SHEET_IMAGE_CLASSNAME = "sheet-image";
    private final Element root = Document.get().createDivElement().cast();
    private final ImageElement image = Document.get().createImageElement();
    private String resourceURL;
    private String location;
    private int col;
    private int row;

    public SheetImage(String resourceURL) {
        this.resourceURL = resourceURL;

        initDOM();
        initListeners();
    }

    private void initDOM() {
        setElement(root);
        root.setClassName(SHEET_IMAGE_CLASSNAME);

        image.setSrc(resourceURL);
        root.appendChild(image);
    }

    private void initListeners() {
        Event.sinkEvents(root, Event.ONMOUSEDOWN | Event.ONCLICK
                | Event.ONDBLCLICK);
        Event.setEventListener(root, new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                // TODO Auto-generated method stub
                event.stopPropagation();
                event.preventDefault();
            }
        });
    }

    public void setLocation(int col, int row) {
        this.setCol(col);
        this.setRow(row);
        if (location != null) {
            root.removeClassName(location);
        }
        location = "col" + col + " row" + row;
        root.addClassName(location);
    }

    public void setWidth(float px) {
        root.getStyle().setWidth(px, Unit.PX);
        image.getStyle().setWidth(px, Unit.PX);
    }

    public void setHeight(float pt) {
        root.getStyle().setHeight(pt, Unit.PT);
        image.getStyle().setHeight(pt, Unit.PT);
    }

    public void setPadding(float x, float y) {
        root.getStyle().setPaddingLeft(x, Unit.PX);
        root.getStyle().setPaddingTop(y, Unit.PT);
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

}
