package com.vaadin.spreadsheet.flowport.gwtexporter;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.vaadin.addon.spreadsheet.client.PopupButtonConnector;
import com.vaadin.addon.spreadsheet.client.SpreadsheetConnector;
import com.vaadin.client.extensions.javascriptmanager.JavaScriptManagerConnector;
import com.vaadin.client.ui.ui.UIConnector;
import com.vaadin.shared.ui.Connect;

public class OptimizedConnectorBundleLoaderFactory extends ConnectorBundleLoaderFactory {

    private Set<String> eagerConnectors = new HashSet<>();
    {
        eagerConnectors.add(UIConnector.class.getName());
        eagerConnectors.add(SpreadsheetConnector.class.getName());
        eagerConnectors.add(JavaScriptManagerConnector.class.getName());
        eagerConnectors.add(JavaScriptManagerConnector.class.getName());
        eagerConnectors.add(PopupButtonConnector.class.getName());
    }

    @Override
    protected Connect.LoadStyle getLoadStyle(JClassType connectorType) {
        if (eagerConnectors.contains(connectorType.getQualifiedBinaryName())) {
            return Connect.LoadStyle.EAGER;
        } else {
            return Connect.LoadStyle.DEFERRED;
        }
    }
}
