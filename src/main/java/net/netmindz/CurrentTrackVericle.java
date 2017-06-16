/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.netmindz;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author will
 */
public class CurrentTrackVericle extends AbstractVerticle {
    

    public void start() {
        final Logger logger = LoggerFactory.getLogger(this.getClass());
        
        logger.info("Listening for track changes");

        EventBus eb = vertx.eventBus();

        Handler<Message<JsonObject>> myHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                logger.info("I received a message " + message);
                Collection<Object> chatters = vertx.sharedData().getLocalMap("chat.room").values();
                logger.info("Sending track update to  " + chatters.size() +  " people in the chat room");
                 for (Object chatter : chatters) {
                    try {
                        JsonObject object = message.body();
                        Map<String, String> current =  vertx.sharedData().getLocalMap("stream.metadata");
                        String jsonOutput = "{\"sender\":\"HardHouseUK\", \"message\":\"New track is " + current.get("StreamTitle") + "\",\"received\":\""+new Date()+"\"}";
                        logger.info("Sending message " + jsonOutput +  " to " + chatter);
                        vertx.eventBus().send(chatter.toString(), jsonOutput);
                    }
                    catch(Exception e) {
                        logger.error("Failed to send track change to chat : " + e.getMessage(), e);
                    } 
                }

            }
        };

        eb.consumer("stream.metadata", myHandler);
    }

}
