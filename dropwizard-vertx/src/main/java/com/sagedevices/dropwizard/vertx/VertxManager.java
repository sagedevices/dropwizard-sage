/*
 * Copyright (c) 2013. Sage Devices, Inc. All Rights Reserved
 */

package com.sagedevices.dropwizard.vertx;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class VertxManager<T extends VertxConfigurationProvider> implements Managed {

    private static final Logger logger = LoggerFactory.getLogger(VertxManager.class);

    private final PlatformManager platformManager;
    private final VerticleDeployer verticleDeployer;
    private final BlockingQueue<Boolean> waitQueue;
    private final boolean waitForVerticleStartup;
    private Throwable deployFailureCause;

    private static SharedObjectBundle sharedObjectBundle;

    public VertxManager(T configuration) {
            this(configuration, new SharedObjectBundle());
    }

    public VertxManager(T configuration, SharedObjectBundle sharedObjects) {
        sharedObjectBundle = sharedObjects;
        waitForVerticleStartup = configuration.getVertx().isWaitForVerticleStartup();
        final Future<Void> future = new DefaultFutureResult<>();
        waitQueue = new ArrayBlockingQueue<>(1);
        future.setHandler(new AsyncResultHandler<Void>() {
            @Override
            public void handle(AsyncResult<Void> res) {
                deployFailureCause = res.cause();
                putWaitQueue(res.succeeded());
            }
        });
        verticleDeployer = new VerticleDeployer(future);
        if (configuration.getVertx().getClusterPort() != 0 || (!Strings.isNullOrEmpty(configuration.getVertx().getClusterHost()))) {
            platformManager = PlatformLocator.factory.createPlatformManager(configuration.getVertx().getClusterPort(), configuration.getVertx().getClusterHost());
        }
        else {
            platformManager = PlatformLocator.factory.createPlatformManager();
        }
    }

    public Vertx getVertx() {
        return platformManager.vertx();
    }

    public static SharedObjectBundle sharedObjects() {
        return sharedObjectBundle;
    }

    @Override
    public void start() throws Exception {
        logger.debug("Starting vert.x deployment");
        verticleDeployer.deploy(platformManager);
        if (waitForVerticleStartup) {
            boolean deploySucceeded = waitQueue.take();
            if (deploySucceeded) {
                logger.debug("Vert.x deployment complete");
            }
            else {
                logger.error("Vert.x deployment failed");
                throw new Exception("Deployment failed", deployFailureCause);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        logger.debug("Undeploying vert.x modules");
        waitQueue.clear();
        platformManager.undeployAll(new AsyncResultHandler<Void>() {
            @Override
            public void handle(AsyncResult<Void> event) {
                logger.debug("Vert.x undeploy complete");
                platformManager.stop();
                putWaitQueue(true);
            }
        });
        waitQueue.take();
    }

    protected void putWaitQueue(boolean val) {
        try {
            waitQueue.put(val);
        }
        catch (InterruptedException ignored) {
            throw new RuntimeException("Error deploying verticles");
        }
    }

    public void addVerticle(Class<? extends DropwizardVerticle> verticle, Environment environment) {
        verticleDeployer.add(verticle);
        addHealthCheck(verticle, environment);
    }

    public void addWorkerVerticle(Class<? extends DropwizardVerticle> verticle, Environment environment) {
        verticleDeployer.addWorker(verticle);
        addHealthCheck(verticle, environment);
    }

    public void addVerticle(Class<? extends DropwizardVerticle> verticle, VerticleConfiguration config, Environment environment) {
        verticleDeployer.add(verticle, config.getInstances(), config.toJson());
        addHealthCheck(verticle, environment);
    }

    public void addWorkerVerticle(Class<? extends DropwizardVerticle> verticle, VerticleConfiguration config, Environment environment) {
        verticleDeployer.addWorker(verticle, config.getInstances(), config.toJson());
        addHealthCheck(verticle, environment);
    }

    public void addVerticle(Class<? extends DropwizardVerticle> verticle, int instances, VerticleConfiguration config, Environment environment) {
        verticleDeployer.add(verticle, instances, config.toJson());
        addHealthCheck(verticle, environment);
    }

    public void addWorkerVerticle(Class<? extends DropwizardVerticle> verticle, int instances, VerticleConfiguration config, Environment environment) {
        verticleDeployer.addWorker(verticle, instances, config.toJson());
        addHealthCheck(verticle, environment);
    }

    protected void addHealthCheck(Class<? extends DropwizardVerticle> verticle, Environment environment) {
        environment.healthChecks().register(verticle.getSimpleName(), new VerticleHealthCheck(verticle, getVertx()));
    }

    public boolean isWaitForVerticleStartup() {
        return waitForVerticleStartup;
    }

}
