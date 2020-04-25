package com.cns.captaindelivery.models;

import java.util.List;

public class ResultCheckEmail extends BaseResult {
    String verify_code;

    public String getVerify_code() {
        return verify_code;
    }

    public void setVerify_code(String verify_code) {
        this.verify_code = verify_code;
    }
}
