package com.vaadin.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vaadin.annotations.HtmlImport;
import com.vaadin.board.client.BoardState;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

@HtmlImport("frontend://vaadin-board/vaadin-board.html")
@HtmlImport("frontend://vaadin-board/vaadin-board-row.html")
public class Board extends AbstractComponent implements HasComponents {

  private final List<Row> rows = new ArrayList<>();

  /**
   * Creates an empty board.
   * <p>
   * Use #addRow(Row) to add content to the board.
   **/
  public Board() {
  }

  @Override
  public BoardState getState() {
    return (BoardState) super.getState();
  }

  /**
   * Creates a new row and adds the given components to the row.
   * <p>
   * All the added components have cols set to 1, i.e. use one slot in the
   * row. The number of slots in the row is the number of added components.
   *
   * @param components components to add, no more than 4
   * @throws IllegalArgumentException if there are more than 4 components
   * @returns a row instance which can be used for further configuration
   **/
  public Row addRow(Component... components) {
    Row row = new Row();
    rows.add(row);
    row.setParent(this);

    row.addComponents(components);
    markAsDirty();
    return row;
  }

  /**
   * Removes the given row from the board.
   *
   * @param row to be removed
   **/
  public void removeRow(Row row) {
    if (rows.remove(row)) {
      row.setParent(null);
      markAsDirty();
    }
  }

  @Override
  public Iterator<Component> iterator() {
    return Collections.<Component>unmodifiableCollection(rows).iterator();
  }

}
