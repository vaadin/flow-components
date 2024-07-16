/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.stream.Stream;

import org.junit.Test;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.testutil.ClassesSerializableTest;

public class GridSerializableTest extends ClassesSerializableTest {
    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                "com\\.vaadin\\.flow\\.component\\.grid\\.it\\..*",
                "com\\.vaadin\\.flow\\.component\\.contextmenu\\.osgi\\..*",
                "com\\.vaadin\\.flow\\.component\\.treegrid\\.it\\..*",
                "com\\.vaadin\\.flow\\.component\\.datepicker\\..*",
                "com\\.vaadin\\.flow\\.component\\.grid\\.GridColumnOrderHelper.*"));
    }

    @Test
    public void treeGridWithHierarchyColumnIsSerializable() throws IOException {
        final TreeGrid<String> grid = new TreeGrid<>();
        grid.addHierarchyColumn(String::toString);
        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(grid);
    }
}
