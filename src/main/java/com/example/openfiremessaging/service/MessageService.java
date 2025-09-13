package com.example.openfiremessaging.service;

import com.example.openfiremessaging.dto.MessageDto;
import com.example.openfiremessaging.model.AppArchivedMessage;
import com.example.openfiremessaging.repository.AppArchivedMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.impl.JidCreate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MessageService {

    private final AppArchivedMessageRepository appArchivedMessageRepository;
    private final Path fileStorageLocation;
    private final Path mediaStorageLocation;
    private final String domain;
    private final String openfireHost;
    private final String adminUsername;
    private final String adminPassword;

    public MessageService(AppArchivedMessageRepository appArchivedMessageRepository,
                          @Value("${file.attachment-dir1}") String fileUploadPath,
                          @Value("${file.attachment-dir2}") String mediaUploadPath,
                          @Value("${openfire.domain}") String domain,
                          @Value("${openfire.host}") String openfireHost,
                          @Value("${openfire.admin.username}") String adminUsername,
                          @Value("${OPENFIRE_ADMIN_PASSWORD}") String adminPassword) {
        this.appArchivedMessageRepository = appArchivedMessageRepository;
        this.domain = domain;
        this.openfireHost = openfireHost;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;

        this.fileStorageLocation = Paths.get(fileUploadPath).toAbsolutePath().normalize();
        this.mediaStorageLocation = Paths.get(mediaUploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            Files.createDirectories(this.mediaStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directories where the uploaded files will be stored.", ex);
        }
    }

    public void sendMessage(MessageDto messageDto) throws Exception {

        String fromUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        String fromJid = fromUsername + "@" + domain;

        // ====== XMPP Sending Logic Start ======
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(adminUsername, adminPassword)
                .setXmppDomain(domain)
                .setHost(openfireHost)
                .setPort(5222)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                .build();

        AbstractXMPPConnection connection = new XMPPTCPConnection(config);

        try {
            connection.connect();
            connection.login();
            log.info("Admin user '{}' connected to Openfire to send a message.", adminUsername);

            // Use MessageBuilder to create the message stanza
            MessageBuilder messageBuilder = MessageBuilder.buildMessage()
                    .to(JidCreate.from(messageDto.getTo()))
                    .from(JidCreate.from(fromJid))
                    .ofType(Message.Type.chat);

            // For now, we only send the text body. File transfer requires more complex XMPP extensions.
            if (StringUtils.hasText(messageDto.getBody())) {
                messageBuilder.setBody(messageDto.getBody());
            } else {
                messageBuilder.setBody(""); // Send empty body if no text but there are attachments
            }

            Message message = messageBuilder.build();

            connection.sendStanza(message);
            log.info("XMPP message sent from {} to {}", fromJid, messageDto.getTo());

        } catch (Exception e) {
            log.error("Failed to send XMPP message", e);
            // Re-throw the exception to notify the controller that the real-time send failed
            throw new Exception("Failed to send XMPP message: " + e.getMessage(), e);
        } finally {
            if (connection.isConnected()) {
                connection.disconnect();
            }
        }
        // ====== XMPP Sending Logic End ======


        // ====== Existing Archiving Logic Start ======
        // This part runs only if the XMPP message was sent successfully.
        String fileAddress = null;
        String mediaAddress = null;
        String uniqueFileName = null;
        String uniqueMediaName = null;

        boolean bodyPresent = StringUtils.hasText(messageDto.getBody());
        boolean mediaPresent = StringUtils.hasText(messageDto.getMedia());
        boolean filePresent = StringUtils.hasText(messageDto.getFile());

        int messageType = determineMessageType(bodyPresent, mediaPresent, filePresent);
        log.info("Determined messageType for archiving: {}", messageType);

        if (filePresent) {
            AttachmentResult fileResult = storeAttachment(messageDto.getFile(), messageDto.getFileName(), fileStorageLocation);
            uniqueFileName = fileResult.getUniqueName();
            fileAddress = fileResult.getFullPath();
        }

        if (mediaPresent) {
            AttachmentResult mediaResult = storeAttachment(messageDto.getMedia(), messageDto.getMediaName(), mediaStorageLocation);
            uniqueMediaName = mediaResult.getUniqueName();
            mediaAddress = mediaResult.getFullPath();
        }

        AppArchivedMessage archivedMessage = new AppArchivedMessage();
        archivedMessage.setFromJid(fromJid);
        archivedMessage.setToJid(messageDto.getTo());
        archivedMessage.setSentDate(Instant.now());
        archivedMessage.setMessageType(messageType);
        archivedMessage.setBody(messageDto.getBody());
        archivedMessage.setMediaName(uniqueMediaName);
        archivedMessage.setFileName(uniqueFileName);
        archivedMessage.setFileAdd(fileAddress);
        archivedMessage.setMediaAdd(mediaAddress);

        appArchivedMessageRepository.save(archivedMessage);
        log.info("Message from {} to {} archived successfully.", fromJid, messageDto.getTo());
        // ====== Existing Archiving Logic End ======
    }


    private AttachmentResult storeAttachment(String base64Content, String originalFileName, Path storageLocation) throws IOException {
        String cleanOriginalName = StringUtils.cleanPath(originalFileName);
        String fileExtension = "";
        if (cleanOriginalName.contains(".")) {
            fileExtension = cleanOriginalName.substring(cleanOriginalName.lastIndexOf("."));
        }
        String uniqueName = UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = storageLocation.resolve(uniqueName);
        MultipartFile multipartFile = convertBase64ToMultipartFile(base64Content, uniqueName);

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Saved attachment '{}' to: {}", originalFileName, targetLocation);
        }

        return new AttachmentResult(uniqueName, targetLocation.toString());
    }

    private static class AttachmentResult {
        private final String uniqueName;
        private final String fullPath;
        public AttachmentResult(String uniqueName, String fullPath) { this.uniqueName = uniqueName; this.fullPath = fullPath; }
        public String getUniqueName() { return uniqueName; }
        public String getFullPath() { return fullPath; }
    }

    private MultipartFile convertBase64ToMultipartFile(String base64Content, String fileName) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
        String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return new MockMultipartFile("file", fileName, contentType, decodedBytes);
    }

    public int determineMessageType(boolean bodyPresent, boolean mediaPresent, boolean filePresent) {
        if (bodyPresent && mediaPresent && filePresent) return 7;
        if (bodyPresent && !mediaPresent && filePresent) return 6;
        if (bodyPresent && mediaPresent && !filePresent) return 5;
        if (!bodyPresent && mediaPresent && filePresent) return 4;
        if (!bodyPresent && !mediaPresent && filePresent) return 3;
        if (!bodyPresent && mediaPresent && !filePresent) return 2;
        if (bodyPresent && !mediaPresent && !filePresent) return 1;
        return 0;
    }

    public List<AppArchivedMessage> getMessageHistory(String withJid) {
        String currentUserJid = SecurityContextHolder.getContext().getAuthentication().getName() + "@" + domain;
        log.info("Fetching conversation history between {} and {}", currentUserJid, withJid);
        return appArchivedMessageRepository.findConversation(currentUserJid, withJid);
    }
}