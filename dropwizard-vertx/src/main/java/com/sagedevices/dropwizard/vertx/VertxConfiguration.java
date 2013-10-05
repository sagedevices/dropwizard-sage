/*
 * Copyright (c) 2013. Sage Devices, Inc. All Rights Reserved
 */

package com.sagedevices.dropwizard.vertx;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VertxConfiguration {

    @JsonProperty
    private boolean waitForVerticleStartup = true;

    @JsonProperty
    private int clusterPort = 0;

    @JsonProperty
    private String clusterHost = "";

    public boolean isWaitForVerticleStartup() {
        return waitForVerticleStartup;
    }

    public int getClusterPort() {
        return clusterPort;
    }

    public String getClusterHost() {
        return clusterHost;
    }

}
