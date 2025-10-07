package com.creatorboost.chat_service.service;

import com.creatorboost.chat_service.entity.ChatMessage;
import com.creatorboost.chat_service.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatMessageServiceTest {

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Mock
    private ChatMessageRepository repository;

    @Mock
    private ChatRoomService chatRoomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_ShouldSetChatIdAndSaveMessage() {
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setSenderId("user1");
        message.setRecipientId("user2");
        String chatId = "chat123";

        when(chatRoomService.getChatRoomId("user1", "user2", true))
                .thenReturn(Optional.of(chatId));

        // Act
        ChatMessage savedMessage = chatMessageService.save(message);

        // Assert
        assertEquals(chatId, savedMessage.getChatId());
        verify(repository, times(1)).save(message);
    }

    @Test
    void save_ShouldThrowException_WhenChatRoomNotFound() {
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setSenderId("user1");
        message.setRecipientId("user2");

        when(chatRoomService.getChatRoomId("user1", "user2", true))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> chatMessageService.save(message));
        verify(repository, never()).save(any());
    }

    @Test
    void findChatMessages_ShouldReturnMessages_WhenChatExists() {
        // Arrange
        String chatId = "chat123";
        ChatMessage message1 = new ChatMessage();
        message1.setChatId(chatId);
        ChatMessage message2 = new ChatMessage();
        message2.setChatId(chatId);

        when(chatRoomService.getChatRoomId("user1", "user2", false))
                .thenReturn(Optional.of(chatId));

        when(repository.findByChatId(chatId))
                .thenReturn(Arrays.asList(message1, message2));

        // Act
        List<ChatMessage> messages = chatMessageService.findChatMessages("user1", "user2");

        // Assert
        assertEquals(2, messages.size());
        assertTrue(messages.contains(message1));
        assertTrue(messages.contains(message2));
    }

    @Test
    void findChatMessages_ShouldReturnEmptyList_WhenChatDoesNotExist() {
        // Arrange
        when(chatRoomService.getChatRoomId("user1", "user2", false))
                .thenReturn(Optional.empty());

        // Act
        List<ChatMessage> messages = chatMessageService.findChatMessages("user1", "user2");

        // Assert
        assertNotNull(messages);
        assertTrue(messages.isEmpty());
    }
}
