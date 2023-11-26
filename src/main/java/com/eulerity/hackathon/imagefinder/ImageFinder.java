package com.eulerity.hackathon.imagefinder;

import lombok.extern.java.Log;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Set;

import static com.eulerity.hackathon.imagefinder.JsoupHelperMethods.connect;

@Log
public class ImageFinder implements Runnable {

    private final String subUrl;
    private final Set<String> imageUrls;

    public ImageFinder(String subUrl, Set<String> imageUrls) {
        this.subUrl = subUrl;
        this.imageUrls = imageUrls;
    }

    @Override
    public void run() {
        imageFind();
    }

    /**
     * Attempts to find all images on webpage
     */
    private void imageFind() {
        Document document = connect(subUrl);
        if (document == null) {
            return;
        }
        Elements images = document.select("img");
        for (Element img : images) {
            String tempImg = img.attr("abs:src");
            if (tempImg != null && !imageUrls.contains(tempImg)) {
                log.info("Added image: " + tempImg);
                imageUrls.add(tempImg);
            }
        }
    }


}
