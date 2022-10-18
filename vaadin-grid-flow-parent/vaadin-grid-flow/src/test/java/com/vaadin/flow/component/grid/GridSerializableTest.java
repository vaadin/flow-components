package com.vaadin.flow.component.grid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.stream.Stream;

import org.junit.Test;

import com.vaadin.flow.component.html.Div;
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

    @Test
    public void gridWithoutColumnsIsSerializable()
            throws IOException, ClassNotFoundException {
        var grid = new Grid<String>();
        var layout = new Div(grid);
        grid.setItems("item");

        var bos = new ByteArrayOutputStream();
        var oos = new ObjectOutputStream(bos);
        oos.writeObject(layout);
        oos.flush();

        var bis = new ByteArrayInputStream(bos.toByteArray());
        var ois = new ObjectInputStream(bis);
        ois.readObject();
    }
}
