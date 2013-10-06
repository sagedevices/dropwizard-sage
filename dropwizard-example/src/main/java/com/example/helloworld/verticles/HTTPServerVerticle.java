package com.example.helloworld.verticles;

import com.sagedevices.dropwizard.vertx.DropwizardVerticle;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Verticle;

import java.util.Map;

public class HTTPServerVerticle extends DropwizardVerticle {

    @Override
    public void onStart() {
        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                System.out.println("Got request: " + req.uri());
                System.out.println("Headers are: ");
                for (Map.Entry<String, String> entry : req.headers()) {
                    System.out.println(entry.getKey() + ":" + entry.getValue());
                }
                req.response().headers().set("Content-Type", "text/html; charset=UTF-8");
                req.response().end("<html><body><h1>Hello from vert.x!</h1></body></html>");
            }
        }).listen(8000);
    }

}
