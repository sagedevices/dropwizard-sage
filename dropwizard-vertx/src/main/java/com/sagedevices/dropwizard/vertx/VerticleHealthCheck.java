/*
 * Copyright (c) 2013. Sage Devices, Inc. All Rights Reserved
 */

package com.sagedevices.dropwizard.vertx;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class VerticleHealthCheck extends HealthCheck {

    private static final Logger logger = LoggerFactory.getLogger(VerticleHealthCheck.class);

    public static String getHealthCheckAddress(VerticleBase verticle) {
        return getHealthCheckAddress(verticle.getName());
    }

    public static String getHealthCheckAddress(Class<? extends VerticleBase> clazz) {
        return getHealthCheckAddress(clazz.getName());
    }

    public static String getHealthCheckAddress(String name) {
        return String.format("system/healthcheck/%s", name);
    }

    private final Vertx vertx;
    private final String eventBusAddress;
    private final BlockingQueue<Boolean> queue;

    public VerticleHealthCheck(Class<? extends VerticleBase> verticleClass, Vertx vertx) {
        this.queue = new ArrayBlockingQueue<>(4);
        this.vertx = vertx;
        eventBusAddress = getHealthCheckAddress(verticleClass);
    }

    @Override
    protected Result check() throws Exception {
        try {
            // TODO: Use vert.x messagebus timeout when it's available
            queue.clear();
            logger.debug("Sending status check request to {}", eventBusAddress);
            final long timerId = vertx.setTimer(2000, new Handler<Long>() {
                @Override
                public void handle(Long event) {
                    try {
                        queue.put(false);
                    }
                    catch (InterruptedException ignored) {}
                }
            });
            final Handler<Message<String>> handler = new Handler<Message<String>>() {
                public void handle(Message<String> response) {
                    vertx.cancelTimer(timerId);
                    logger.debug("Got reply from {}: {}", eventBusAddress, response.body());
                    try {
                        boolean success = "ok".equals(response.body());
                        queue.put(success);
                    }
                    catch (InterruptedException ignored) {
                    }
                }
            };

            vertx.eventBus().send(eventBusAddress, "", handler);

            if (queue.take()) {
                return Result.healthy();
            }
            else {
                return Result.unhealthy("Verticle health check timed out");
            }
        }
        catch (Exception ex) {
            logger.error("Error during health check", ex);
            throw ex;
        }
    }

}
