package io.kuenzler.android.lateagain.control;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;

import io.kuenzler.android.lateagain.model.Departure;

/**
 * @author Leonhard Künzler
 * @version 0.2
 * @date 03.11.15 01:00
 */
public class Crawler2 {

    private final String mBahnUrl;
    private final RequestLoop reqLoop;
    private Document mBahn;
    private String date, time, start, dest;
    private ArrayList<Departure> departures;

    /**
     * Creates crawler object with corresponding loop given
     */
    public Crawler2(RequestLoop reqLoop) {
        this.reqLoop = reqLoop;
        mBahnUrl = "http://mobile.bahn.de/bin/mobil/bhftafel.exe/dox?country=DEU&rt=1&use_realtime_filter=1&webview=&";
        //setTestData(); // TODO delete when working with gui (getter & setter needed)
        //sendRequest();
        //cleanAndParseResults();
        // if (!departures.isEmpty()) {
        //     for (Departure d : departures) {
        //        System.out.println(d);
        //    }
        //   DateCalculator dc = new DateCalculator(this);
        //    dc.countToDeparture(departures.get(1));
        //}
    }

    /**
     * @param date
     * @param time
     * @param start
     * @param dest
     * @return
     */
    public ArrayList<Departure> getDepartures(String date, String time, String start, String dest) {
        this.start = start;
        this.dest = dest;
        this.date = date;
        this.time = time;
        //Todo
        setTestData();
        sendRequest();
        cleanAndParseResults();
        return departures;
    }

    /**
     * Testdata for use without gui
     */
    private void setTestData() {
        Date today = new Date();
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT,
                Locale.GERMANY);
        if (this.date == null) {
            date = formatter.format(today);
        }
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm", Locale.GERMANY);
        if (this.time == null) {
            time = ft.format(today);
        }
        //TODO start = "Eching";
        //dest = "Feldmoching";
        //System.out.println(date + "-" + time + ", " + start + " to " + dest);
    }

    /**
     * Cleans results and parses alternative start and dest locations, sets them
     * when possible
     */
    private boolean cleanAndParseAlternativeLocations() {
        boolean changed = true;
        while (mBahn == null) {
            //wait for request
        }
        if (!mBahn.title().contains("Ihre Anfrage")) {
            throw new NoSuchElementException(
                    "Did expect drop down dialog, found site " + mBahn.title());
        }

        // get alternatives for start (REQ0JourneyStopsS0K)
        Elements alternatives = mBahn.getElementsByAttributeValue("name",
                "REQ0JourneyStopsS0K");
        alternatives = alternatives.first().children();
        ArrayList<String> alternativeLocations = new ArrayList<String>();
        //TODO System.out.println("--Alternatives to " + start);
        for (Element x : alternatives) {
            System.out.println(x.text()); // TODO
            alternativeLocations.add(x.text());
        }
        if (!alternativeLocations.isEmpty()) {
            reqLoop.getAlternativeLocations(alternativeLocations, 0);
            //TODO start
            //TODO automative - start = getBestAlternative(start, alternativeLocations);
            //System.out.println("Start set to " + start);
        }
        /*
        // get alternatives for dest (REQ0JourneyStopsZ0K)
        alternatives = mBahn.getElementsByAttributeValue("name",
                "REQ0JourneyStopsZ0K");
        alternatives = alternatives.first().children();
        alternativeLocations = new ArrayList<String>();
        //System.out.println("--Alternatives to " + dest); // TODO
        for (Element x : alternatives) {
            System.out.println(x.text());
            alternativeLocations.add(x.text());
        }
        if (!alternativeLocations.isEmpty()) {
            reqLoop.getAlternativeLocations(alternativeLocations, 1);
            // TODO dest
            // dest = getBestAlternative(start, alternativeLocations);
            //System.out.println("Dest set to " + dest);
        }
        */
        return changed;
    }

    /**
     * Returns String with highest similarity to location string
     *
     * @param location     Location string
     * @param alternatives Alternative strings from db site
     * @return best matching string from array
     */
    private String getBestAlternative(String location, ArrayList<String> alternatives) {
        double currentDistance, distance = 0;
        String result = "";
        for (String s : alternatives) {
            currentDistance = StringSimilarity
                    .getSimilarityPercent(location, s);
            if (currentDistance > distance) {
                distance = currentDistance;
                result = s;
            }
        }
        return result;
    }

    /**
     * Cleans the document and parses departures. Expects Query to be sent.
     * Triggers search for alternative locations if unsuccessful
     */
    private void cleanAndParseResults() {
        while (mBahn == null) {
            //wait for request
        }
        if (!mBahn.title().contains("Abfahrt")) {
            if (cleanAndParseAlternativeLocations()) {
                departures = null;//TODO
                return;
            }
            mBahn = null;
            sendRequest();
            // throw new NoSuchElementException("No right results to parse");
        }
        while (mBahn == null) {
            //wait for request
        }
        Elements information = mBahn.select("div[class=fline stdpadding").get(0).children();
        String info = information.first().text();
        start = info.substring(0, info.indexOf(" - ")).trim();
        Log.i("LateAgainStart", start);
        reqLoop.setCorrectedLocations(start, dest);

        Elements departureList = mBahn.select("div[class=clicktable").get(0).children();

        departures = new ArrayList<>();
        for (Element departureTable : departureList) {
            Departure dep = new Departure();
            String delay = departureTable.select("span[class=okmsg").text();
            if (delay.trim().isEmpty()) {
                delay = "-0";
            }
            dep.setDelay(delay);
            Elements classBold = departureTable.select("span[class=bold");
            dep.setType(classBold.get(0).text().replace(" ", ""));
            dep.setTimeStart(classBold.get(1).text());
            String ownText = departureTable.ownText();
            String platform, target;
            if (ownText.contains("Gl. ")) {
                target = ownText.substring(2, ownText.length() - 8).trim();
                platform = ownText.substring(ownText.indexOf("Gl."));
            } else {
                target = ownText.substring(2, ownText.length() - 5).trim();
                platform = "-";
            }
            dep.setPlatform(platform);
            dep.setLocDestination(target);
            departures.add(dep);
        }

         /*
        String all = "list:";
        for (int i = 0; i < departureList.size(); i++) {
            //all += "\n" + i + ": " + departureList.get(i).text();
            all += "\nDelay: " + departureList.get(i).select("span[class=okmsg").text();
            Elements classBold = departureList.get(i).select("span[class=bold");
            all += ", Type: " + classBold.get(0).text();
            all += ", Time: " + classBold.get(1).text();
            String ownText = departureList.get(i).ownText();
            String platform, target;
            if(ownText.contains("Gl. ")){
                target = ownText.substring(2, ownText.length()-8).trim();
                platform = ownText.substring(ownText.indexOf("Gl."));
            } else {
                target = ownText.substring(2, ownText.length()-5).trim();
                platform = "-";
            }
            all += ", Platform: "+ platform;
            all += ", Target: " + target;
        }
        Log.i("LateAgainNew", all);
        return;
        */
    }

    /**
     * prepare and send request to mBahnUrl and save output in mBahn
     */
    private void sendRequest() {
        Log.i("LateAgainRequest", "Trying input:" + start + ", date:" + date + ", time:" + time);
        new Thread() {
            public void run() {
                try {
                    mBahn = Jsoup
                            .connect(mBahnUrl)
                            //start location
                            .data("input", start)
                            .data("inputRef", "#")
                            // date
                            .data("date", date)
                            // time
                            .data("time", time)
                            .data("productsFilter", "1111111111000000") //TODO check filters
                            .data("REQTrain_name", dest) //TODO testing only
                            .data("maxJourneys", "15") //todo fewer?
                            .data("boardType", "Abfahrt")
                            .data("ao", "yes").data("start", "Suchen")
                            .userAgent("Mozilla").post();
                } catch (UnknownHostException e) {
                    Log.e("LateAgain", "No connection!!");
                } catch (IOException | NoSuchElementException e) {
                    Log.e("LateAgain", "Request failed:\n" + e.toString());
                }
            }
        }.start();
    }
}
