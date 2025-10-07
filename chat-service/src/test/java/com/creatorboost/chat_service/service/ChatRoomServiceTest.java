package com.creatorboost.chat_service.service;

import com.creatorboost.chat_service.entity.ChatRoom;
import com.creatorboost.chat_service.repository.ChatRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatRoomServiceTest {

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getChatRoomId_ShouldReturnExistingChatId() {
        // Arrange
        String senderId = "user1";
        String recipientId = "user2";
        String chatId = "user1_user2";

        ChatRoom existingRoom = ChatRoom.builder()
                .chatId(chatId)
                .senderId(senderId)
                .recipientId(recipientId)
                .build();

        when(chatRoomRepository.findBySenderIdAndRecipientId(senderId, recipientId))
                .thenReturn(Optional.of(existingRoom));

        // Act
        Optional<String> result = chatRoomService.getChatRoomId(senderId, recipientId, true);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(chatId, result.get());
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    void getChatRoomId_ShouldCreateNewChatId_WhenNotExistsAndFlagTrue() {
        // Arrange
        String senderId = "user1";
        String recipientId = "user2";

        when(chatRoomRepository.findBySenderIdAndRecipientId(senderId, recipientId))
                .thenReturn(Optional.empty());

        // Act
        Optional<String> result = chatRoomService.getChatRoomId(senderId, recipientId, true);

        // Assert
        assertTrue(result.isPresent());
        String expectedChatId = "user1_user2";
        assertEquals(expectedChatId, result.get());
        verify(chatRoomRepository, times(2)).save(any(ChatRoom.class)); // saves senderRecipient and recipientSender
    }

    @Test
    void getChatRoomId_ShouldReturnEmpty_WhenNotExistsAndFlagFalse() {
        // Arrange
        String senderId = "user1";
        String recipientId = "user2";

        when(chatRoomRepository.findBySenderIdAndRecipientId(senderId, recipientId))
                .thenReturn(Optional.empty());

        // Act
        Optional<String> result = chatRoomService.getChatRoomId(senderId, recipientId, false);

        // Assert
        assertTrue(result.isEmpty());
        verify(chatRoomRepository, never()).save(any());
    }
}
