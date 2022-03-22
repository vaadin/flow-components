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

import com.vaadin.client.ui.Action;
import com.vaadin.client.ui.ActionOwner;

public class SpreadsheetAction extends Action {

    private SpreadsheetServerRpc rpc;

    private String actionKey = "";

    /** 0 = cell, 1 = row, 2 = column */
    private int type;

    private SpreadsheetWidget widget;

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

}
