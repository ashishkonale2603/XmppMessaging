package com.example.openfiremessaging.dto;

import com.example.openfiremessaging.validationrepo.ValidMessage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@ValidMessage
@NoArgsConstructor
@Data
@Getter
@Setter
public class MessageDto {

    @NotBlank(message = "Recipient 'to' field cannot be blank")
    private String to;

    private String body;
    private String mediaName;
    private String media;
    private String fileName;
    private String file;
    private String mediaAdd;
    private String fileAdd;


    public MessageDto(String body,String fileName, String media, String to,String attachAdd,String mediaName,String file) {
        this.body = body;
        this.fileName = fileName;
        this.media = media;
        this.to = to;
        this.mediaName =mediaName;
        this.file=file;
    }
}

