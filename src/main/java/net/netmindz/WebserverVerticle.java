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
import java.io.File;
import java.io.IOException;
import java.util.Date;
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
				logger.info("registering new connection with id: " + id);
				vertx.sharedData().getLocalMap("chat.room").put(id, id);
                                Map<String, String> current =  vertx.sharedData().getLocalMap("stream.metadata");
                                String jsonOutput = "{\"sender\":\"HardHouseUK\",\"message\":\"Welcome to Hard House UK, The current track is " + current.get("StreamTitle") + "\",\"received\":\""+new Date()+"\"}";
                                logger.info("Sending welcome message [" + jsonOutput + "]");
                                eventBus.send(id, jsonOutput);

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
							((ObjectNode) rootNode).put("received", new Date().toString());
							String jsonOutput = m.writeValueAsString(rootNode);
							logger.info("json generated: " + jsonOutput);
							for (Object chatter : vertx.sharedData().getLocalMap("chat.room" ).values()) {
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
}
