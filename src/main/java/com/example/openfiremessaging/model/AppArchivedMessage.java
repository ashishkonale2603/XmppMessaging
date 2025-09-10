package com.example.openfiremessaging.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "app_message_archive")
@Data
public class AppArchivedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromJid;

    private String toJid;

    private String sentDate;

    private String body;

    private String fileName;

    private String media;
}

