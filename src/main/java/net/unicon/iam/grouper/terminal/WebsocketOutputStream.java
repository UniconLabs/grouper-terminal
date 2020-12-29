package net.unicon.iam.grouper.terminal;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.OutputStream;

public class WebsocketOutputStream extends OutputStream {
    private final WebSocketSession webSocketSession;

    public WebsocketOutputStream(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    @Override
    public void write(int b) throws IOException {
        if (webSocketSession.isOpen()) {
            webSocketSession.sendMessage(new BinaryMessage(new byte[]{(byte) b}));
        }
    }
}
