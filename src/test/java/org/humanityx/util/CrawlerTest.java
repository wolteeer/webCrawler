package org.humanityx.util;

import org.humanityx.scrape.api.model.InsertTask;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test http://www.denhaag.nl/
 * 1 Threads: Crawled 375 pages in 421.9 seconds
 * 32 Threads: Crawled 297 pages in 18.8 seconds
 * 128 Threads: Crawled 259 pages in 4.8 seconds
 * @author Arvid
 * @version 25-5-2015 - 11:43
 */
public class CrawlerTest {
    public static void main(String[] args) throws IOException {
//        System.out.println("Crawler.disallowedUrls(http://www.google.com) = " + Crawler.disallowedUrls("http://www.google.com"));
//        System.out.println("Crawler.sitemapUrls(\"https://bto.bluecoat.com/sitemap.xml\") = " + Crawler.sitemapUrls("https://bto.bluecoat.com/sitemap.xml"));
//        Crawler.crawl("http://www.robomind.net/nl");

//        String url = "http://www.handleidinghtml.nl";
//        String url = "https://docs.oracle.com/javase/8/docs/api/";
//        String url = "https://docs.oracle.com/javase/8/docs/api/overview-summary.html";
//        String url = "https://www.xs4all.nl/";
//        String url = "http://www.robomind.net/nl";
//        String url = "http://www.dropwizard.io/";
//        String url = "http://www.dropwizard.io/manual/";
//        String url = "http://www.denhaag.nl";
//        String url = "http://www.dropwizard.io/";
//        String url = "http://www.nu.nl";
//        String url = "https://www.robomindacademy.com/";
//        String url = "https://www.degoudenton.nl/wijnaanbod/type/olijfolie-azijn.html";
//        String url = "http://mens-en-gezondheid.infonu.nl/aandoeningen/103394-slaapstoornissen.html";
//        String url = "https://www.xs4all.nl/";
//        String url = "https://en.wikipedia.org/wiki/Main_Page";
        String url = "http://www.irinnews.org/";
        AtomicInteger i = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        Crawler crawler = new Crawler();
        crawler.crawlDocuments(url, document -> {
            System.out.println("site = " + document.location());
            i.addAndGet(1);
            Map<String, String> sections = crawler.sections(document, InsertTask.DEFAULT_ELEMENTS_TO_EXCLUDE);
            for (Map.Entry<String, String> en : sections.entrySet()) {
//                System.out.printf("  %s \t= %s\n", en.getKey(), en.getValue());
            }

        }, -1);

        double stop = System.currentTimeMillis();
        System.out.format("Crawled %d pages in %.1f seconds\n", i.get(), (stop - start)/1000.0);


        /*i.set(0);
        start = System.currentTimeMillis();
        crawler.crawlDocuments(url, d -> {
            System.out.println("d = " + d.title());
            i.addAndGet(1);

        }, 10);
        stop = System.currentTimeMillis();
        System.out.format("Crawled %d pages in %.1f seconds\n", i.get(), (stop - start)/1000.0);

        System.out.println("sections:" );
        for (Map.Entry<SectionDAO.Section, String> entry : crawler.sections("http://robomind.net/nl/edSecondary.html").entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }*/
    }
}
