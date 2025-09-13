package com.example.openfiremessaging.controller;

import com.example.openfiremessaging.dto.MessageDto;
import com.example.openfiremessaging.model.AppArchivedMessage;
import com.example.openfiremessaging.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;

    @PostMapping(path = "/send")
    public ResponseEntity<?> sendMessage(@Valid @RequestBody MessageDto messageDto) {
        // The file from the request is now inside messageDto.getFile()
        try {
            messageService.sendMessage(messageDto);
            return ResponseEntity.ok("Message sent and archived successfully");
        } catch (Exception e) {
            log.error("Failed to send message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send message: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getMessageHistory(@RequestParam String with) {
        try {
            List<AppArchivedMessage> history = messageService.getMessageHistory(with);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Failed to retrieve message history for user {}", with, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve message history: " + e.getMessage());
        }
    }
}