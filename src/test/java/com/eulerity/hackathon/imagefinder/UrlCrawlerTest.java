package com.eulerity.hackathon.imagefinder;

import com.google.gson.Gson;
import crawlercommons.robots.BaseRobotRules;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
public class UrlCrawlerTest {

    @Test
    public void testUrlCrawler() {
        BaseRobotRules mockRules = mock(BaseRobotRules.class);
        String initialUrl = "http://example.com";
        Set<String> validUrls = new HashSet<>();
        Set<String> visitedUrls = new HashSet<>();

        UrlCrawler urlCrawler = new UrlCrawler(initialUrl, mockRules, validUrls, visitedUrls);
        Set<String> result = urlCrawler.call();
        verify(mockRules, times(1)).isAllowed(anyString());
        assertEquals(1, result.size()); // Assuming that the initial URL is valid
    }

}