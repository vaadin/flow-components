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
        } else if (name.contains(PushTestCase.class.getSimpleName())) {
            return PushTestCase.class;
        } else if (name.contains(TouchUI.class.getSimpleName())) {
            return TouchUI.class;
        } else if (name.contains(MultipleSpreadsheetUI.class.getSimpleName())) {
            return MultipleSpreadsheetUI.class;
        }
        return null;
    }
}
