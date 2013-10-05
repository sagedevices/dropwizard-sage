/*
 * Copyright (c) 2013. Sage Devices, Inc. All Rights Reserved
 */

package com.sagedevices.dropwizard.vertx;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public class VerticleConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(VerticleConfiguration.class);

    @JsonProperty
    private int instances = 1;

    public int getInstances() {
        return instances;
    }

    public JsonObject toJson() {
        String json = Json.encode(this);
        return new JsonObject(json);
    }

    public static <T> T fromJson(final JsonObject config, final ObjectMapper mapper, final Class<T> clazz) {
        final Map<String, Object> map = getFieldValue(config, "map");
        try {
            final String json = mapper.writeValueAsString(map);
            return mapper.readValue(json, clazz);
        }
        catch (IOException e) {
            logger.error("Error converting json to " + clazz.getCanonicalName(), e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E> E getFieldValue(Object object, String fieldName) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (E) field.get(object);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

}
