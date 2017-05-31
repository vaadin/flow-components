package com.vaadin.addon.board.testUI;

import static com.vaadin.addon.board.testUI.UIFunctions.testLayout;

import java.util.stream.Stream;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.HorizontalLayout;

/**
 *
 */

//Todo - video https://youtu.be/n3jXzy2EvaU
//DASH-116
public class CompatSpreadsheetUI extends AbstractTestUI {

  boolean simple = false;


  @Override
  protected void init(VaadinRequest vaadinRequest) {

    if(simple) {
      Spreadsheet sheet = nextElement();
      HorizontalLayout layout = new HorizontalLayout();
      layout.addComponent(sheet);
      layout.setSizeFull(); // Typically
      setContent(layout);
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
