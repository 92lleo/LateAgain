package io.kuenzler.android.lateagain.control.crawler;

import io.kuenzler.android.lateagain.control.RequestLoop;

/**
 * @author Leonhard Künzler
 * @version 0.1
 * @date 17.02.2016
 * //TODO: Work with RegExes here, read from file or sth like that, choosing automatically by now
 */
public class GenericCrawler {

    private final static int NO_NET = 0;
    private final static int DB = 1;
    private final static int MVG = 2;

    private GenericCrawler() {
    }

    public static Crawler getCrawler(RequestLoop requestLoop) {
        int currentTransportNet = 0;
        //currentTransportNet = requestLoop.getCurrentTransportNet();
        switch (currentTransportNet) {
            case NO_NET:
                return new DBCrawler(requestLoop);
            case DB:
                return new DBCrawler(requestLoop);
            case MVG:
                return new MVGCrawler(requestLoop);
            default:
                return new DBCrawler(requestLoop);
        }
    }
}
