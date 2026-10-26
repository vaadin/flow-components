/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.ai.tests.endpoints.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Monthly revenue of a sales region. The month column is a quoted identifier,
 * as the month name is a keyword in H2.
 */
@Entity
@Table(name = "sales")
public class Sale {

    @Id
    private Integer id;

    @Column(name = "\"MONTH\"", length = 50, nullable = false)
    private String month;

    @Column(name = "month_order", nullable = false)
    private int monthOrder;

    @Column(nullable = false)
    private int revenue;

    @Column(length = 50, nullable = false)
    private String region;

    protected Sale() {
        // For JPA
    }

    public Sale(int id, String month, int monthOrder, int revenue,
            String region) {
        this.id = id;
        this.month = month;
        this.monthOrder = monthOrder;
        this.revenue = revenue;
        this.region = region;
    }

    public Integer getId() {
        return id;
    }

    public String getMonth() {
        return month;
    }

    public int getMonthOrder() {
        return monthOrder;
    }

    public int getRevenue() {
        return revenue;
    }

    public String getRegion() {
        return region;
    }
}
