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

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A KPI with its current value, target and value range. */
@Entity
@Table(name = "kpi_metrics")
public class KpiMetric {

    @Id
    private Integer id;

    @Column(name = "metric_name", length = 50, nullable = false)
    private String metricName;

    @Column(name = "current_val", precision = 10, scale = 2, nullable = false)
    private BigDecimal currentVal;

    @Column(name = "target_val", precision = 10, scale = 2, nullable = false)
    private BigDecimal targetVal;

    @Column(name = "min_val", precision = 10, scale = 2, nullable = false)
    private BigDecimal minVal;

    @Column(name = "max_val", precision = 10, scale = 2, nullable = false)
    private BigDecimal maxVal;

    protected KpiMetric() {
        // For JPA
    }

    public KpiMetric(int id, String metricName, BigDecimal currentVal,
            BigDecimal targetVal, BigDecimal minVal, BigDecimal maxVal) {
        this.id = id;
        this.metricName = metricName;
        this.currentVal = currentVal;
        this.targetVal = targetVal;
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    public Integer getId() {
        return id;
    }

    public String getMetricName() {
        return metricName;
    }

    public BigDecimal getCurrentVal() {
        return currentVal;
    }

    public BigDecimal getTargetVal() {
        return targetVal;
    }

    public BigDecimal getMinVal() {
        return minVal;
    }

    public BigDecimal getMaxVal() {
        return maxVal;
    }
}
