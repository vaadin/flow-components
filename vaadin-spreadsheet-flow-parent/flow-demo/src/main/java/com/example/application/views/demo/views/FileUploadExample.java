package com.example.application.views.demo.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.upload.FinishedEvent;
import com.vaadin.flow.component.upload.ProgressListener;
import com.vaadin.flow.component.upload.ProgressUpdateEvent;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.StartedEvent;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.shared.Registration;

public class FileUploadExample extends Div implements ComponentEventListener, Receiver {

    private Upload upload;
    private final long maxSize = 1000000;
    private Div spreadsheetPanel;
    private ByteArrayOutputStream baos = null;


    public FileUploadExample() {
        setSizeFull();
        addClassName("formattingexample");

        initSpreadsheetPanel();
        add(createUploadLayout());
        add(spreadsheetPanel);
        //layout.setExpandRatio(spreadsheetPanel, 1);
    }

    private HorizontalLayout createUploadLayout() {
        initUpload();
        HorizontalLayout header = new HorizontalLayout();
        header.setSpacing(false);
        header.setMargin(true);
        header.add(upload);
        //header.setComponentAlignment(progressBar, Alignment.MIDDLE_CENTER);
        //header.setExpandRatio(progressBar, 1);
        return header;
    }

    private void initUpload() {
        upload = new Upload(this);
        upload.addStartedListener(this);
        upload.addProgressListener(this);
        upload.addFinishedListener(this);
        upload.addSucceededListener(this);
    }

    private void initSpreadsheetPanel() {
        Spreadsheet spreadsheet = new Spreadsheet();
        CellStyle backgroundColorStyle = spreadsheet.getWorkbook()
                .createCellStyle();
        backgroundColorStyle.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
        Cell cell = spreadsheet.createCell(0, 0,
                "Click the upload button to choose and upload an excel file.");
        cell.setCellStyle(backgroundColorStyle);

        for (int i = 1; i <= 5; i++) {
            cell = spreadsheet.createCell(0, i, "");
            cell.setCellStyle(backgroundColorStyle);
        }

        spreadsheetPanel = new Div();
        spreadsheetPanel.setSizeFull();
        spreadsheetPanel.add(spreadsheet);
    }


    @Override
    public void onComponentEvent(ComponentEvent componentEvent) {
        if (componentEvent instanceof StartedEvent) {
        } else if (componentEvent instanceof ProgressUpdateEvent) {
            long readBytes = ((ProgressUpdateEvent) componentEvent).getReadBytes();
            long contentLength = ((ProgressUpdateEvent) componentEvent).getContentLength();
            if (readBytes > maxSize || contentLength > maxSize) {
                upload.interruptUpload();
                Notification.show("File is to big. Maximum filesize: "
                        + maxSize / 1000 + "KB");
            }
        } else if (componentEvent instanceof SucceededEvent) {
            ByteArrayInputStream bais = null;
            try {
                bais = new ByteArrayInputStream(baos.toByteArray());
                Spreadsheet spreadsheet = new Spreadsheet(bais);
                spreadsheetPanel.removeAll();
                spreadsheetPanel.add(spreadsheet);
            } catch (IOException e) {
                Notification.show("Not a valid file!");
            } finally {
                baos = null;
                IOUtils.closeQuietly(bais);
            }
        } else if (componentEvent instanceof FinishedEvent) {
        }
    }

    @Override
    public OutputStream receiveUpload(final String filename, String mimeType) {
        return baos = new ByteArrayOutputStream();
    }
}
