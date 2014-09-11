/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.netmindz;

import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import net.netmindz.streambot.IcyStreamMeta;
import net.netmindz.streambot.MetaDataListener;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

/**
 *
 * @author will
 */
public class StreamBotVerticle extends Verticle {
    
    
    public void start() {
        final Logger logger = container.logger();
        try {
            logger.info("Loading stream");
            IcyStreamMeta stream = new IcyStreamMeta(new URL("http://streams.netmindz.net/hhuk.mp3"));
            stream.setListener(new MetaDataListener() {

                @Override
                public void newMetaData(Map<String, String> metaData) {
                    logger.info("Updating metadata " + metaData);
                    JsonObject object =  new JsonObject();
                    for(Entry<String, String> entry : metaData.entrySet()) {
                        object.putString(entry.getKey(), entry.getValue());
                    }
                    vertx.eventBus().publish("stream.metadata", object);
                    vertx.sharedData().getMap("stream.metadata").putAll(metaData);
                }
            });
            logger.info("Starting stream");
            stream.refreshMeta();
            logger.info("End of stream");
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
