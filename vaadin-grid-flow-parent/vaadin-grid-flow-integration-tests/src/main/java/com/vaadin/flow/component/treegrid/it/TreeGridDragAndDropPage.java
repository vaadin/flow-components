package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route("vaadin-grid/treegrid-drag-and-drop")
@JavaScript("DragAndDropHelpers.js")
public class TreeGridDragAndDropPage extends Div {
    private final TreeData<String> treeData;
    private final TreeDataProvider<String> treeDataProvider;
    private final List<String> draggedNodes = new ArrayList<>();

    public TreeGridDragAndDropPage() {
        TreeGrid<String> treeGrid = new TreeGrid<>();
        treeGrid.addHierarchyColumn(item -> item);
        treeGrid.setRowsDraggable(true);
        treeGrid.setDropMode(GridDropMode.ON_TOP);

        treeGrid.addDragStartListener(event -> {
            draggedNodes.clear();
            draggedNodes.addAll(event.getDraggedItems());
        });

        treeGrid.addDragEndListener(event -> draggedNodes.clear());

        treeGrid.addDropListener(this::drop);

        treeData = createTreeData();
        treeDataProvider = new TreeDataProvider<>(treeData);
        treeGrid.setDataProvider(treeDataProvider);
        treeGrid.expandRecursively(List.of("root"), 2);

        add(treeGrid);
    }

    private void drop(GridDropEvent<String> event) {
        Optional<String> dragged = draggedNodes.stream().findFirst();
        Optional<String> dropped = event.getDropTargetItem();

        if (dragged.isPresent() && dropped.isPresent()) {
            String draggedNode = dragged.get();
            String droppedNode = dropped.get();

            String previousParent = treeData.getParent(draggedNode);

            treeData.removeItem(draggedNode);
            treeData.addItem(droppedNode, draggedNode);

            treeDataProvider.refreshItem(previousParent, true);
            treeDataProvider.refreshItem(droppedNode, true);
        }
    }

    private TreeData<String> createTreeData() {
        TreeData<String> treeData = new TreeData<>();

        treeData.addRootItems("root");
        treeData.addItem("root", "item 1");
        treeData.addItem("root", "item 2");
        treeData.addItem("item 1", "item 1-1");
        treeData.addItem("item 2", "item 2-1");

        return treeData;
    }
}
