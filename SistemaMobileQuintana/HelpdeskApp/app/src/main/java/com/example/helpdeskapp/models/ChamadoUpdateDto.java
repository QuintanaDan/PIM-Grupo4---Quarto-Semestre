package com.example.helpdeskapp.models;

public class ChamadoUpdateDto {
    private String status;

    public ChamadoUpdateDto() {}

    public ChamadoUpdateDto(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
