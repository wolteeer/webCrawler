package org.humanityx.scrape.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.humanityx.util.Crawler;
import org.humanityx.util.PageCache;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.File;

/**
 * The Configuration class which specifies environment-specific parameters.
 * These parameters are specified in a YAML configuration file which is deserialized
 * to an instance of your application's configuration class and validated.
 *
 * Docs:
 * https://dropwizard.github.io/dropwizard/manual/configuration.html
 *
 * @author Arvid Halma
 * @version 9-4-2015 - 20:51
 */
public class Config extends Configuration {
    private final Logger logger = LoggerFactory.getLogger(Config.class);

    @NotNull
    @JsonProperty
    private String pageCacheDir;

    @JsonProperty("swagger")
    private SwaggerBundleConfiguration swaggerBundleConfiguration;

    @JsonProperty
    public boolean prettyPrintJsonResponse;

    @JsonIgnore
    private PageCache pageCache;

    @JsonIgnore
    private Crawler crawler;

    public String getPageCacheDir() {
        return pageCacheDir;
    }

    public SwaggerBundleConfiguration getSwaggerBundleConfiguration() {
        return swaggerBundleConfiguration;
    }

    public PageCache getPageCache(){
        if(pageCache == null){
            pageCache = new PageCache(new File(pageCacheDir), 2, 2);
        }
        return pageCache;
    }

    public Crawler getCrawler(){
        if(crawler == null){
            crawler = new Crawler(getPageCache());
        }
        return crawler;
    }

}