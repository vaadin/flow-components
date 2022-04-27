package com.vaadin.component.spreadsheet.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.vaadin.client.ui.ui.UIConnector;

public class SpreadsheetConnectorBundleLoaderFactory
        extends ConnectorBundleLoaderFactory {

    // spreadsheet does not use any connector, but UIConnector is still needed
    private List<String> usedConnectors = Arrays
            .asList(UIConnector.class.getName());

    @Override
    protected Collection<JClassType> getConnectorsForWidgetset(
            TreeLogger logger, TypeOracle typeOracle)
            throws UnableToCompleteException {
        return super.getConnectorsForWidgetset(logger, typeOracle).stream()
                .filter(c -> usedConnectors
                        .contains(c.getQualifiedSourceName()))
                .collect(Collectors.toList());
    }
}
