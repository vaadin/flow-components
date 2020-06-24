package com.vaadin.flow.component.select.data;

import com.vaadin.flow.component.select.entity.Team;

import java.util.ArrayList;
import java.util.List;

public class TeamData {

    private final static List<Team> TEAM_LIST = createTeamList();

    private static List<Team> createTeamList() {
        List<Team> teamList = new ArrayList<>();

        teamList = new ArrayList<>();
        teamList.add(new Team(1, "Flow", 1));
        teamList.add(new Team(2, "Components", 1));
        teamList.add(new Team(3, "Pro tools", 1));
        teamList.add(new Team(4, "Developers Journey and Onboarding", 1));
        teamList.add(new Team(5, "Experts", 2));
        teamList.add(new Team(6, "Incubator", 2));

        return teamList;
    }

    public List<Team> getTeams() {
        return TEAM_LIST;
    }
}
