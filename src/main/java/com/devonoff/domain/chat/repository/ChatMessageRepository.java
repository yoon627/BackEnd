package com.devonoff.domain.chat.repository;

import com.devonoff.domain.chat.entity.ChatMessage;
import com.devonoff.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  Page<ChatMessage> findAllByChatRoom(ChatRoom chatRoom, Pageable pageable);

}
