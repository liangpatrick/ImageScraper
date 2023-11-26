package com.eulerity.hackathon.imagefinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.java.Log;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Log
@WebServlet(name = "ImageFinder", urlPatterns = {"/main"})
public class WebCrawlerController extends HttpServlet {
    //This is just a test array
    public static final String[] testImages = {"https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny", "https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny", "https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny", "https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"};
    private static final long serialVersionUID = 1L;
    private static final Gson GSON = new GsonBuilder().create();
    private final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    private final Set<Future<?>> requestFutures = ConcurrentHashMap.newKeySet();
    private WebCrawler webCrawler;


    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/json");
        String path = req.getServletPath();
        String url = req.getParameter("url");
        log.info("Got request of:" + path + " with query param:" + url);
        addShutdownHook();
        // processesRequest
        try {
            processRequest(null, resp, requestFutures, url);
        } catch (Exception e) {
            log.info("Exception: " + e + " occurred when processing url: " + url);
        }
//        finally {
//            handleFutures(resp, requestFutures);
//        }
    }

    /**
     * Shutdown Hook to gracefully shut down ExecutorService
     */
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdownNow));
    }

    /**
     * Method processes request, sends it to be crawled, and then waits until all images are retrieved.
     *
     * @param webCrawler     Param used for testing purposes
     * @param resp           Used to send imageUrls to webpage
     * @param requestFutures Passed in for OOP principles
     * @param url            The url to be crawled
     * @throws IOException
     */
    protected void processRequest(WebCrawler webCrawler, HttpServletResponse resp, Set<Future<?>> requestFutures, String url) throws IOException {
        // This logic is for testing purposes only
        if (webCrawler != null) {
            this.webCrawler = webCrawler.createWebCrawler(processUrl(url));
        } else {
            this.webCrawler = new WebCrawler().createWebCrawler(processUrl(url));
        }
        requestFutures.add(executorService.submit(this.webCrawler));
        handleFutures(resp, requestFutures);

    }

    protected static String processUrl(String url) {
        // Verifies URL
        try {
            URL urlObj = new URL(url);
            return urlObj.getProtocol() + "://" + urlObj.getHost() + (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");

        } catch (MalformedURLException e) {
            log.info("MalformedURL Exception: " + e.getMessage() + " when processing URL: " + url);
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles futures returned by multiple requests
     *
     * @param resp           Used to send imageUrls to webpage
     * @param requestFutures Passed in for OOP principles
     * @throws IOException
     */
    protected void handleFutures(HttpServletResponse resp, Set<Future<?>> requestFutures) throws IOException {
        for (Future<?> future : requestFutures) {
            try {
                future.get();
                log.info("Returning all found images; # of images: " + webCrawler.getImageUrls().size());
                resp.getWriter().print(GSON.toJson(webCrawler.getImageUrls()));
            } catch (InterruptedException | ExecutionException e) {
                log.info("Exception: " + e + " occurred when retrieving future");
            } finally {
                // Removed so that multiple Futures aren't continually iterated through
                requestFutures.remove(future);
            }
        }
    }


}
