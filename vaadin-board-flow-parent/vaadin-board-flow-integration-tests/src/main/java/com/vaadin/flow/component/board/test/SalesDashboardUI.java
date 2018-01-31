package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.examples.ImageCollage;
import com.vaadin.flow.component.board.examples.SalesDashboard;
import com.vaadin.server.VaadinRequest;

public class SalesDashboardUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {
        SalesDashboard salesDashboard = new SalesDashboard();
        setContent(salesDashboard);
    }

}
