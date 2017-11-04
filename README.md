# HxScrape #

Web scraping service.

Humanity-x.org - Centre for Innovation - Leiden University

## Getting started ##

* Install Java 1.8+
* Run maven build
* In the target directory you'll find: `scrape-x.y.z.jar`
* Run: java -jar scrape-x.y.z.jar server ../settings.yml
* Browse to: http://localhost:1337/


## Fixes ##
* Sitemap issues 11-04-2017 Since it is the first commit changes per function will be notated: function crawlDocumentsForSiteMapStrings() in crawler.java, changed forloop so it will recursivly call itself when a deeper sitemap(sitemap.xml?page=1) it will treat it as sitemap not as webpage. In line 461 in crawler.java there is added an extra requirement, not all sitemaps end with xml.  


