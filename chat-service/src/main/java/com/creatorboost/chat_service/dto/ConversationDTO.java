package com.creatorboost.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


import java.util.Date;

@Data
@AllArgsConstructor
public class ConversationDTO {
    private String Id;          // e.g. sender_recipient
    private String participantId;
    private String participantName;   // Name of the other user
    private String participantAvatar;// The other user in this conversation
    private String lastMessage;     // Preview text
    private Date lastMessageTime; // Timestamp of last message
}
