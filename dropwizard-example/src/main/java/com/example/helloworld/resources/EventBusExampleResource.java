package com.example.helloworld.resources;

import com.codahale.metrics.annotation.Timed;
import com.sagedevices.dropwizard.vertx.VertxManager;
import com.sagedevices.dropwizard.vertx.VertxResource;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EventBusExampleResource extends VertxResource {

    public EventBusExampleResource(VertxManager vertxManager) {
        super(vertxManager);
    }

    @GET
    @Timed
    @Path("/echo")
    public String echo() {
        final BlockingQueue<String> queue = new ArrayBlockingQueue<>(4);
        eventBus().send("echo", "ping", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> event) {
                queue.add(event.body());
            }
        });
        try {
            return queue.take();
        } catch (InterruptedException e) {
            return "Error";
        }
    }

}
