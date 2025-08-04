package com.creatorboost.chat_service.service;

import com.creatorboost.chat_service.entity.ChatRoom;
import com.creatorboost.chat_service.repository.ChatRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    private String testSenderId;
    private String testRecipientId;
    private String testChatId;
    private ChatRoom testChatRoom;

    @BeforeEach
    void setUp() {
        testSenderId = "sender123";
        testRecipientId = "recipient456";
        testChatId = "sender123_recipient456";

        testChatRoom = ChatRoom.builder()
                .chatId(testChatId)
                .senderId(testSenderId)
                .recipientId(testRecipientId)
                .build();
    }

    @Test
    void getChatRoomId_WhenChatRoomExists_ShouldReturnChatId() {
        // Arrange
        when(chatRoomRepository.findBySenderIdAndRecipientId(testSenderId, testRecipientId))
                .thenReturn(Optional.of(testChatRoom));

        // Act
        Optional<String> result = chatRoomService.getChatRoomId(testSenderId, testRecipientId, false);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testChatId, result.get());
        verify(chatRoomRepository, times(1)).findBySenderIdAndRecipientId(testSenderId, testRecipientId);
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    void getChatRoomId_WhenChatRoomDoesNotExistAndCreateNewIsTrue_ShouldCreateAndReturnChatId() {
        // Arrange
        when(chatRoomRepository.findBySenderIdAndRecipientId(testSenderId, testRecipientId))
                .thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);

        // Act
        Optional<String> result = chatRoomService.getChatRoomId(testSenderId, testRecipientId, true);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testChatId, result.get());
        verify(chatRoomRepository, times(1)).findBySenderIdAndRecipientId(testSenderId, testRecipientId);
        verify(chatRoomRepository, times(2)).save(any(ChatRoom.class)); // Should save both directions
    }

    @Test
    void getChatRoomId_WhenChatRoomDoesNotExistAndCreateNewIsFalse_ShouldReturnEmpty() {
        // Arrange
        when(chatRoomRepository.findBySenderIdAndRecipientId(testSenderId, testRecipientId))
                .thenReturn(Optional.empty());

        // Act
        Optional<String> result = chatRoomService.getChatRoomId(testSenderId, testRecipientId, false);

        // Assert
        assertFalse(result.isPresent());
        verify(chatRoomRepository, times(1)).findBySenderIdAndRecipientId(testSenderId, testRecipientId);
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    void getChatRoomId_WhenCreatingNewChatRoom_ShouldCreateBothDirections() {
        // Arrange
        when(chatRoomRepository.findBySenderIdAndRecipientId(testSenderId, testRecipientId))
                .thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);

        // Act
        Optional<String> result = chatRoomService.getChatRoomId(testSenderId, testRecipientId, true);

        // Assert
        assertTrue(result.isPresent());
        verify(chatRoomRepository, times(2)).save(any(ChatRoom.class));

        // Verify that both sender->recipient and recipient->sender chat rooms are created
        verify(chatRoomRepository).save(argThat(chatRoom ->
            chatRoom.getSenderId().equals(testSenderId) &&
            chatRoom.getRecipientId().equals(testRecipientId)
        ));
        verify(chatRoomRepository).save(argThat(chatRoom ->
            chatRoom.getSenderId().equals(testRecipientId) &&
            chatRoom.getRecipientId().equals(testSenderId)
        ));
    }

    @Test
    void getChatRoomId_ShouldGenerateCorrectChatIdFormat() {
        // Arrange
        when(chatRoomRepository.findBySenderIdAndRecipientId(testSenderId, testRecipientId))
                .thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);

        // Act
        Optional<String> result = chatRoomService.getChatRoomId(testSenderId, testRecipientId, true);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("sender123_recipient456", result.get());
    }

    @Test
    void getChatRoomId_WithNullParameters_ShouldHandleGracefully() {
        // Act & Assert - These should be handled by the repository layer
        assertDoesNotThrow(() -> {
            chatRoomService.getChatRoomId(null, testRecipientId, false);
        });

        assertDoesNotThrow(() -> {
            chatRoomService.getChatRoomId(testSenderId, null, false);
        });
    }

    @Test
    void getChatRoomId_WithEmptyStrings_ShouldCreateValidChatId() {
        // Arrange
        String emptySender = "";
        String emptyRecipient = "";
        when(chatRoomRepository.findBySenderIdAndRecipientId(emptySender, emptyRecipient))
                .thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);

        // Act
        Optional<String> result = chatRoomService.getChatRoomId(emptySender, emptyRecipient, true);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("_", result.get());
    }
}
