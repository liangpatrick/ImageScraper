package com.eulerity.hackathon.imagefinder;

import com.google.gson.Gson;
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
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebCrawlerControllerTest {

    public HttpServletRequest request;
    public HttpServletResponse response;
    public StringWriter sw;
    public HttpSession session;
    public WebCrawler webCrawler;
    public Future future;
    public Set<Future<?>> requestFutures;
    public Iterator<Future<?>> futureIterator;

    @Before
    public void before() throws Exception {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        webCrawler = mock(WebCrawler.class);
        futureIterator = mock(Iterator.class);
        requestFutures = mock(ConcurrentHashMap.newKeySet().getClass());
        future = mock(Future.class);
        sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);
        when(request.getRequestURI()).thenReturn("/foo/foo/foo");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/foo/foo/foo"));
        when(request.getSession()).thenReturn(session);
        when(webCrawler.createWebCrawler(any())).thenReturn(webCrawler);
        when(webCrawler.getImageUrls()).thenReturn(new ArrayList<>(Arrays.asList(WebCrawlerController.testImages)));
        when(requestFutures.iterator()).thenReturn(futureIterator);
        when(futureIterator.hasNext()).thenReturn(true, false);
        when(futureIterator.next()).thenReturn(future);
        doNothing().when(webCrawler).run();
        session = mock(HttpSession.class);
    }


    @Test
    public void testProcessRequest() throws IOException, ExecutionException, InterruptedException {
        response.setContentType("text/json");
        String url = "https://google.com";
        new WebCrawlerController().processRequest(webCrawler, response, requestFutures, url);
        verify(webCrawler, times(1)).createWebCrawler(url);
        verify(webCrawler, times(1)).run();
        verify(future, times(1)).get();
        verify(requestFutures, times(1)).remove(any());
        verify(futureIterator, times(2)).hasNext();
        verify(futureIterator, times(1)).next();
        Assert.assertEquals(new Gson().toJson(webCrawler.getImageUrls()), sw.toString());

    }

}
