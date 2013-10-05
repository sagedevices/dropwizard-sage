/*
 * Copyright (c) 2013. Sage Devices, Inc. All Rights Reserved
 */

package com.sagedevices.dropwizard.vertx;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

public class VertxResource<T extends SharedObjectBundle> {

    private final VertxManager vertxManager;

    public VertxResource(VertxManager vertxManager) {
        this.vertxManager = vertxManager;
    }

    @SuppressWarnings("unchecked")
    protected T sharedObjects() {
        return (T)VertxManager.sharedObjects();
    }

    protected EventBus eventBus() {
        return vertxManager.getVertx().eventBus();
    }

    protected Vertx vertx() {
        return vertxManager.getVertx();
    }

}
