package com.example.openfiremessaging.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class XmppConnectionService {

    @Value("${openfire.host}")
    private String host;

    @Value("${openfire.port}")
    private int port;

    @Getter
    @Value("${openfire.domain}")
    private String domain;

    /**
     * Sends a message by creating a new, temporary connection for the actual user.
     * This is the most reliable way to ensure the sender is recorded correctly by Openfire.
     *
     * @param fromUsername The username of the sender.
     * @param fromPassword The password of the sender.
     * @param toJid        The full JID of the recipient.
     * @param body         The message content.
     * @throws Exception if connection, login, or sending fails.
     */
    public void sendMessageAsUser(String fromUsername, String fromPassword, String toJid, String body,String fileName,String media) throws Exception {
        log.info("Creating temporary XMPP connection for user '{}' to send a message.", fromUsername);
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(fromUsername, fromPassword)
                .setXmppDomain(domain)
                .setHost(host)
                .setPort(port)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();

        // This connection is created just for this message and then closed.
        AbstractXMPPConnection userConnection = new XMPPTCPConnection(config);

        try {
            userConnection.connect();
            userConnection.login();
            log.info("Temporary connection successful for user '{}'.", fromUsername);

            EntityBareJid recipientJid = JidCreate.entityBareFrom(toJid);
            ChatManager chatManager = ChatManager.getInstanceFor(userConnection);
            Chat chat = chatManager.chatWith(recipientJid);
            chat.send(body);

            log.info("Message sent successfully from '{}' to '{}'.", fromUsername, toJid);
        } finally {
            // Ensure the temporary connection is always disconnected, even if sending fails.
            if (userConnection.isConnected()) {
                userConnection.disconnect();
                log.info("Temporary connection for '{}' disconnected.", fromUsername);
            }
        }
    }
}

