/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.netmindz;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.Map;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

/**
 *
 * @author will
 */
public class CurrentTrackVericle extends Verticle {
    

    public void start() {
        final Logger logger = container.logger();
        
        logger.info("Listening for track changes");

        EventBus eb = vertx.eventBus();

        Handler<Message> myHandler = new Handler<Message>() {
            public void handle(Message message) {
                logger.info("I received a message " + message);
                 for (Object chatter : vertx.sharedData().getSet("chat.room")) {
                    try {
                        JsonObject object = (JsonObject) message.body();
                        Map<String, String> current =  vertx.sharedData().getMap("stream.metadata");
                        String jsonOutput = "{\"sender\":\"HardHouseUK\", \"message\":\"Current track is " + current.get("StreamTitle") + "\",\"received\":\""+new Date()+"\"}";
                        logger.info("Sending message " + jsonOutput +  " to " + chatter);
                        vertx.eventBus().send(chatter.toString(), jsonOutput);
                    }
                    catch(Exception e) {
                        logger.error("Failed to send track change to chat : " + e.getMessage(), e);
                    } 
                }

            }
        };

        eb.registerHandler("stream.metadata", myHandler);
    }

}
