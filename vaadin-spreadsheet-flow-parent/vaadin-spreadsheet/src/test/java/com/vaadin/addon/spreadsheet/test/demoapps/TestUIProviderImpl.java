package com.vaadin.addon.spreadsheet.test.demoapps;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

/**
 *
 */
@SuppressWarnings("serial")
public class TestUIProviderImpl extends UIProvider {

    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        String name = (event.getRequest()).getPathInfo();
        if (name.contains(TestexcelsheetUI.class.getSimpleName())) {
            return TestexcelsheetUI.class;
        } else if (name.contains(SpreadsheetDemoUI.class.getSimpleName())) {
            return SpreadsheetDemoUI.class;
        }
        return null;
    }
}
