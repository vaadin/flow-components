package com.vaadin.addon.board.testUI;

import com.vaadin.addon.board.examples.ImageCollage;
import com.vaadin.addon.board.examples.SalesDashboard;
import com.vaadin.server.VaadinRequest;

public class SalesDashboardUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {
        SalesDashboard salesDashboard = new SalesDashboard();
        setContent(salesDashboard);
    }

}
