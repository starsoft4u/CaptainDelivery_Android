package com.cns.captaindelivery.models;

import java.util.List;

public class ResultGooglePlaceList {
    String next_page_token;
    List<InfoGooglePlace> results;
    String status;              //"OK"

    public String getNext_page_token() {
        return next_page_token;
    }

    public void setNext_page_token(String next_page_token) {
        this.next_page_token = next_page_token;
    }

    public List<InfoGooglePlace> getResults() {
        return results;
    }

    public void setResults(List<InfoGooglePlace> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
