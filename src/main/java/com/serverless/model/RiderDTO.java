package com.serverless.model;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public class RiderDTO {

    private String id;
    private int rank;
    private String name;
    private String time;
    private String team;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public static RiderDTO mapToDto(Map<String, AttributeValue> item) {
        RiderDTO riderDTO = new RiderDTO();
        riderDTO.setId(item.get("id").s());
        riderDTO.setName(item.get("Rider").s());
        riderDTO.setRank(Integer.parseInt(item.get("Rank").n()));
        riderDTO.setTeam(item.get("Team").s());
        riderDTO.setTime(item.get("Time").s());
        return riderDTO;
    }

    @Override
    public String toString() {
        return "RiderDTO{" +
                "id='" + id + '\'' +
                ", rank=" + rank +
                ", name='" + name + '\'' +
                ", time='" + time + '\'' +
                ", team='" + team + '\'' +
                '}';
    }
}
