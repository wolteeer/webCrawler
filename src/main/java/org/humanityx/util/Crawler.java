package org.humanityx.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Retrieve text from a given url/website.
 * @author Arvid Halma
 * @version 9-5-2015 - 9:30
 */
public class Crawler {

    public final static int NICE = 0;

    public final static int THREAD_COUNT = 7;

    public final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.65 Safari/537.36";

    public final static int TIMEOUT = 10000;

    public final static Pattern IGNORE_SUFFIX_PATTERN = Pattern.compile(
            "\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|" +
                    "wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz|rdf)$", Pattern.CASE_INSENSITIVE);

    public final static Pattern SITEMAP_LOC_PATTERN = Pattern.compile("<loc>(.*?)</loc>");

    private PageCache pageCache;

    public Crawler() {
    }

    public Crawler(PageCache pageCache) {
        this.pageCache = pageCache;
    }

    public String text(String url) throws IOException {
        Document doc = getDocument(url);
        String title = doc.title();
        String body = doc.body().text();
        return title + ". " + body;
    }

    public Document getDocument(String url) throws IOException {
        if(pageCache != null){
            String html = pageCache.get(url);
            if(html != null){
                return Jsoup.parse(html, url);
            }
        }

        // not in cache, download
        Document document = Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIMEOUT).followRedirects(true).get();
        try {
            if(pageCache != null){
                pageCache.set(url, document.outerHtml());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    public String getHtml(String url) throws IOException {
        return getDocument(url).html();
    }

    public void download(URL url, File dest) throws IOException {
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(dest);
        fos.getChannel().transferFrom(rbc, 0, 1 << 24);
        fos.close();
    }

    public Map<String, String> sections(String url) throws IOException {
        return sections(getDocument(url), null);
    }

    /**
     * Retrieve text parts from different sections. Html elements can be blacklisted by using a list of excluded css selector patterns:
     * <h3>Selector overview</h3> 
     * <ul> 
     * <li><code>tagname</code>: find elements by tag, e.g. <code><a href="/apidocs/org/jsoup/select/Evaluator.CssNthEvaluator.html#a">a</a></code></li> 
     * <li><code>ns|tag</code>: find elements by tag in a namespace, e.g. <code>fb|name</code> finds <code>&lt;fb:name&gt;</code> elements</li> 
     * <li><code>#id</code>: find elements by ID, e.g. <code>#logo</code></li> 
     * <li><code>.class</code>: find elements by class name, e.g. <code>.masthead</code></li> 
     * <li><code>[attribute]</code>: elements with attribute, e.g. <code>[href]</code></li> 
     * <li><code>[^attr]</code>: elements with an attribute name prefix, e.g. <code>[^data-]</code> finds elements with HTML5 dataset attributes</li> 
     * <li><code>[attr=value]</code>: elements with attribute value, e.g. <code>[width=500]</code> (also quotable, like <code><a href="data-name=&quot;launch">sequence"</a></code>)</li> 
     * <li><code>[attr^=value]</code>, <code>[attr$=value]</code>, <code>[attr *=value]</code>: elements with attributes that start with, end with, or contain the value, e.g. <code>[href *=/path/]</code></li> 
     * <li><code>[attr~=regex]</code>: elements with attribute values that match the regular expression; e.g. <code>img[src~=(?i)\.(png|jpe?g)]</code></li> 
     * <li><code> *</code>: all elements, e.g. <code> *</code></li> 
     * </ul> 
     * <h3>Selector combinations</h3> 
     * <ul> 
     * <li><code>el#id</code>: elements with ID, e.g. <code>div#logo</code></li> 
     * <li><code>el.class</code>: elements with class, e.g. <code>div.masthead</code></li> 
     * <li><code>el[attr]</code>: elements with attribute, e.g. <code>a[href]</code></li> 
     * <li>Any combination, e.g. <code>a[href].highlight</code></li> 
     * <li><code>ancestor child</code>: child elements that descend from ancestor, e.g. <code>.body p</code> finds <code>p</code> elements anywhere under a block with class "body"</li> 
     * <li><code>parent &gt; child</code>: child elements that descend directly from parent, e.g. <code>div.content &gt; p</code> finds <code>p</code> elements; and <code>body &gt;  *</code> finds the direct children of the body tag</li> 
     * <li><code>siblingA + siblingB</code>: finds sibling B element immediately preceded by sibling A, e.g. <code>div.head + div</code></li> 
     * <li><code>siblingA ~ siblingX</code>: finds sibling X element preceded by sibling A, e.g. <code>h1 ~ p</code></li> 
     * <li><code>el, el, el</code>: group multiple selectors, find unique elements that match any of the selectors; e.g. <code>div.masthead, div.logo</code></li> 
     * </ul> 
     * <h3>Pseudo selectors</h3> 
     * <ul> 
     * <li><code>:lt(n)</code>: find elements whose sibling index (i.e. its position in the DOM tree relative to its parent) is less than <code>n</code>; e.g. <code>td:lt(3)</code></li> 
     * <li><code>:gt(n)</code>: find elements whose sibling index is greater than <code>n</code>; e.g. <code>div p:gt(2)</code></li> 
     * <li><code>:eq(n)</code>: find elements whose sibling index is equal to <code>n</code>; e.g. <code>form input:eq(1)</code></li> 
     * <li><code>:has(seletor)</code>: find elements that contain elements matching the selector; e.g. <code>div:has(p)</code></li> 
     * <li><code>:not(selector)</code>: find elements that do not match the selector; e.g. <code>div:not(.logo)</code></li> 
     * <li><code>:contains(text)</code>: find elements that contain the given text. The search is case-insensitive; e.g. <code>p:contains(jsoup)</code></li> 
     * <li><code>:containsOwn(text)</code>: find elements that directly contain the given text</li> 
     * <li><code>:matches(regex)</code>: find elements whose text matches the specified regular expression; e.g. <code>div:matches((?i)login)</code></li> 
     * <li><code>:matchesOwn(regex)</code>: find elements whose own text matches the specified regular expression</li> 
     * <li>Note that the above indexed pseudo-selectors are 0-based, that is, the first element is at index 0, the second at 1, etc</li> 
     * </ul> 
     * @param doc
     * @param excludeHtmlElements
     * @return
     * @throws IOException
     */
    public Map<String, String> sections(Document doc, List<String> excludeHtmlElements)  {
        if(excludeHtmlElements != null){
            for (String excludeHtmlElement : excludeHtmlElements) {
                Elements toExclude = doc.select(excludeHtmlElement);
                if(!toExclude.isEmpty()) {
//                    System.out.println("Exclude by rule '" + excludeHtmlElement + "' -> " + toExclude);
                    //System.out.println("Exclude by rule '" + excludeHtmlElement + "'");
                    toExclude.remove();
                }
            }
        }

        Map<String, String> result = new HashMap<>();
        String[] urlParts = urlSplit(doc.location());

        result.put("URLDOMAIN", urlParts[0]);
        result.put("URLPAGE", urlParts[1]);
        result.put("TITLE", doc.title());
        result.put("DESCRIPTION", getMetaTag(doc, "description"));
        result.put("KEYWORDS", getMetaTag(doc, "keywords"));
        result.put("H1", text(doc.select("h1")));
        result.put("H2", text(doc.select("h2")));
        result.put("H3", text(doc.select("h3")));
        Element body = doc.body();
        result.put("BODY", body != null ? body.text() : doc.text());
        return result;
    }

    private static String text(Elements elts){
        StringBuilder sb = new StringBuilder();
        for (Element element : elts) {
            if (sb.length() != 0)
                sb.append("\n");
            sb.append(element.text());
        }
        return sb.toString();
    }

    public String[] urlSplit(String url){
        if(url == null){
            return new String[]{"",""};
        }
        int firstSlashIx = url.indexOf("/", "https://".length());
        if(firstSlashIx > 0){
            return new String[]{url.substring(0, firstSlashIx), url.substring(firstSlashIx, url.length())};
        } else {
            return new String[]{url, ""};
        }
    }

    public String getMetaTag(Document document, String attr) {
        Elements elements = document.select("meta[name=" + attr + "]");
        for (Element element : elements) {
            final String s = element.attr("content");
            if (s != null) return s;
        }
        elements = document.select("meta[property=" + attr + "]");
        for (Element element : elements) {
            final String s = element.attr("content");
            if (s != null) return s;
        }
        return "";
    }

    public Set<URL> sitemapUrls(String url) {
        try {
            return sitemapUrls(new URL(url));
        } catch (MalformedURLException e) {
            return Collections.<URL>emptySet();
        }
    }

    @Deprecated
    public Set<URL> sitemapUrlPatterns(URL url) {
        Set<URL> result = new HashSet<>();
        try {
            if(!url.getFile().endsWith("xml")){
                // guess url
                url = new URL(url, "sitemap.xml");
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while((line = in.readLine()) != null) {
                Matcher matcher = RegexUtil.URL.matcher(line);
                while(matcher.find()){
                    result.add(new URL(matcher.group()));
                }
            }
            in.close();
        } catch (IOException ignored) {}
        return result;
    }

    public Set<URL> sitemapUrls(URL url) {
        Set<URL> result = Collections.synchronizedSet(new HashSet<>());
        try {
            if(!url.getFile().endsWith("xml") && !url.getFile().contains("sitemap")){
                // guess url
                url = new URL(url, "sitemap.xml");
            }

            final Document doc = Jsoup.connect(url.toString())
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .followRedirects(true)
                    .parser(Parser.xmlParser())
                    .get();

            Set<URL> linksToDo = new HashSet<>();
            for (Element loc : doc.select("loc")) {
                String link = loc.text().trim();
                if(link.endsWith("xml")){
                    linksToDo.add(new URL(link));
                } else {
                    result.add(new URL(link));
                }
            }

            Parallel.run(() -> linksToDo.stream().parallel().forEach(s -> result.addAll(sitemapUrls(s))), THREAD_COUNT);
        } catch (IOException ignored) {}
        return result;
    }

    public List<URL> disallowedUrls(String baseUrl) {
        try {
            return disallowedUrls(new URL(baseUrl));
        } catch (MalformedURLException e) {
            return Collections.<URL>emptyList();
        }
    }

    public List<URL> disallowedUrls(URL baseUrl) {
        List<URL> result = new ArrayList<>();
        try {
            URL robotURL = new URL(baseUrl, "robots.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(robotURL.openStream()));
            String line;
            while((line = in.readLine()) != null) {
                if(line.startsWith("Disallow:")){
                    line = line.substring(9).trim();
                    result.add(new URL(baseUrl, line));
                }
            }
            in.close();
        } catch (IOException ignored) {}
        return result;
    }


    public void crawlDocuments(String baseUrl) {
        crawlDocuments(baseUrl, doc -> System.out.println(doc.title()), -1);
    }


    public void crawlDocuments(String url, Consumer<Document> consumer, int limit) {
        if(limit < 0)
            limit = Integer.MAX_VALUE;
        Set<URL> siteMap = sitemapUrls(url);
        if(siteMap.isEmpty()){
            // scrape links
            if(url.endsWith("sitemap.xml") ){
                /** Do we need if the sitemap is dirty(HTTP error responses) **/ 
                url = url.substring(0, url.length() - 11);
            }
            Set<String> disallowed = Collections.synchronizedSet(disallowedUrls(url).stream().map(URL::toString).collect(Collectors.toSet()));            
            crawlDocumentsFollowLinks(url, disallowed, consumer, limit);
        } else {
            crawlDocumentsForSiteMapUrls(siteMap, consumer, limit);
        }
    }


    public void crawlDocumentsFollowLinks(final String url, final Set<String> disallowed, final Consumer<Document> consumer, int limit) {
        ForkJoinPool pool = new ForkJoinPool(THREAD_COUNT);
        disallowed.add(url); // will be added first time any way
        pool.invoke(new CrawlDocumentTask(url, url, disallowed, consumer, new AtomicInteger(0), limit));
    }

    private class CrawlDocumentTask extends RecursiveAction {
        final String baseUrl;
        final String url;
        final Set<String> disallowed;
        final Consumer<Document> consumer;
        final int limit;
        final AtomicInteger nr;

        /**
         * Crawl a page and spawn new CrawlTasks resulting from links found on the page.
         * @param url target location
         * @param disallowed disallowed means only disallowed for urls resulting from 'url'. url itself will be visited anyway.
         * @param consumer what to do when an url is visited (will be called with 'url' as argument for sure)
         */
        public CrawlDocumentTask(String baseUrl, String url, Set<String> disallowed, Consumer<Document> consumer, AtomicInteger nr, int limit) {
            this.baseUrl = baseUrl;
            this.url = url;
            this.disallowed = disallowed;
            this.consumer = consumer;
            this.nr = nr;
            this.limit = limit < 0 ? Integer.MAX_VALUE : limit;
        }

        @Override
        protected void compute() {
            try {
                List<CrawlDocumentTask> todo = new ArrayList<>();
                if(url.endsWith("xml") || url.contains("xml")){
                    // assume it is a sitemap
                    Set<URL> links = sitemapUrls(url);
                    for (URL link : links) {
                        String absLink = link.toString();
                        if(ignoreLink(absLink, baseUrl)){
                            continue;
                        }

                        if(disallowed.contains(absLink)){
                            continue; // don't follow blacklisted links
                        }

                        if(nr.incrementAndGet() >= limit){
                            break;
                        }

                        disallowed.add(absLink);
                        todo.add(new CrawlDocumentTask(baseUrl, absLink, disallowed, consumer, nr, limit));
                    }

                } else {
                    // assume html
                    final Document doc = getDocument(url);
                    consumer.accept(doc);

                    final List<String> absLinks = new ArrayList<>();
                    // add links
                    final Elements links = doc.select("a[href]");
                    for(Element link: links){
                        absLinks.add(link.attr("abs:href"));
                    }
                    // add frames
                    final Elements frames = doc.select("frame");
                    for(Element frame: frames) {
                        absLinks.add(frame.absUrl("src"));

                    }

                    for(String absLink : absLinks){
                        // filter out unwanted links
                        if(ignoreLink(absLink, baseUrl)){
                            continue;
                        }

                        if(disallowed.contains(absLink)){
                            continue; // don't follow blacklisted links
                        }

                        if(nr.incrementAndGet() >= limit){
                            break;
                        }

                        disallowed.add(absLink);
                        todo.add(new CrawlDocumentTask(baseUrl, absLink, disallowed, consumer, nr, limit));
                    }

                }

                try {
                    Thread.sleep(NICE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                invokeAll(todo);

            } catch (IOException ignored) {

            }
        }

        @Override
        public String toString() {
            return "CrawlDocumentTask{" +
                    "url='" + url + '\'' +
                    '}';
        }
    }

    public boolean ignoreLink(String url, String baseUrl){
        if(url == null){
            return true;
        }

        if(url.equals(baseUrl)) {
            return true; // don't go to yourself
        }

        if(!url.startsWith(baseUrl)) {
            return true; // don't go to external site
        }

        if(url.contains("#")) {
            return true; // don't follow anchor links
        }

        if(IGNORE_SUFFIX_PATTERN.matcher(url).find()){
            return true; // don't download files
        }

        return false;

    }

    public void crawlUrlsForSiteMapStrings(Collection<String> urls, Consumer<String> consumer, int limit) {
        Parallel.run(() -> urls.stream().parallel().limit(limit).forEach(consumer), THREAD_COUNT) ;
    }

    public void crawlUrlsForSiteMapUrls(Collection<URL> urls, Consumer<String> consumer, int limit) {
        crawlUrlsForSiteMapStrings(urls.stream().map(URL::toString).collect(Collectors.toList()), consumer, limit);
    }

    
    /**  Check if founded URL's do not contain sitemap as well **/ 
    public void crawlDocumentsForSiteMapStrings(Collection<String> urls, Consumer<Document> consumer, int limit) {
        if(limit < 0){
            limit = Integer.MAX_VALUE;
        }
        
        final int finalLimit = limit;
        
        Parallel.run(() -> urls.stream().parallel().limit(finalLimit).forEach(
                        /** Check if sitemap occurs in the URL, if so recursive 
                         call crawlDocumentsForSiteMapUrls **/
                        url -> {
                            if(url.contains("sitemap.xml")){
                                Set<URL> siteMap = sitemapUrls(url);                                
                                crawlDocumentsForSiteMapUrls(siteMap, consumer,finalLimit);
                            } 
                            else{
                                try {
                                    Document doc = getDocument(url);
                                    consumer.accept(doc);
                                    Thread.sleep(NICE);
                                } catch (IOException | InterruptedException ignored) {
                                }
                            }
                        }),
                THREAD_COUNT) ;
    }

    public void crawlDocumentsForSiteMapUrls(Collection<URL> urls, Consumer<Document> consumer, int limit) {
        crawlDocumentsForSiteMapStrings(urls.stream().map(URL::toString).collect(Collectors.toList()), consumer, limit);
    }



}
