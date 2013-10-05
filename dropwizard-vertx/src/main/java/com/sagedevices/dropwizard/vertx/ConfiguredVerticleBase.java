/*
 * Copyright (c) 2013. Sage Devices, Inc. All Rights Reserved
 */

package com.sagedevices.dropwizard.vertx;

import io.dropwizard.util.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConfiguredVerticleBase<T extends VerticleConfiguration> extends VerticleBase {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguredVerticleBase.class);

    private T configuration;

    @SuppressWarnings("unchecked")
    protected T getConfiguration() {
        if (configuration == null) {
            Class<T> c = (Class<T>)Generics.getTypeParameter(getClass());
            configuration = T.fromJson(container.config(), objectMapper(), c);
        }
        return configuration;
    }

}
