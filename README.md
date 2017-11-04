# webCrawler

** Crawler which allows you to crawl any site**

Fixes 11-4-2017: 
  Sitemap issues:
     if the sitemap contained redirections to deeper sitemaps, e.g. sitemap.xml?page=1 the crawler recognized the deeper sitemap as normal
     webpage. Since this is the first commit I've added changes in the README.md
     Changed the for-loop in function crawlDocumentsForSiteMapStrings(), line 461, so it recognize sitemaps redirects in a sitemap
     Changed the if-statement on line 237, sitemap.xml?page=1 never ended on xml, so added !url.getFile().contains("sitemap") as extra 
     rule.
