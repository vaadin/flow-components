package com.vaadin.addon.spreadsheet.client;

import com.gargoylesoftware.htmlunit.javascript.host.event.EventTarget;
import com.google.gwt.core.client.JavaScriptObject;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.VWindow;

public class PopupButtonHeader extends Widget {

    protected final static String CLASSNAME = PopupButtonWidget.POPUP_OVERLAY_CLASSNAME
            + "-header";

    private DivElement root = Document.get().createDivElement();
    private DivElement close = Document.get().createDivElement();
    private DivElement caption = Document.get().createDivElement();
    private PopupPanel popup;
    private SheetWidget sheetWidget;

    public PopupButtonHeader() {
        root.setClassName(CLASSNAME);
        close.setClassName(VWindow.CLASSNAME + "-closebox");
        close.setAttribute("role", "button");
        caption.setClassName("header-caption");
        root.appendChild(close);
        root.appendChild(caption);
        Event.sinkEvents(close, Event.ONCLICK);
        Event.setEventListener(close, this);

        setElement(root);
    }

    public void setCaption(String caption) {
        this.caption.setInnerText(caption);
    }

    public void setPopup(PopupPanel popup) {
        this.popup = popup;
    }

    public void setSheet(SheetWidget sheetWidget) {
        this.sheetWidget = sheetWidget;
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (event.getEventTarget().equals((JavaScriptObject) close)) {
            popup.hide();
            sheetWidget.focusSheet();
        } else {
            super.onBrowserEvent(event);
        }
    }

    public void setHidden(boolean headerHidden) {
        getElement().getStyle()
                .setDisplay(headerHidden ? Display.NONE : Display.BLOCK);
    }

    public boolean isHidden() {
        return Display.NONE.getCssName()
                .equals(getElement().getStyle().getDisplay());
    }
}
