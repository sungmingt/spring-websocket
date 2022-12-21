package study.websocket.stomp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StompMessage {
    private String type;
    private String sender;
    private String channelId;  //구독 채널 id
    private Object data;

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void newConnect(){
        this.type = "new";
    }

    public void closeConnect() {
        this.type = "close";
    }
}
