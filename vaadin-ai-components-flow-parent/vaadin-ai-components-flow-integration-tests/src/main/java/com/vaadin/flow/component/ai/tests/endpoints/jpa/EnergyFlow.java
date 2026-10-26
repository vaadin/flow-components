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

/** An energy flow from a source to a target. */
@Entity
@Table(name = "energy_flow")
public class EnergyFlow {

    @Id
    private Integer id;

    @Column(name = "source_name", length = 50, nullable = false)
    private String sourceName;

    @Column(name = "target_name", length = 50, nullable = false)
    private String targetName;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    protected EnergyFlow() {
        // For JPA
    }

    public EnergyFlow(int id, String sourceName, String targetName,
            BigDecimal amount) {
        this.id = id;
        this.sourceName = sourceName;
        this.targetName = targetName;
        this.amount = amount;
    }

    public Integer getId() {
        return id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getTargetName() {
        return targetName;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
