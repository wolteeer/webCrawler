package org.humanityx.scrape.api;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.humanityx.scrape.api.resource.DocumentResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * The Application class pulls together the various bundles and commands which provide basic functionality.
 * @author Arvid Halma
 */
public class App extends Application<Config> {
    private final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public String getName() {
        return "hxscrape";
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {

        // Serve static content
        bootstrap.addBundle(new AssetsBundle("/assets", "/", "index.html"));

        // Swagger API docs
        bootstrap.addBundle(new SwaggerBundle<Config>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(Config configuration) {
                return configuration.getSwaggerBundleConfiguration();
            }
        });
    }

    @Override
    public void run(Config configuration, Environment environment) {

        // ensure pagecache dir
        String pageCacheDir = configuration.getPageCacheDir();
        if(pageCacheDir != null && !pageCacheDir.isEmpty()) {
            new File(pageCacheDir).mkdirs();
        }

        final Health healthCheck = new Health();
        environment.healthChecks().register("system", healthCheck);

        // resources
        final DocumentResource documentResource = new DocumentResource(configuration);
        environment.jersey().register(documentResource);

        if(configuration.prettyPrintJsonResponse) {
            environment.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        }


        logger.warn("HxScrape application started...");

    }

}