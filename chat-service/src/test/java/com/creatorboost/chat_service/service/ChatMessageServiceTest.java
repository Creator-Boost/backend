package com.creatorboost.chat_service.service;

import com.creatorboost.chat_service.entity.ChatMessage;
import com.creatorboost.chat_service.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository repository;

    @Mock
    private ChatRoomService chatRoomService;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private ChatMessage testChatMessage;
    private String testSenderId;
    private String testRecipientId;
    private String testChatId;

    @BeforeEach
    void setUp() {
        testSenderId = "sender123";
        testRecipientId = "recipient456";
        testChatId = "chat789";

        testChatMessage = new ChatMessage();
        testChatMessage.setId("msg123");
        testChatMessage.setSenderId(testSenderId);
        testChatMessage.setRecipientId(testRecipientId);
        testChatMessage.setContent("Test message content");
        testChatMessage.setTimestamp(LocalDateTime.now());
    }

    @Test
    void save_WithValidChatMessage_ShouldSaveAndReturnChatMessage() {
        // Arrange
        when(chatRoomService.getChatRoomId(testSenderId, testRecipientId, true))
                .thenReturn(Optional.of(testChatId));
        when(repository.save(any(ChatMessage.class))).thenReturn(testChatMessage);

        // Act
        ChatMessage result = chatMessageService.save(testChatMessage);

        // Assert
        assertNotNull(result);
        assertEquals(testChatMessage.getId(), result.getId());
        assertEquals(testChatMessage.getSenderId(), result.getSenderId());
        assertEquals(testChatMessage.getRecipientId(), result.getRecipientId());
        assertEquals(testChatMessage.getContent(), result.getContent());
        assertEquals(testChatId, result.getChatId());

        verify(chatRoomService, times(1)).getChatRoomId(testSenderId, testRecipientId, true);
        verify(repository, times(1)).save(testChatMessage);
    }

    @Test
    void save_WhenChatRoomNotFound_ShouldThrowException() {
        // Arrange
        when(chatRoomService.getChatRoomId(testSenderId, testRecipientId, true))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatMessageService.save(testChatMessage);
        });

        verify(chatRoomService, times(1)).getChatRoomId(testSenderId, testRecipientId, true);
        verify(repository, never()).save(any(ChatMessage.class));
    }

    @Test
    void save_ShouldSetChatIdOnMessage() {
        // Arrange
        ChatMessage messageWithoutChatId = new ChatMessage();
        messageWithoutChatId.setSenderId(testSenderId);
        messageWithoutChatId.setRecipientId(testRecipientId);
        messageWithoutChatId.setContent("Test content");

        when(chatRoomService.getChatRoomId(testSenderId, testRecipientId, true))
                .thenReturn(Optional.of(testChatId));
        when(repository.save(any(ChatMessage.class))).thenReturn(messageWithoutChatId);

        // Act
        ChatMessage result = chatMessageService.save(messageWithoutChatId);

        // Assert
        assertEquals(testChatId, messageWithoutChatId.getChatId());
        verify(chatRoomService, times(1)).getChatRoomId(testSenderId, testRecipientId, true);
        verify(repository, times(1)).save(messageWithoutChatId);
    }

    @Test
    void findChatMessages_WhenChatRoomExists_ShouldReturnMessages() {
        // Arrange
        List<ChatMessage> expectedMessages = Arrays.asList(
                createChatMessage("msg1", "Hello"),
                createChatMessage("msg2", "How are you?"),
                createChatMessage("msg3", "Good, thanks!")
        );

        when(chatRoomService.getChatRoomId(testSenderId, testRecipientId, false))
                .thenReturn(Optional.of(testChatId));
        when(repository.findByChatId(testChatId)).thenReturn(expectedMessages);

        // Act
        List<ChatMessage> result = chatMessageService.findChatMessages(testSenderId, testRecipientId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedMessages, result);

        verify(chatRoomService, times(1)).getChatRoomId(testSenderId, testRecipientId, false);
        verify(repository, times(1)).findByChatId(testChatId);
    }

    @Test
    void findChatMessages_WhenChatRoomDoesNotExist_ShouldReturnEmptyList() {
        // Arrange
        when(chatRoomService.getChatRoomId(testSenderId, testRecipientId, false))
                .thenReturn(Optional.empty());

        // Act
        List<ChatMessage> result = chatMessageService.findChatMessages(testSenderId, testRecipientId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(chatRoomService, times(1)).getChatRoomId(testSenderId, testRecipientId, false);
        verify(repository, never()).findByChatId(anyString());
    }

    @Test
    void findChatMessages_WhenNoChatMessagesExist_ShouldReturnEmptyList() {
        // Arrange
        when(chatRoomService.getChatRoomId(testSenderId, testRecipientId, false))
                .thenReturn(Optional.of(testChatId));
        when(repository.findByChatId(testChatId)).thenReturn(Collections.emptyList());

        // Act
        List<ChatMessage> result = chatMessageService.findChatMessages(testSenderId, testRecipientId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(chatRoomService, times(1)).getChatRoomId(testSenderId, testRecipientId, false);
        verify(repository, times(1)).findByChatId(testChatId);
    }

    @Test
    void findChatMessages_WithValidUsers_ShouldCallChatRoomServiceWithCorrectParameters() {
        // Arrange
        when(chatRoomService.getChatRoomId(testSenderId, testRecipientId, false))
                .thenReturn(Optional.of(testChatId));
        when(repository.findByChatId(testChatId)).thenReturn(Collections.emptyList());

        // Act
        chatMessageService.findChatMessages(testSenderId, testRecipientId);

        // Assert
        verify(chatRoomService, times(1)).getChatRoomId(
                eq(testSenderId),
                eq(testRecipientId),
                eq(false)
        );
    }

    @Test
    void save_WithValidUsers_ShouldCallChatRoomServiceWithCorrectParameters() {
        // Arrange
        when(chatRoomService.getChatRoomId(testSenderId, testRecipientId, true))
                .thenReturn(Optional.of(testChatId));
        when(repository.save(any(ChatMessage.class))).thenReturn(testChatMessage);

        // Act
        chatMessageService.save(testChatMessage);

        // Assert
        verify(chatRoomService, times(1)).getChatRoomId(
                eq(testSenderId),
                eq(testRecipientId),
                eq(true)
        );
    }

    // Helper method
    private ChatMessage createChatMessage(String id, String content) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setSenderId(testSenderId);
        message.setRecipientId(testRecipientId);
        message.setContent(content);
        message.setChatId(testChatId);
        message.setTimestamp(LocalDateTime.now());
        return message;
    }
}
