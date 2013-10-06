package com.example.helloworld.verticles;

import com.sagedevices.dropwizard.vertx.DropwizardVerticle;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

public class EchoVerticle extends DropwizardVerticle {

    @Override
    public void onStart() {
        vertx.eventBus().registerHandler("echo", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                message.reply("pong");
            }
        });
    }

}
