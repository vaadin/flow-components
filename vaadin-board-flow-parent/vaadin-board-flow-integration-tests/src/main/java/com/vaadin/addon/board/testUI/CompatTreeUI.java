package com.vaadin.addon.board.testUI;

import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;

/**
 *
 */
public class CompatTreeUI extends AbstractTestCompUI {

  @Override
  protected Component[] createTestedComponents() {
    Component[] comps = { nextElement(), nextElement(), nextElement() };
    return comps;
  }

  static Tree<String> nextElement() {
    Tree<String> tree = new Tree<>();
    tree.setHeight("undefined");
    tree.setWidth("100%");
    TreeData<String> treeData = new TreeData<>();

    // Couple of childless root items
    treeData.addItem(null, "Mercury");
    treeData.addItem(null, "Venus");

    // Items with hierarchy
    treeData.addItem(null, "Earth");
    treeData.addItem("Earth", "The Moon");

    TreeDataProvider inMemoryDataProvider = new TreeDataProvider<>(treeData);
    tree.setDataProvider(inMemoryDataProvider);
    tree.expand("Earth"); // Expand programmatically
    return tree;
  }
}
