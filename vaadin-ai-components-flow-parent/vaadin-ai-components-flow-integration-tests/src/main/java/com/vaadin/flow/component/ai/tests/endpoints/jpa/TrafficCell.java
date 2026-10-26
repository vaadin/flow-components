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
 * Website visitors in one hour of one weekday. Both indices are 0-based: day 0
 * is Monday and hour 0 is 9am.
 */
@Entity
@Table(name = "website_traffic")
public class TrafficCell {

    @Id
    private Integer id;

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "hour_of_day", nullable = false)
    private int hourOfDay;

    @Column(nullable = false)
    private int visitors;

    protected TrafficCell() {
        // For JPA
    }

    public TrafficCell(int id, int dayOfWeek, int hourOfDay, int visitors) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.hourOfDay = hourOfDay;
        this.visitors = visitors;
    }

    public Integer getId() {
        return id;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public int getVisitors() {
        return visitors;
    }
}
