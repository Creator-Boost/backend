package com.creatorboost.chat_service.service;


import com.creatorboost.chat_service.dto.ConversationDTO;
import com.creatorboost.chat_service.dto.UserSummaryDTO;
import com.creatorboost.chat_service.entity.ChatMessage;
import com.creatorboost.chat_service.entity.ChatRoom;
import com.creatorboost.chat_service.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    private UserSummaryDTO getUserProfile(String userId) {
        return userCache.computeIfAbsent(userId, id ->
                webClient.get()
                        .uri("http://localhost:8081/api/v1/profile/" + id)
                        .retrieve()
                        .bodyToMono(UserSummaryDTO.class)
                        .block()
        );
    }

    public List<ConversationDTO> getUserConversations(String userId) {
        var chatRooms = chatRoomService.getChatRoomsForUser(userId);
        List<ConversationDTO> conversations = new ArrayList<>();

        for (ChatRoom room : chatRooms) {
            List<ChatMessage> messages = repository.findByChatId(room.getChatId());
            if (!messages.isEmpty()) {
                ChatMessage lastMessage = messages.get(messages.size() - 1);
                // Determine the other participant
                String participantId = room.getSenderId().equals(userId) ? room.getRecipientId() : room.getSenderId();
                UserSummaryDTO profile = getUserProfile(participantId);

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