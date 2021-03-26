package com.example.application.views.helloworld;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

import com.example.application.views.main.MainView;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.flow.router.RouteAlias;

@CssImport("./views/helloworld/hello-world-view.css")
@Route(value = "hello", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Hello World")
public class HelloWorldView extends VerticalLayout {

    private final Spreadsheet spreadsheet;

    private TextField name;
    private Button updateName;
    private TextField updateSharedStateParam;
    private Button updateSharedState;
    private TextField alertParam;
    private Button callAlert;

    public HelloWorldView() {
        addClassName("hello-world-view");

        //add(new MyElement2());

        add(spreadsheet = new Spreadsheet());
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Hoja 1");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Hola!");
        spreadsheet.setWorkbook(wb);

        add(new H1("-"));

        add(new H1("These are usual flow server side components"));

        HorizontalLayout l;
        add(l =new HorizontalLayout());
        name = new TextField("Value");
        updateName = new Button("Update cell 0,0 value");
        l.add(name, updateName);
        l.setVerticalComponentAlignment(Alignment.END, name, updateName);
        updateName.addClickListener(e -> {
            Notification.show("Hello " + name.getValue());
            wb.getSheetAt(0).getRow(0).getCell(0).setCellValue(name.getValue());
            spreadsheet.refreshAllCellValues();
        });

        add(new Button("Reload", e -> {
            spreadsheet.reload();
        }));

        //spreadsheet.reload();

    }

}
