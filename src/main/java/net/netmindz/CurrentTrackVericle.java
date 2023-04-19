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
import io.vertx.core.shareddata.LocalMap;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
            String last  = null;
            public void handle(Message<JsonObject> message) {
                logger.info("I received a message " + message.body());
                LocalMap<String, String>  chatters = vertx.sharedData().getLocalMap("chat.room");
                logger.info("Sending track update to " + chatters.size() +  " people in the chat room");
                 for (Map.Entry<String, String> chatter : chatters.entrySet()) {
                    try {
                        if(last != null) {
                            JsonObject updateMessage = new JsonObject();
                            updateMessage.put("sender","HardHouseUK");
                            updateMessage.put("message", "You have beeen listening to " + last);
                            updateMessage.put("received", new Date().toGMTString());
                            logger.info("Sending message " + updateMessage.encode() +  " to " + chatter.getValue());
                            vertx.eventBus().send(chatter.getKey(), updateMessage.encode());
                        }

                        Map<String, String> current =  vertx.sharedData().getLocalMap("stream.metadata");
                        JsonObject updateMessage = new JsonObject();
                        updateMessage.put("sender","HardHouseUK");
                        updateMessage.put("message", "New up is " + current.get("StreamTitle"));
                        updateMessage.put("received", new Date().toGMTString());
                        logger.info("Sending message " + updateMessage.encode() +  " to " + chatter.getValue());
                        vertx.eventBus().send(chatter.getKey(), updateMessage.encode());
                        last = current.get("StreamTitle");
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
