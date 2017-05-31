package com.vaadin.addon.board.testUI;

import static com.vaadin.addon.board.testUI.UIFunctions.testLayout;

import java.util.stream.Stream;

import com.vaadin.board.Board;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Tree;

/**
 *
 */
public class CompatTreeUI extends AbstractTestUI {

  boolean simple = false;

  //DASH-113
  @Override
  protected void init(VaadinRequest request) {

    if (simple) {
      Board board = new Board();
      board.setSizeFull();
      board.addRow(nextElement());
      setContent(board);
    } else {
      setContent(
          testLayout().apply(
              Stream.of(
                  nextElement(),
                  nextElement(),
                  nextElement())
          ));
    }
  }

  private Tree<String> nextElement() {
    Tree<String> tree = new Tree<>();
    tree.setSizeFull();
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
