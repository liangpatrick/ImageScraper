package com.eulerity.hackathon.imagefinder;

import crawlercommons.robots.BaseRobotRules;
import lombok.extern.java.Log;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.eulerity.hackathon.imagefinder.JsoupHelperMethods.connect;

@Log
public class UrlCrawler implements Callable<Set<String>> {
    private final String url;
    private final BaseRobotRules rules;
    private final Set<String> validUrls;
    private final Set<String> visitedUrls;
    private final Set<String> currValidUrls;

    //For testing purposes only
    public UrlCrawler() {
        this.url = null;
        this.rules = null;
        this.validUrls = null;
        this.visitedUrls = null;
        this.currValidUrls = null;
    }
    public UrlCrawler(String url, BaseRobotRules rules, Set<String> validUrls, Set<String> visitedUrls) {
        this.url = url;
        this.rules = rules;
        this.validUrls = validUrls;
        this.visitedUrls = visitedUrls;
        this.currValidUrls = new HashSet<>();
    }

    public UrlCrawler createUrlCrawler(String url, BaseRobotRules rules, Set<String> validUrls, Set<String> visitedUrls) {
        return new UrlCrawler(url, rules, validUrls, visitedUrls);
    }

    @Override
    public Set<String> call() {
        findSubUrls(url, rules);
        return currValidUrls;
    }

    /**
     * Crawls through all pages recursively
     *
     * @param url   Current url being parsed
     * @param rules Parsed robots.txt rules
     */
    private void findSubUrls(String url, BaseRobotRules rules) {
        if (visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);
        Document document = connect(url);
        if (document == null) {
            return;
        }
        currValidUrls.add(url);
        validUrls.add(url);
        log.info("Added valid URL: " + url);
        // Extract all links (a tags) from the document
        Elements elements = document.select("a[href]");
        // Iterate through the links and print the sub-URLs
        for (Element e : elements) {
            String tempUrl = e.attr("abs:href");
            if (isSubUrl(url, tempUrl) && rules.isAllowed(tempUrl)) {
                findSubUrls(tempUrl, rules);
            }
        }
    }

    /**
     * Helper method to verify url is part of domain
     *
     * @param url     original URL
     * @param tempUrl subUrl that needs to be verified
     * @return true if is url is part of domain
     */
    private boolean isSubUrl(String url, String tempUrl) {
        try {
            URL tempUrlObj = new URL(tempUrl);
            String tempUrlHost = tempUrlObj.getHost();
            URL urlObj = new URL(url);
            String urlHost = urlObj.getHost();
            return tempUrlHost != null && (tempUrlHost.endsWith("." + urlHost) || tempUrlHost.equals(urlHost));

        } catch (MalformedURLException e) {
            log.info("URL Exception occurred when processing URL: " + url + " or tempURL: " + tempUrl + "; with exception: " + e.getMessage());
            return false;
        }
    }
}
