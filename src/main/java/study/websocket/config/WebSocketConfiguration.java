package study.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket  //웹 소켓 서버를 사용하도록 정의
public class WebSocketConfiguration implements WebSocketConfigurer { //웹 소켓 서버 설정

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(signalingSocketHandler(), "/room") //웹 소켓 서버 endpoint
                .setAllowedOrigins("*"); //CORS
    }

    @Bean
    public WebSocketHandler signalingSocketHandler() {
        return new study.websocket.WebSocketHandler();
    }
}
