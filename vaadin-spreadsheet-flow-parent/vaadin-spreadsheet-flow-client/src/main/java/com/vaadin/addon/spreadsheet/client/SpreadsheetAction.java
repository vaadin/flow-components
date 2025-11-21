/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.Element;
import com.vaadin.client.ui.Action;
import com.vaadin.client.ui.ActionOwner;

public class SpreadsheetAction extends Action {

    private SpreadsheetServerRpc rpc;

    private String actionKey = "";

    /** 0 = cell, 1 = row, 2 = column */
    private int type;

    private SpreadsheetWidget widget;

    private Element iconElement;

    public SpreadsheetAction(ActionOwner owner) {
        super(owner);
    }

    public SpreadsheetAction(ActionOwner owner, SpreadsheetServerRpc rpc,
            String key, int type, SpreadsheetWidget widget,
            Element iconElement) {
        this(owner);
        this.rpc = rpc;
        this.type = type;
        this.widget = widget;
        actionKey = key;
        this.iconElement = iconElement;
    }

    @Override
    public void execute() {
        if (type == 0) {
            rpc.actionOnCurrentSelection(actionKey);
        } else if (type == 1) {
            rpc.actionOnRowHeader(actionKey);
        } else {
            rpc.actionOnColumnHeader(actionKey);
        }
        owner.getClient().getContextMenu().hide();
        widget.focusSheet();
    }

    @Override
    public String getHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        boolean iconRendered = false;
        if (iconElement != null) {
            sb.append(getOuterHTML(iconElement));
            iconRendered = true;
        }
        if (!iconRendered) {
            // Fallback to legacy iconUrl mechanism if present, else no icon
            if (getIconUrl() != null) {
                return super.getHTML();
            }
        }
        sb.append(getCaption());
        sb.append("</div>");
        return sb.toString();
    }

    private native String getOuterHTML(Element element) /*-{
        return element.outerHTML;
    }-*/;

}
