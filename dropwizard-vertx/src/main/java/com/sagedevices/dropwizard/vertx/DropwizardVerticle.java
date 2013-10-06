/*
 * Copyright (c) 2013. Sage Devices, Inc. All Rights Reserved
 */

package com.sagedevices.dropwizard.vertx;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

public abstract class DropwizardVerticle extends Verticle {

    private static final Logger logger = LoggerFactory.getLogger(DropwizardVerticle.class);

    private String name;

    public String getName() {
        if (Strings.isNullOrEmpty(name)) {
            name = getVerticleName(getClass());
        }
        return name;
    }

    public EventBus eventBus() {
        return vertx.eventBus();
    }

    public static String getVerticleName(Class<? extends DropwizardVerticle> clazz) {
        return clazz.getName();
    }

    public void onStart() {

    }

    public void onStart(Future<Void> startedResult) {
        onStart();
        startedResult.setResult(null);
    }

    public MetricRegistry metrics() {
        return VertxManager.sharedObjects().getMetricRegistry();
    }

    protected ObjectMapper objectMapper() {
        return VertxManager.sharedObjects().getObjectMapper();
    }

    public void onStop() {

    }

    protected void registerHealthCheck() {
        vertx.eventBus().registerHandler(VerticleHealthCheck.getHealthCheckAddress(this), new Handler<Message>() {
            @Override
            public void handle(Message event) {
                logger.debug("Got healthcheck request, replying OK");
                event.reply("ok");
            }
        });
    }

    @Override
    public void stop() {
        try {
            onStop();
        }
        catch(Exception ex) {
            logger.error("Error while stopping verticle: " + getName(), ex);
            throw ex;
        }
        logger.debug("Stopped: {}", getName());
    }

    @Override
    public void start(Future<Void> startedResult) {
        try {
            registerHealthCheck();
            onStart(startedResult);
            logger.debug("Started: {}", getName());
        }
        catch(Exception ex) {
            logger.error("Error while starting verticle: " + getName(), ex);
            throw ex;
        }
    }

}
