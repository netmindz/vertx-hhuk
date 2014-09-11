/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.netmindz;

import org.vertx.java.platform.Verticle;

/**
 *
 * @author will
 */
public class MasterVerticle extends Verticle {
     public void start() {
         container.deployWorkerVerticle("net.netmindz.CurrentTrackVericle");
         container.deployWorkerVerticle("net.netmindz.StreamBotVerticle");
         container.deployWorkerVerticle("net.netmindz.WebserverVerticle");
     }
}
