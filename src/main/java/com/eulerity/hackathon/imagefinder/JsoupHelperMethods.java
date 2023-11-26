package com.eulerity.hackathon.imagefinder;

import lombok.extern.java.Log;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

@Log
public class JsoupHelperMethods {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36";


    /**
     * Wrapper method to retrieve Document
     *
     * @param url Verified URL
     * @return Returns document retrieved through Jsoup
     */
    public static Document connect(String url) {
        return getDocument(getResponse(url), url);
    }

    /**
     * Helper method to connect to url using Jsoup and receive Response from webpage
     *
     * @return Response object
     */
    public static Connection.Response getResponse(String url) {
        try {
            return Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent(USER_AGENT)
                    .referrer("http://www.google.com")
                    .timeout(12000)
                    .followRedirects(true)
                    .execute();
        } catch (IOException e) {
            log.info("Response error for URL: " + url + "; Message: " + e.getMessage());
        }
        return null;

    }

    /**
     * Retrieves Document parsed from getResponse()
     *
     * @param response Response object obtained from Jsoup
     * @param url      Verified URL
     * @return Document when successful, otherwise return null;
     */
    private static Document getDocument(Connection.Response response, String url) {
        if (response != null && 200 == response.statusCode()) {
            try {
                return response.parse();
            } catch (IOException e) {
                log.info("Document Parse error for URL: " + url + "; Message: " + e.getMessage());
                return null;
            }
        } else if (response != null) {
            System.out.printf("%s is unavailable; Status code: %d%n", url, response.statusCode());
            return null;
        } else {
            log.info("No response from page: " + url);
            return null;
        }
    }
}
