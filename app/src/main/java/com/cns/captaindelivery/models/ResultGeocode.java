package com.cns.captaindelivery.models;

import java.util.List;

public class ResultGeocode {
    List<InfoGeocode> results;
    String status;              //"OK"

    public List<InfoGeocode> getResults() {
        return results;
    }

    public void setResults(List<InfoGeocode> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
