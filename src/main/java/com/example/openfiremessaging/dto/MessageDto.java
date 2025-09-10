package com.example.openfiremessaging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MessageDto {

    @NotBlank(message = "Recipient 'to' field cannot be blank")
    private String to;

    private String body;

    @NotBlank(message = "'password' field cannot be blank")
    private String password;

    private String fileName;

    private String media;

    public MessageDto(String body, String fileName, String media, String password, String to) {
        this.body = body;
        this.fileName = fileName;
        this.media = media;
        this.password = password;
        this.to = to;
    }
}

