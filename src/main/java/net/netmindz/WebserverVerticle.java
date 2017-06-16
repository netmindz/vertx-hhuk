package net.netmindz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


public class WebserverVerticle extends AbstractVerticle {

	@Override
	public void start() {
		final EventBus eventBus = vertx.eventBus();
		final Logger logger = LoggerFactory.getLogger(this.getClass());

                HttpServer server = vertx.createHttpServer();
                
                server.requestHandler(request -> {
                    // Handle the request in here
                    if(request.path().equals("/")) {
                        logger.info("Index page requested");
			request.response().sendFile("web/chat.html");
                    }
                    else if(request.path().endsWith(".css") || request.path().endsWith(".js")) {
                        logger.info(request.path() + " requested");
                        request.response().sendFile("web" + new File(request.path()));
                    }
                    else {
                        logger.warn("Bad request " + request.path());
                        request.response().setStatusCode(500).close();
                    }
                });


		server.websocketHandler(new Handler<ServerWebSocket>() {
			@Override
			public void handle(final ServerWebSocket ws) {

				final String id = ws.textHandlerID();
                                String nickname;
                                try {
                                    nickname = splitQuery(new URI(ws.uri())).get("nickname");
                                }
                                catch(URISyntaxException | UnsupportedEncodingException e) {
                                    throw new RuntimeException(e);
                                }
				logger.info("registering new connection with id: " + id + " for " + nickname);
                                    vertx.sharedData().getLocalMap("chat.room").put(id, nickname);
                                    
                                Map<String, String> current =  vertx.sharedData().getLocalMap("stream.metadata");
                                String jsonOutput = "{\"sender\":\"HardHouseUK\",\"message\":\"Welcome to Hard House UK, The current track is " + current.get("StreamTitle") + "\",\"received\":\""+new Date()+"\"}";
                                logger.info("Sending welcome message [" + jsonOutput + "]");
                                eventBus.send(id, jsonOutput);
                                LocalMap<Object, Object> room = vertx.sharedData().getLocalMap("chat.room");
                                if(room.size() > 1) {
                                    jsonOutput = "{\"sender\":\"HardHouseUK\",\"message\":\"There are "+room.size()+" people in the chat room : "+room.values().toString()+"\",\"received\":\""+new Date()+"\"}";
                                    logger.info("Sending count message [" + jsonOutput + "]");
                                    eventBus.send(id, jsonOutput);
                                }

                                jsonOutput = "{\"sender\":\"HardHouseUK\",\"message\":\""+nickname+" just joined chat\",\"received\":\""+new Date()+"\"}";
                                for (Object chatter : room.keySet()) {
                                    if(chatter.toString().equals(id)) continue;
                                    eventBus.send((String) chatter, jsonOutput);
                                }

                                
				ws.closeHandler(new Handler<Void>() {
					@Override
					public void handle(final Void event) {
						logger.info("un-registering connection with id: " + id);
						vertx.sharedData().getLocalMap("chat.room").remove(id);
					}
				});

				ws.handler(new Handler<Buffer>() {
					@Override
					public void handle(final Buffer data) {

						ObjectMapper m = new ObjectMapper();
						try {
							JsonNode rootNode = m.readTree(data.toString());
                                                        String nickname = (String) vertx.sharedData().getLocalMap("chat.room").get(id);
							((ObjectNode) rootNode).put("received", new Date().toString());
                                                        ((ObjectNode) rootNode).put("sender", nickname);
							String jsonOutput = m.writeValueAsString(rootNode);
							logger.info("json generated: " + jsonOutput);
							for (Object chatter : vertx.sharedData().getLocalMap("chat.room").keySet()) {
								eventBus.send((String) chatter, jsonOutput);
							}
						} catch (IOException e) {
							ws.reject();
						}
					}
				});

			}
		}).listen(8090);
	}
        
    private static Map<String, String> splitQuery(URI uri) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = uri.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
}
}
