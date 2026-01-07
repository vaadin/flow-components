/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import com.vaadin.client.ui.Action;
import com.vaadin.client.ui.ActionOwner;

public class SpreadsheetAction extends Action {

    private SpreadsheetServerRpc rpc;

    private String actionKey = "";

    /** 0 = cell, 1 = row, 2 = column */
    private int type;

    private SpreadsheetWidget widget;

    private String iconContainerId;

    public SpreadsheetAction(ActionOwner owner) {
        super(owner);
    }

    public SpreadsheetAction(ActionOwner owner, SpreadsheetServerRpc rpc,
            String key, int type, SpreadsheetWidget widget) {
        this(owner);
        this.rpc = rpc;
        this.type = type;
        this.widget = widget;
        actionKey = key;
    }

    public void setIconContainerId(String iconContainerId) {
        this.iconContainerId = iconContainerId;
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

    /**
     * Overriding the {@link Action#getHTML()} to add a container for the icon
     * if present.
     */
    @Override
    public String getHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "<div style=\"display:flex; align-items: baseline; gap: 5px;\">");
        if (iconContainerId != null && !iconContainerId.isEmpty()) {
            sb.append("<div id=\"").append(iconContainerId)
                    .append("\" style=\"display:contents\"></div>");
        }
        sb.append(getCaption());
        sb.append("</div>");
        return sb.toString();
    }

}
