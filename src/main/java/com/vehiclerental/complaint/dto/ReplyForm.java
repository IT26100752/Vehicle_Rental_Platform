package com.vehiclerental.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReplyForm {

    @NotBlank(message = "Write a message")
    @Size(max = 1500, message = "Message must be under 1500 characters")
    private String message;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
