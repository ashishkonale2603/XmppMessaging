package com.example.openfiremessaging.service;

import com.example.openfiremessaging.dto.MessageDto;
import com.example.openfiremessaging.model.AppArchivedMessage;
import com.example.openfiremessaging.repository.AppArchivedMessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private final XmppConnectionService xmppConnectionService;
    private final AppArchivedMessageRepository appArchivedMessageRepository;

    public void sendMessage(MessageDto messageDto) throws Exception {
        // 1. Get the sender's username from the JWT token.
        String fromUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Call the XMPP service to send the message using the user's own credentials.
        // The password is provided in the request body for this purpose.
        xmppConnectionService.sendMessageAsUser(
                fromUsername,
                messageDto.getPassword(),
                messageDto.getTo(),
                messageDto.getBody(),
                messageDto.getFileName(),
                messageDto.getMedia()
        );

        // 3. Manually save a record to our own archive table for reliable history.
        String fromJid = fromUsername + "@" + xmppConnectionService.getDomain();
        AppArchivedMessage archivedMessage = new AppArchivedMessage();
        long now = System.currentTimeMillis();
        archivedMessage.setFromJid(fromJid);
        archivedMessage.setToJid(messageDto.getTo());
        archivedMessage.setSentDate(String.valueOf(now));
        archivedMessage.setBody(messageDto.getBody());
        archivedMessage.setFileName(messageDto.getFileName());
        archivedMessage.setMedia(messageDto.getMedia());

        appArchivedMessageRepository.save(archivedMessage);
    }

    public List<AppArchivedMessage> getMessageHistory(String withJid) {
        String currentUserJid = SecurityContextHolder.getContext().getAuthentication().getName() + "@" + xmppConnectionService.getDomain();
        logger.info("currentUserJid = {}", currentUserJid);
        logger.info("withJid = {}", withJid);

        return appArchivedMessageRepository.findConversation(currentUserJid, withJid);
    }
}

