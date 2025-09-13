package com.example.openfiremessaging.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "app_message_archive")
@Data
public class AppArchivedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromJid;

    private String toJid;

    private Instant sentDate;

    private String body;

    private String mediaName;

    private Integer messageType;

    private String mediaAdd;
    private String fileAdd;

    private String fileName;
}

