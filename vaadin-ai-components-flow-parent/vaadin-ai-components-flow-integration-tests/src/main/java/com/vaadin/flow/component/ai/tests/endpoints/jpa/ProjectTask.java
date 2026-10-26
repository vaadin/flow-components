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
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A task of the project plan. The dependency on another task is an association,
 * which JPQL navigates as {@code t.dependsOn.taskName}.
 */
@Entity
@Table(name = "project_tasks")
public class ProjectTask {

    @Id
    private Integer id;

    @Column(name = "task_name", length = 100, nullable = false)
    private String taskName;

    @Column(length = 50, nullable = false)
    private String phase;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(precision = 3, scale = 2, nullable = false)
    private BigDecimal progress;

    @ManyToOne
    @JoinColumn(name = "depends_on")
    private ProjectTask dependsOn;

    protected ProjectTask() {
        // For JPA
    }

    public ProjectTask(int id, String taskName, String phase,
            LocalDate startDate, LocalDate endDate, BigDecimal progress,
            ProjectTask dependsOn) {
        this.id = id;
        this.taskName = taskName;
        this.phase = phase;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
        this.dependsOn = dependsOn;
    }

    public Integer getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getPhase() {
        return phase;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BigDecimal getProgress() {
        return progress;
    }

    public ProjectTask getDependsOn() {
        return dependsOn;
    }
}
