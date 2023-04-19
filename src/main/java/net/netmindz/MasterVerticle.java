/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.netmindz;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 *
 * @author will
 */
public class MasterVerticle extends AbstractVerticle {
    
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public void start() {
        
        logger.info("Starting CurrentTrackVericle");
        vertx.deployVerticle("net.netmindz.CurrentTrackVericle");
        
        logger.info("Starting StreamBotVerticle");
        vertx.deployVerticle("net.netmindz.StreamBotVerticle", new DeploymentOptions().setWorker(true));

        logger.info("Starting WebserverVerticle");
        vertx.deployVerticle("net.netmindz.WebserverVerticle");
        
        logger.info("Started");
    }
}
