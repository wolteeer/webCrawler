package org.humanityx.scrape.api;

import com.codahale.metrics.health.HealthCheck;

/**
 * System enviroment health checks
 * @author Arvid Halma
 * @version 12-10-17
 */
public class Health extends HealthCheck {

    public Health() {
    }

    @Override
    protected Result check() throws Exception {
        //todo: check data dirs etc
        return Result.healthy();
    }
}