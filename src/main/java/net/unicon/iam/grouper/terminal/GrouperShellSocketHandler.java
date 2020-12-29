package net.unicon.iam.grouper.terminal;

import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.shell.IO;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@Component
public class GrouperShellSocketHandler implements WebSocketHandler {
    private final static String TERMINAL_KEY = "grouper_shell";
    private final static String OUTPUT_KEY = "output_stream";
    private final static String THREAD_KEY = "thread";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        session.getAttributes().putIfAbsent(OUTPUT_KEY, pipedOutputStream);
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

        OutputStream outputStream = new WebsocketOutputStream(session);

        IO io = new IO(pipedInputStream, outputStream, outputStream);

        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();

        GrouperGroovysh shell = new GrouperGroovysh(io, compilerConfiguration, false);
        session.getAttributes().putIfAbsent(TERMINAL_KEY, shell);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String command = ":load " + this.getClass().getResource("/groovysh.profile").toExternalForm();
                shell.run(command);
            }
        });
        thread.start();
        session.getAttributes().putIfAbsent(THREAD_KEY, thread);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        switch (message.getClass().getSimpleName()) {
            case "TextMessage":
                ((OutputStream)session.getAttributes().get(OUTPUT_KEY)).write(((TextMessage)message).asBytes());
                ((OutputStream)session.getAttributes().get(OUTPUT_KEY)).flush();
                break;
            default:
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        ((GrouperGroovysh)session.getAttributes().get(TERMINAL_KEY)).execute(":quit");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
