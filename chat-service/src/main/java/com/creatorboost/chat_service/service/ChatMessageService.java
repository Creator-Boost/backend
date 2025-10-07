package com.creatorboost.chat_service.service;


import com.creatorboost.chat_service.dto.ConversationDTO;
import com.creatorboost.chat_service.dto.UserSummaryDTO;
import com.creatorboost.chat_service.entity.ChatMessage;
import com.creatorboost.chat_service.entity.ChatRoom;
import com.creatorboost.chat_service.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;
    private final WebClient webClient; // Injected for profile service calls

    private final Map<String, UserSummaryDTO> userCache = new ConcurrentHashMap<>();


    public ChatMessage save(ChatMessage chatMessage) {
        var chatId = chatRoomService
                .getChatRoomId(chatMessage.getSenderId(), chatMessage.getRecipientId(), true)
                .orElseThrow(); // You can create your own dedicated exception
        chatMessage.setChatId(chatId);
        repository.save(chatMessage);
        return chatMessage;
    }

    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
        return chatId.map(repository::findByChatId).orElse(new ArrayList<>());
    }

    private UserSummaryDTO getUserProfile(String userId,String token) {
        //System.out.println("JWT forwarded to WebClient (Service): " + token);
        return userCache.computeIfAbsent(userId, id -> {
            try {
                return webClient.get()
                        .uri("/api/auth/profile/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(UserSummaryDTO.class)
                        .block();
            } catch (WebClientResponseException e) {
                throw new RuntimeException("Failed to fetch profile for user " + id, e);
            }
        });
    }

    public List<ConversationDTO> getUserConversations(String userId,String token) {
        var chatRooms = chatRoomService.getChatRoomsForUser(userId);
        List<ConversationDTO> conversations = new ArrayList<>();

        for (ChatRoom room : chatRooms) {
            List<ChatMessage> messages = repository.findByChatId(room.getChatId());
            if (!messages.isEmpty()) {
                ChatMessage lastMessage = messages.get(messages.size() - 1);
                // Determine the other participant
                String participantId = room.getSenderId().equals(userId) ? room.getRecipientId() : room.getSenderId();
                UserSummaryDTO profile = getUserProfile(participantId, token);

                conversations.add(new ConversationDTO(
                        room.getId(),
                        participantId,
                        profile.getName(),
                        profile.getImageUrl(),
                        lastMessage.getContent(),
                        lastMessage.getTimestamp()
                ));
            }
        }
        return conversations;
    }
}