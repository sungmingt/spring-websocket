package study.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    //웹 소켓 연결
    //최초 웹소켓 서버 연결 시 서버에 연결된 다른 사용자에게 접속 여부를 전달해주는 로직을 구현한다. 즉, 채팅방에 이미 들어와있는 사용자에게 신규 멤버가 들어왔다는 것을 알려주는 것이다.
    //이를 구현하기 위해서는 기존 접속 사용자의 웹소켓 세션을 전부 관리하고 있어야 한다.
    //세션 ID를 Key, 세션을 Value로 저장하는 map을 정의한다.
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session); //세션 저장

        //웹소켓 최초 연결 시 map에 세션을 저장해두고, 접속 중인 모든 세션에게 메시지를 보낸다.
        Message message = Message.builder().sender(sessionId).receiver("all").build();
        message.newConnect();

        sessions.values().forEach(s -> {
            try {
                if (!s.getId().equals(sessionId)) {
                    s.sendMessage(new TextMessage((CharSequence) message));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        super.afterConnectionEstablished(session);
    }

    //양방향 데이터 통신
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
    }

    //소켓 통신 에러
   @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    //소켓 연결 종료
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }
}
