package com.example.openfiremessaging.service;

import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.stringprep.XmppStringprepException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class AuthService {

    private final String openfireXmppDomain;
    private final String openfireXmppHost;
    private final int openfireXmppPort;

    public AuthService(@Value("${openfire.xmpp.domain}") String openfireXmppDomain,
                       @Value("${openfire.xmpp.host}") String openfireXmppHost,
                       @Value("${openfire.xmpp.port}") int openfireXmppPort) {
        this.openfireXmppDomain = openfireXmppDomain;
        this.openfireXmppHost = openfireXmppHost;
        this.openfireXmppPort = openfireXmppPort;
    }

    public boolean authenticate(String username, String password) {
        log.info("Attempting to authenticate user: {}", username);

        AbstractXMPPConnection connection = null;

        try {
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setXmppDomain(openfireXmppDomain)
                    .setHost(openfireXmppHost)
                    .setPort(openfireXmppPort)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                    // FIX: Removed .setReplyTimeout(10000) from here
                    .build();

            connection = new XMPPTCPConnection(config);

            // FIX: Set the timeout on the connection object itself, before connecting.
            connection.setReplyTimeout(10000); // 10 seconds

            connection.connect();
            connection.login();
            log.info("User {} authenticated successfully with Openfire.", username);
            return true;

        } catch (XmppStringprepException e) {
            log.warn("Authentication failed for user {}: Invalid username format.", username);
            return false;
        } catch (SASLErrorException e) {
            log.warn("Authentication failed for user {}: Invalid credentials.", username);
            return false;
        } catch (SmackException | IOException | XMPPException | InterruptedException e) {
            log.error("Failed to connect or login for user {} due to a connection/protocol error: {}", username, e.getMessage());
            return false;
        } finally {
            if (connection != null && connection.isConnected()) {
                connection.disconnect();
            }
        }
    }
}