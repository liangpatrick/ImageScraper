package com.eulerity.hackathon.imagefinder;


import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import org.jsoup.Connection;
import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.eulerity.hackathon.imagefinder.JsoupHelperMethods.getResponse;
import static com.eulerity.hackathon.imagefinder.WebCrawlerController.processUrl;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebCrawlerTest {
    @Test
    public void testParseRulesNoRobots() {
        String url = "https://asdfjklsemicolon.com/";
        WebCrawler webCrawler = new WebCrawler(processUrl(url));
        BaseRobotRules rules = webCrawler.parseRules();
        assertEquals(new SimpleRobotRules(SimpleRobotRules.RobotRulesMode.ALLOW_ALL), rules);
    }

    @Test
    public void testParseRules() {
        String url = "https://sleeper.com/";
        String robotUrl = url + "/robots.txt";
        WebCrawler webCrawler = new WebCrawler(processUrl(url));
        BaseRobotRules rules = webCrawler.parseRules();
        Connection.Response response = getResponse(robotUrl);
        assertEquals(new SimpleRobotRulesParser().parseContent(robotUrl, response.bodyAsBytes(), "text/plain", "mozilla"), rules);
    }

    @Test
    public void testAllSubUrls() throws ExecutionException, InterruptedException {
        String url = "https://sleeper.com/";
        WebCrawler webCrawler = new WebCrawler(processUrl(url));
        UrlCrawler rootUrlCrawler = mock(UrlCrawler.class);
        UrlCrawler urlCrawler = mock(UrlCrawler.class);
        BaseRobotRules mockRules = mock(BaseRobotRules.class);
        Iterator<Future<Set<String>>> futureIterator = mock(Iterator.class);
        Set<Future<Set<String>>> subUrlFutures = mock(ConcurrentHashMap.newKeySet().getClass());
        Future<Set<String>> future = mock(Future.class);

        Iterator<String> stringIterator = mock(Iterator.class);
        Set<String> tempUrls = mock(ConcurrentHashMap.newKeySet().getClass());
        Set<String> tempSet = mock(HashSet.class);

        when(subUrlFutures.iterator()).thenReturn(futureIterator);
        when(futureIterator.hasNext()).thenReturn(true, false);
        when(futureIterator.next()).thenReturn(future);
        when(mockRules.isAllowed(any())).thenReturn(true);
        when(rootUrlCrawler.createUrlCrawler(any(), any(), any(), any())).thenReturn(urlCrawler);
        when(future.get()).thenReturn(tempUrls);
        when(tempUrls.iterator()).thenReturn(stringIterator);
        when(stringIterator.hasNext()).thenReturn(true, false);
        when(stringIterator.next()).thenReturn("asdf");
        when(urlCrawler.call()).thenReturn(tempSet);
        webCrawler.allSubUrls(rootUrlCrawler, subUrlFutures, url, mockRules, 1);

        verify(future, times(1)).get();
        verify(subUrlFutures, times(2)).add(any());
        verify(futureIterator, times(2)).hasNext();
        verify(futureIterator, times(1)).next();

        verify(stringIterator, times(2)).hasNext();
        verify(stringIterator, times(1)).next();

    }

}