package org.humanityx.scrape.api.resource;

import io.swagger.annotations.Api;
import org.humanityx.scrape.api.Config;
import org.humanityx.scrape.api.model.InsertTask;
import org.humanityx.scrape.api.model.Source;
import org.humanityx.util.Hash;
import org.humanityx.util.PageCache;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("/doc")
@Api("/doc")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class DocumentResource {

    protected final Logger logger = LoggerFactory.getLogger(DocumentResource.class);

    protected final Config config;

    protected final PageCache pageCache;


    public DocumentResource(Config config) {
        this.config = config;
        this.pageCache = config.getPageCache();
    }


    @GET
    @Path("/md5/{md5}")
    public String getByMD5(@PathParam("md5") String md5){
        return pageCache.getByMd5(md5);
    }

    @GET
    @Path("/url/{url}")
    public String getByUrl(@PathParam("url") String url){
        return pageCache.get(url);
    }

    @GET
    @Path("/md5/{url}/text")
    @Produces({MediaType.TEXT_PLAIN})
    public String getText(@PathParam("url") String url) {
        return Jsoup.parse(pageCache.getByMd5(url)).text();
    }


    @POST
    @Path("/url")
    public List<Source> add(@Valid InsertTask insertTask) throws IOException {
        if(insertTask.isCrawl()){
            return crawl(insertTask);
        } else {
            return Collections.singletonList(addPage(insertTask.getSource(), null, insertTask.getExcludeHtmlElements()));
        }
    }


    protected List<Source> crawl(@Valid InsertTask insertTask) throws IOException {
        Source source = insertTask.getSource();

        final int docType = source.getType();
        final List<Source> result = Collections.synchronizedList(new ArrayList<>());
        final Source base;

        logger.warn("Start crawling {}", source.getUrl());

        try {
            base = addPage(source, null, insertTask.getExcludeHtmlElements());
        } catch (Exception e) {
            logger.warn("Could not crawl " + source.getUrl(), e);
            return result;
        }

        config.getCrawler().crawlDocuments(source.getUrl(), document -> {
            String url = document.location();
            try {
                Source src = new Source(url);
                src.setType(docType);
                src.setBaseId(base.getId());
                result.add(addPage(src, document, insertTask.getExcludeHtmlElements()));
            } catch (Exception e) {
                logger.warn("Could not crawl " + url, e);
            }
        }, insertTask.getLimit());
        logger.warn("Stop crawling: {} pages indexed for {}", result.size(), source.getUrl());
        return result;
    }

    protected Source addPage(Source source, org.jsoup.nodes.Document document, List<String> excludeHtmlElements) throws IOException {
        String url = source.getUrl();
        if (url == null) {
            return null;
        }

        // set md5
        if (source.getMd5() == null) {
            source.setMd5(Hash.md5(url));
        }
        // set insert date
        if (source.getCreated() == null) {
            source.setCreated(new DateTime());
        }

        // crawler will update page cache
        document = config.getCrawler().getDocument(url);

        // todo: store source object in db

        // if already existing, don't add again
        /*if (!pageCache.contains(url)) {
            if (document == null) {
                document = config.getCrawler().getDocument(url);
            }

            pageCache.set(url, document.outerHtml());
        }*/

        return source;
    }



}
