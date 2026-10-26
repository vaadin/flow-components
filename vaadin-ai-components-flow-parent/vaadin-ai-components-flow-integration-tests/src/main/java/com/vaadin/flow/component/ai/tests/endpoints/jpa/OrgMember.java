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

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * A member of the organization chart with the manager as an association and the
 * direct reports as its inverse.
 */
@Entity
@Table(name = "org_chart")
public class OrgMember {

    @Id
    private Integer id;

    @Column(name = "emp_name", length = 100, nullable = false)
    private String empName;

    @Column(name = "job_title", length = 100, nullable = false)
    private String jobTitle;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private OrgMember manager;

    @Column(name = "team_color", length = 20)
    private String teamColor;

    @OneToMany(mappedBy = "manager")
    private List<OrgMember> reports = new ArrayList<>();

    protected OrgMember() {
        // For JPA
    }

    public OrgMember(int id, String empName, String jobTitle, OrgMember manager,
            String teamColor) {
        this.id = id;
        this.empName = empName;
        this.jobTitle = jobTitle;
        this.manager = manager;
        this.teamColor = teamColor;
    }

    public Integer getId() {
        return id;
    }

    public String getEmpName() {
        return empName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public OrgMember getManager() {
        return manager;
    }

    public String getTeamColor() {
        return teamColor;
    }

    public List<OrgMember> getReports() {
        return reports;
    }
}
