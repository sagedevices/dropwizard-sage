/*
 * Copyright (c) 2013. Sage Devices, Inc. All Rights Reserved
 */

package com.sagedevices.dropwizard.vertx;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SharedObjectBundle implements Bundle {

    private MetricRegistry metricRegistry;
    private ObjectMapper objectMapper;

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        this.metricRegistry = bootstrap.getMetricRegistry();
        this.objectMapper = bootstrap.getObjectMapper();
    }

    @Override
    public void run(Environment environment) {
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
