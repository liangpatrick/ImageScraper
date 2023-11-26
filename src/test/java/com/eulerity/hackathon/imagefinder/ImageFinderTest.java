package com.eulerity.hackathon.imagefinder;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ImageFinderTest {
    public ImageFinder imageFinder;
    public Set<String> imageUrls = new HashSet<>();

    @Test
    public void testImageFind() {
        // This url is confirmed to only have one image
        String url = "https://asdfjklsemicolon.com/";
        imageFinder = new ImageFinder(url, imageUrls);
        // Run the imageFind method
        imageFinder.run();

        // Assert that the image URL was added to the set
        assertTrue(imageUrls.contains("https://asdfjklsemicolon.com/ui/title_jkl.jpg"));
    }

    @Test
    public void testImageFindNoImage() {
        // No image
        String url = "https://example.com/";
        imageFinder = new ImageFinder(url, imageUrls);
        // Run the imageFind method
        imageFinder.run();

        // Assert that the image URL was added to the set
        assertEquals(0, imageUrls.size());
    }



}