/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.netmindz;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import net.netmindz.streambot.IcyStreamMeta;
import net.netmindz.streambot.MetaDataListener;


/**
 *
 * @author will
 */
public class StreamBotVerticle extends AbstractVerticle {
    
    
    public void start() {
        final Logger logger = LoggerFactory.getLogger(this.getClass());
        try {
            logger.info("Loading stream");
            IcyStreamMeta stream = new IcyStreamMeta(new URL("http://streams.netmindz.net/hhuk.mp3"));
            stream.setListener((Map<String, String> metaData) -> {
                logger.info("Updating metadata " + metaData);
                JsonObject object =  new JsonObject();
                metaData.entrySet().forEach((entry) -> {
                    object.put(entry.getKey(), entry.getValue());
                });
                vertx.eventBus().publish("stream.metadata", object);
                vertx.sharedData().getLocalMap("stream.metadata").putAll(metaData);
            });
            logger.info("Starting stream");
            new Thread(() -> {
                try {
                    stream.refreshMeta();
                }
                catch(IOException ioe) {
                    logger.error(ioe);
                    throw new RuntimeException(ioe);
                }
            }).start();
            logger.info("End of stream");
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
            try {
                stop();
            }
            catch(Exception e2) {
                logger.error(e2);
            }
        }
    }
}
