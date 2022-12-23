package study.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    //단일 서버를 사용할 경우에만 유효한 방법이다 (서버 메모리에 세션 저장)
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    ObjectMapper mapper = new ObjectMapper();

    //웹 소켓 연결
    //최초 웹소켓 서버 연결 시 서버에 연결된 다른 사용자에게 접속 여부를 전달해주는 로직을 구현한다. 즉, 채팅방에 이미 들어와있는 사용자에게 신규 멤버가 들어왔다는 것을 알려주는 것이다.
    //이를 구현하기 위해서는 기존 접속 사용자의 웹소켓 세션을 전부 관리하고 있어야 한다.
    //세션 ID를 Key, 세션을 Value로 저장하는 map을 정의한다.
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();  //WebSocketSession의 sessionId는 UUID를 통해 생성된다.
        sessions.put(sessionId, session); //세션 저장

        //웹소켓 최초 연결 시 map에 세션을 저장해두고, 접속 중인 모든 세션에게 메시지를 보낸다.
        Message message = Message.builder()
                .sender(sessionId)
                .receiver("all")
                .build();

        message.newConnect();

        sessions.values().forEach(s -> {
            try {
                if (!s.getId().equals(sessionId)) {
                    s.sendMessage(new TextMessage("hello this is " + sessionId));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        super.afterConnectionEstablished(session);
    }
    
    //웹소켓 양방향 데이터 통신 구현
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Message message = mapper.readValue(textMessage.getPayload(), Message.class); /////
        message.setSender(session.getId());

        log.info("### {}", message.getData());
        log.info("### {}", message.getReceiver());
        WebSocketSession receiver = sessions.get(message.getReceiver()); //메시지 수신자를 찾는다.

        if (receiver != null && receiver.isOpen()) { //수신자가 존재하고 연결된 상태일 경우 메시지를 전송한다.
            receiver.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
        }
    }

    //소켓 통신 에러
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Message message = new Message("error", "server", session.getId(), exception.getMessage());
        session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));

        super.handleTransportError(session, exception);
    }

    //소켓 연결 종료
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);   //세션 저장소에서 연결이 끊어진 사용자 삭제

        final Message message = new Message();
        message.closeConnect();
        message.setSender(sessionId);

        sessions.values().forEach(s -> {
            try {
                s.sendMessage(new TextMessage(mapper.writeValueAsString(message)));  //다른 사용자들에게 접속 종료를 알린다.
            } catch (IOException e) {
                log.info("### ex ", e);
            }
        });
    }
}
