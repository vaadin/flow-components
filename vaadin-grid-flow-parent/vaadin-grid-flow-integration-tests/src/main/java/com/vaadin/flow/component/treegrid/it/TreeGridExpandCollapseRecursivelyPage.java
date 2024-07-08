/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-expand-collapse-recursively")
public class TreeGridExpandCollapseRecursivelyPage extends Div {

    private static class Directory {

        private String name;
        private Directory parent;
        private List<Directory> subDirectories = new ArrayList<>();

        public Directory(String name, Directory parent) {
            this.name = name;
            this.parent = parent;
        }

        public String getName() {
            return name;
        }

        @SuppressWarnings("unused")
        public void setName(String name) {
            this.name = name;
        }

        @SuppressWarnings("unused")
        public Directory getParent() {
            return parent;
        }

        @SuppressWarnings("unused")
        public void setParent(Directory parent) {
            this.parent = parent;
        }

        public List<Directory> getSubDirectories() {
            return subDirectories;
        }

        @SuppressWarnings("unused")
        public void setSubDirectories(List<Directory> subDirectories) {
            this.subDirectories = subDirectories;
        }
    }

    private static final int DEPTH = 4;
    private static final int CHILDREN = 5;

    public TreeGridExpandCollapseRecursivelyPage() {

        Collection<Directory> roots = generateDirectoryStructure(DEPTH);

        TreeGrid<Directory> grid = new TreeGrid<>();
        grid.addHierarchyColumn(item -> "Item" + item.getName());

        grid.setItems(roots, Directory::getSubDirectories);

        RadioButtonGroup<Integer> depthSelector = new RadioButtonGroup<>();
        depthSelector.setItems(0, 1, 2, 3);
        depthSelector.getElement().getStyle().set("display", "flex");
        depthSelector.getElement().getStyle().set("flexDirection", "row");
        depthSelector.setValue(3);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.add(new NativeButton("Expand recursively",
                e -> grid.expandRecursively(roots, depthSelector.getValue())));
        buttons.add(new NativeButton("Collapse recursively", e -> grid
                .collapseRecursively(roots, depthSelector.getValue())));

        add(depthSelector, buttons, grid);
    }

    private Collection<Directory> generateDirectoryStructure(int depth) {
        return generateDirectories(depth, null, CHILDREN);
    }

    private Collection<Directory> generateDirectories(int depth,
            Directory parent, int childCount) {
        Collection<Directory> dirs = new ArrayList<>();
        if (depth >= 0) {
            for (int i = 0; i < childCount; i++) {
                String name = parent != null ? parent.getName() + "-" + i
                        : "-" + i;
                Directory dir = new Directory(name, parent);
                if (parent != null) {
                    parent.getSubDirectories().add(dir);
                }
                dirs.add(dir);

                generateDirectories(depth - 1, dir, childCount);
            }
        }
        return dirs;
    }
}
