package com.vaadin.addon.spreadsheet.client;

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
        root.getStyle().setMarginLeft(x, Unit.PX);
        root.getStyle().setMarginTop(y, Unit.PT);
    }

}
