package tutorial.sockets.ARSWLAB6.endpoints;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@ServerEndpoint("/timer")
public class TimerEndpoint {
    private static  final Logger logger = Logger.getLogger("ETFEndpoint");

    static Queue<Session> queue =new ConcurrentLinkedQueue<>();

    public static void send(String msg){
        try{
            for(Session session: queue){
                session.getBasicRemote().sendText(msg);
                logger.log(Level.INFO,"Sent:{0},msg");
            }
        } catch (IOException e) {
            logger.log(Level.INFO,e.toString());
        }
    }
    @OnOpen
    public void openConnection(Session session){
        queue.add(session);
        logger.log(Level.INFO,"Connection opened.");
        try{
            session.getBasicRemote().sendText("Connection established.");
        } catch (IOException e) {
            Logger.getLogger(TimerEndpoint.class.getName()).log(Level.SEVERE,null,e);
        }
    }

    @OnClose
    public void closedConnection(Session session){
        queue.remove(session);
        logger.log(Level.INFO,"COnnection Closed.");

    }

    @OnError
    public void error(Session session,Throwable t){
        queue.remove(session);
        logger.log(Level.INFO,t.toString());
        logger.log(Level.INFO,"Connection Error.");
    }
}
