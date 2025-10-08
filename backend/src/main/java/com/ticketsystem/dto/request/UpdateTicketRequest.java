package com.ticketsystem.dto.request;

import lombok.Data;

@Data
public class UpdateTicketRequest {
    private String title;
    private String description;
    private String priority;
    private String status;
}
