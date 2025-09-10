package com.example.openfiremessaging.repository;

import com.example.openfiremessaging.model.AppArchivedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppArchivedMessageRepository extends JpaRepository<AppArchivedMessage, Long> {


    @Query("SELECT m FROM AppArchivedMessage m WHERE " +
            "(m.fromJid = :jid1 AND m.toJid = :jid2) OR " +
            "(m.fromJid = :jid2 AND m.toJid = :jid1) " +
            "ORDER BY m.sentDate ASC")
    List<AppArchivedMessage> findConversation(@Param("jid1") String jid1, @Param("jid2") String jid2);
}

