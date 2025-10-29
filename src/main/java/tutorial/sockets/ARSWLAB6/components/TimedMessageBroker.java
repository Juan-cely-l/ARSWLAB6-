package tutorial.sockets.ARSWLAB6.components;

import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tutorial.sockets.ARSWLAB6.endpoints.TimerEndpoint;

import javax.naming.NamingEnumeration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Scope("Singleton")
public class TimedMessageBroker {

    private static final SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm:ss");

    private static final Logger logger = Logger.getLogger(TimedMessageBroker.class.getName());

    @Scheduled(fixedRate=5000)
    public void broadcast(){
        logger.log(Level.INFO,"broadcastingMessages");
        TimerEndpoint.send(String.format("The time is now " + dateFormat.format(new Date())));

    }
}
