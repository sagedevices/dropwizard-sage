package com.sagedevices.dropwizard.vertx;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VerticleDeployer {

    private static final Logger logger = LoggerFactory.getLogger(VerticleDeployer.class);

    private Optional<Future<Void>> startedResult;
    private boolean isStarted;

    protected List<VerticleDescriptor> verticles;

    public VerticleDeployer() {
        this.startedResult = Optional.absent();
        reset();
    }

    public VerticleDeployer(Future<Void> startedResult) {
        this.startedResult = Optional.of(startedResult);
        reset();
    }

    protected void reset() {
        verticles = new ArrayList<>();
        isStarted = false;
    }

    public void add(Class<? extends VerticleBase> verticle) {
        add(verticle, 1, null, false);
    }

    public void addWorker(Class<? extends VerticleBase> verticle) {
        add(verticle, 1, null, true);
    }

    public void add(Class<? extends VerticleBase> verticle, JsonObject config) {
        add(verticle, 1, config, false);
    }

    public void addWorker(Class<? extends VerticleBase> verticle, JsonObject config) {
        add(verticle, 1, config, true);
    }

    public void add(Class<? extends VerticleBase> verticle, int instances, JsonObject config) {
        add(verticle, instances, config, false);
    }

    public void addWorker(Class<? extends VerticleBase> verticle, int instances, JsonObject config) {
        add(verticle, instances, config, true);
    }

    private synchronized void add(Class<? extends VerticleBase> verticle, int instances, JsonObject config, boolean isWorker) {
        if (!isStarted) {
            verticles.add(new VerticleDescriptor(verticle, instances, config, isWorker));
        }
        else throw new IllegalStateException("Cannot add verticles once deployment has started");
    }

    public synchronized void deploy(PlatformManager pm) {
        if (!isStarted) {
            if (verticles.size() == 0) {
                throw new IllegalStateException("You must add at least one verticle");
            }
            isStarted = true;
            for(VerticleDescriptor v : verticles) {
                v.deploy(pm);
            }
        }
        else throw new IllegalStateException("Cannot deploy more than once");
    }

    /***
     * This is called when an individual verticle has been successfully deployed.  This will check to see
     * if all other verticles have finished deploying, and if so, will fire the main deployer callback
     * @param vd
     */
    private synchronized void deployComplete(VerticleDescriptor vd) {
        if (isStarted) {
            boolean success = true;
            Throwable failureCause = null;

            for(VerticleDescriptor v : verticles) {
                if (!v.complete()) {
                    return;
                }
                if (!v.succeeded()) {
                    success = false;
                    if (v.result != null) {
                        failureCause = v.result.cause();
                    }
                }
            }

            if (startedResult.isPresent()) {
                if (success) {
                    startedResult.get().setResult(null);
                }
                else {
                    startedResult.get().setFailure(failureCause);
                }
            }
            if (success) {
                logDeployment();
            }
            reset();
        }
    }

    private void logDeployment() {
        final StringBuilder stringBuilder = new StringBuilder(1024).append(String.format("%n%n"));

        stringBuilder.append(String.format("                instances  worker %n"));

        for (VerticleDescriptor d: verticles) {
            stringBuilder.append(String.format("    VERTICLE    %-9d  %-6s (%s)%n",
                    d.instances,
                    d.isWorker ? "yes" : "",
                    d.verticle.getCanonicalName()));
        }

        logger.info("verticles = {}", stringBuilder.toString());
    }

    protected class VerticleDescriptor {

        private boolean isWorker;
        private int instances;
        private Class<? extends VerticleBase> verticle;
        private JsonObject config;
        private AsyncResult<String> result;

        protected VerticleDescriptor(Class<? extends VerticleBase> verticle, int instances, JsonObject config, boolean isWorker) {
            this.verticle = verticle;
            this.instances = instances;
            this.config = config;
            this.isWorker = isWorker;
            this.result = null;
        }

        boolean complete() {
            return result != null;
        }

        boolean succeeded() {
            return complete() && result.succeeded();
        }

        void deploy(PlatformManager pm) {

            if (config == null) config = new JsonObject();

            AsyncResultHandler<String> handler = new AsyncResultHandler<String>() {
                @Override
                public void handle(AsyncResult<String> event) {
                VerticleDescriptor.this.result = event;
                deployComplete(VerticleDescriptor.this);
                }
            };

            if (isWorker) {
                pm.deployWorkerVerticle(false, verticle.getCanonicalName(), config, new URL[0], instances, null, handler);
            }
            else {
                pm.deployVerticle(verticle.getCanonicalName(), config, new URL[0], instances, null, handler);
            }

        }

    }


}
