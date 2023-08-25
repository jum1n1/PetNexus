package com.sparta.petnexus.chat.controller;

import com.sparta.petnexus.chat.dto.ChatMessageDto;
import com.sparta.petnexus.chat.service.ChatService;

import com.sparta.petnexus.common.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    private final TokenProvider tokenProvider;

    // stompConfig 에서 설정한 applicationDestinationPrefixes 와 @MessageMapping 경로가 병합됨 (/pub + ...)
    // /pub/chat/enter 에 메세지가 오면 동작
    @MessageMapping("chat/enter/{roomId}")
    @SendTo("/sub/chat/{roomId}")
    public ChatMessageDto enter(@DestinationVariable Long roomId, ChatMessageDto message,
        @Header("Authorization") String token) { // 채팅방 입장

        String authorization = tokenProvider.getAccessToken(token);
        String username = tokenProvider.getAuthentication(authorization).getName();

        message.setSender(username);
        message.setRoomId(roomId);
        message.setMessage(username + "님이 채팅방에 참여하였습니다.");

        return message;
    }

    // /pub/chat/message 에 메세지가 오면 동작
    @MessageMapping("chat/message/{roomId}") // 오픈채팅
    @SendTo("/sub/chat/{roomId}")
    public ChatMessageDto message(@DestinationVariable Long roomId, ChatMessageDto message,
        @Header("Authorization") String token) {
        String authorization = tokenProvider.getAccessToken(token);
        String username = tokenProvider.getAuthentication(authorization).getName();

        message.setSender(username);
        message.setRoomId(roomId);

        chatService.saveMessage(roomId, message);

        return message;
    }

    // /pub/trade-chat/message 에 메세지가 오면 동작
    @MessageMapping("tradechat/message/{roomId}") // 중고거래 채팅
    @SendTo("/sub/tradechat/{roomId}")
    public ChatMessageDto tradeMessage(@DestinationVariable Long roomId, ChatMessageDto message,
        @Header("Authorization") String token) {
        String authorization = tokenProvider.getAccessToken(token);
        String username = tokenProvider.getAuthentication(authorization).getName();

        message.setSender(username);
        message.setRoomId(roomId);

        chatService.saveTradeMessage(roomId, message);

        return message;
    }
}