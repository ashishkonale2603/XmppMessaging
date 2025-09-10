package com.example.openfiremessaging.service;

import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

    @Value("${openfire.host}")
    private String host;

    @Value("${openfire.port}")
    private int port;

    @Value("${openfire.domain}")
    private String domain;

    public boolean authenticate(String username, String password) {
        try {
            log.info("Attempting to authenticate user: {}", username);
            // The .addSASLMechanism() line has been removed from the configuration builder.
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setXmppDomain(domain)
                    .setHost(host)
                    .setPort(port)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .build();

            AbstractXMPPConnection tempConnection = new XMPPTCPConnection(config);
            tempConnection.connect();
            tempConnection.login();
            boolean isAuthenticated = tempConnection.isAuthenticated();
            tempConnection.disconnect();

            if (isAuthenticated) {
                log.info("Authentication successful for user: {}", username);
            } else {
                log.warn("Authentication failed for user: {}", username);
            }
            return isAuthenticated;

        } catch (Exception e) {
            log.error("XMPP authentication error for user: {}", username, e);
            return false;
        }
    }
}

