package net.netmindz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

public class WebserverVerticle extends Verticle {

	@Override
	public void start() {
		final EventBus eventBus = vertx.eventBus();
		final Logger logger = container.logger();

		RouteMatcher httpRouteMatcher = new RouteMatcher().get("/", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest request) {
				request.response().sendFile("web/chat.html");
			}
		}).get(".*\\.(css|js)$", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest request) {
				request.response().sendFile("web/" + new File(request.path()));
			}
		});

		vertx.createHttpServer().requestHandler(httpRouteMatcher).listen(8080, "localhost");

		vertx.createHttpServer().websocketHandler(new Handler<ServerWebSocket>() {
			@Override
			public void handle(final ServerWebSocket ws) {

				final String id = ws.textHandlerID();
				logger.info("registering new connection with id: " + id);
				vertx.sharedData().getSet("chat.room").add(id);
                                Map<String, String> current =  vertx.sharedData().getMap("stream.metadata");
                                String jsonOutput = "{\"sender\":\"HardHouseUK\",\"message\":\"Welcome to Hard House UK, The current track is " + current.get("StreamTitle") + "\",\"received\":\""+new Date()+"\"}";
                                logger.info("Sending welcome message [" + jsonOutput + "]");
                                eventBus.send(id, jsonOutput);

				ws.closeHandler(new Handler<Void>() {
					@Override
					public void handle(final Void event) {
						logger.info("un-registering connection with id: " + id);
						vertx.sharedData().getSet("chat.room").remove(id);
					}
				});

				ws.dataHandler(new Handler<Buffer>() {
					@Override
					public void handle(final Buffer data) {

						ObjectMapper m = new ObjectMapper();
						try {
							JsonNode rootNode = m.readTree(data.toString());
							((ObjectNode) rootNode).put("received", new Date().toString());
							String jsonOutput = m.writeValueAsString(rootNode);
							logger.info("json generated: " + jsonOutput);
							for (Object chatter : vertx.sharedData().getSet("chat.room" )) {
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
