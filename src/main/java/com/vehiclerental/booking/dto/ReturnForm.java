package com.vehiclerental.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Admin inspecting a returned vehicle. */
public class ReturnForm {

    @NotNull(message = "Enter the date the vehicle was returned")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate actualReturnDate = LocalDate.now();

    @Size(max = 300, message = "Keep the note under 300 characters")
    private String conditionNote;

    private boolean needsMaintenance;

    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }
    public String getConditionNote() { return conditionNote; }
    public void setConditionNote(String conditionNote) { this.conditionNote = conditionNote; }
    public boolean isNeedsMaintenance() { return needsMaintenance; }
    public void setNeedsMaintenance(boolean needsMaintenance) { this.needsMaintenance = needsMaintenance; }
}
