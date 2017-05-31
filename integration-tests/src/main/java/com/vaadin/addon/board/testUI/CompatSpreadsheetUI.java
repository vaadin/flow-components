package com.vaadin.addon.board.testUI;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.ui.Component;

/**
 *
 */

//Todo - video https://youtu.be/n3jXzy2EvaU
//DASH-116
public class CompatSpreadsheetUI extends AbstractTestCompUI {

  @Override
  protected Component[] createTestedComponents() {
    Component[] comps = { nextElement(), nextElement(), nextElement() };
    return comps;
  }

  private Spreadsheet nextElement() {
    Spreadsheet sheet = new Spreadsheet();
    sheet.createCell(0, 0, "Hello, world");
    sheet.createCell(1, 0, 6);
    sheet.createCell(1, 1, 7);
    sheet.createCell(1, 2, ""); // Set a dummy value
    sheet.getCell(1, 2).setCellFormula("A2*B2");
    sheet.autofitColumn(0);
    sheet.setSizeFull();
    return sheet;
  }

}
