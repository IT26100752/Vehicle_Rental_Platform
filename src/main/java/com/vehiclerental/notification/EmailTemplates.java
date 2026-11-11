package com.vehiclerental.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Factory for {@link EmailBuilder}: knows the public web address (for links) and the brand name. */
@Component
public class EmailTemplates {

    private final String baseUrl;
    private final String brand;

    public EmailTemplates(@Value("${app.base-url:http://localhost:8080}") String baseUrl,
                          @Value("${app.mail.from-name:DriveLanka Rentals}") String brand) {
        this.baseUrl = baseUrl;
        this.brand = brand;
    }

    /** Start a new email with this subject / heading. */
    public EmailBuilder builder(String title) {
        return new EmailBuilder(title, baseUrl, brand);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getBrand() {
        return brand;
    }
}
