package com.eulerity.hackathon.imagefinder;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import lombok.extern.java.Log;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static com.eulerity.hackathon.imagefinder.JsoupHelperMethods.getResponse;

@Log
public class WebCrawler implements Runnable {
    private static final int THREAD_POOL = 50;
    private final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_POOL);
    public BaseRobotRules rules;
    private String url;
    private String robotUrl;
    private final Set<String> imageUrls = ConcurrentHashMap.newKeySet();
    private static final int SLEEP_MS = 60000;

    /**
     * Used for testing purposes
     */
    public WebCrawler() {
    }

    /**
     * Constructor used to set fields
     *
     * @param url Verified URL passed in from WebCrawlerController
     */
    public WebCrawler(String url) {
        this.url = url;
        this.robotUrl = this.url + "/robots.txt";
    }

    @Override
    public void run() {
        crawler();
    }

    /**
     * Main Crawler method
     */
    private void crawler() {
        log.info("Entering with url: " + url);

        rules = parseRules();
        Set<Future<Set<String>>> subUrlFutures = new HashSet<>();
        ArrayList<String> subUrls = allSubUrls(new UrlCrawler(), subUrlFutures, url, rules, SLEEP_MS);
        log.info("subUrls found: " + subUrls.size());
        Set<Future<?>> imageFutures = new HashSet<>();

        // Goes through all subUrls found sequentially and finds images
        try {
            imageFutures.add(executorService.submit(new ImageFinder(url, imageUrls)));
            for (Future<?> ignored : imageFutures) {
                for (String subUrl : subUrls) {
                    imageFutures.add(executorService.submit(new ImageFinder(subUrl, imageUrls)));
                }
            }
        } finally {
            // Wait for all tasks to complete
            for (Future<?> future : imageFutures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.info("Exception: " + e.getMessage() + " occurred when retrieving Future<?> for imageFutures");
                }
            }
            executorService.shutdown();
        }

    }

    protected BaseRobotRules parseRules() {
        SimpleRobotRules tempRules;
        Response response = getResponse(robotUrl);
        // This sets the rules for the domain
        if (response == null) {
            tempRules = new SimpleRobotRules(SimpleRobotRules.RobotRulesMode.ALLOW_ALL);
        } else {
            tempRules = new SimpleRobotRulesParser().parseContent(robotUrl, response.bodyAsBytes(), "text/plain", "mozilla");
        }

        return tempRules;
    }

    protected ArrayList<String> allSubUrls(UrlCrawler urlCrawler, Set<Future<Set<String>>> subUrlFutures, String url, BaseRobotRules rules, int SLEEP_MS) {
        Set<String> validUrls = ConcurrentHashMap.newKeySet();
        Set<String> visitedUrls = ConcurrentHashMap.newKeySet();

        // crawls through all urls
        Set<String> tempUrls;
        subUrlFutures.add(executorService.submit(urlCrawler.createUrlCrawler(url, rules, validUrls, visitedUrls)));

        // Wait for all tasks to complete
        for (Future<Set<String>> future : subUrlFutures) {
            try {
                tempUrls = future.get();
                for (String tempUrl : tempUrls) {
                    subUrlFutures.add(executorService.submit(urlCrawler.createUrlCrawler(tempUrl, rules, validUrls, visitedUrls)));
                }
            } catch (InterruptedException | ExecutionException e) {
                log.info("Exception: " + e.getMessage() + " occurred when retrieving Future<Set<String>> for subUrlFutures");
            }
        }

        // Safety net in case a UrlCrawler object hasn't finished crawling
        while (executorService.getActiveCount() > 0) {
            try {
                log.info("Sleeping for " + SLEEP_MS + " ms until all subUrls have been found");
                Thread.sleep(SLEEP_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return new ArrayList<>(validUrls);
    }


    /**
     * Retrieves imageUrls
     *
     * @return imageUrls
     */
    public ArrayList<String> getImageUrls() {
        return new ArrayList<>(imageUrls);
    }

    /**
     * Returns new WebCrawler object
     *
     * @param url Passed in URL
     * @return new WebCrawler object
     */
    protected WebCrawler createWebCrawler(String url) {
        return new WebCrawler(url);
    }
}
