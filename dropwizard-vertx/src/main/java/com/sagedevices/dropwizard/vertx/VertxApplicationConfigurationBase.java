/*
 * Copyright (c) 2013. Sage Devices, Inc. All Rights Reserved
 */

package com.sagedevices.dropwizard.vertx;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public abstract class VertxApplicationConfigurationBase extends Configuration {

    @JsonProperty
    private VertxConfiguration vertx = new VertxConfiguration();

    public VertxConfiguration getVertx() {
        return vertx;
    }

}
