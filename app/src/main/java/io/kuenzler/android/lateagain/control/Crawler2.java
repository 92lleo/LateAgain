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
        mBahnUrl = "http://mobile.bahn.de/bin/mobil/query.exe/dox?country=DEU&rt=1&use_realtime_filter=1&webview=&searchMode=NORMAL";
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

        // get alternatives for start (REQ0JourneyStopsZ0K)
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
        if (!mBahn.title().contains("Ihre Auskunft")) {
            //mBahn = null;
            if(cleanAndParseAlternativeLocations()){
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
        // get corrected start and dest
        Elements locations = mBahn.select("div[class=stdpadding editBtnCon paddingleft]").get(0).children();
        start = locations.get(1).text();
        dest = locations.get(2).text();
        reqLoop.setCorrectedLocations(start, dest);
        // get departures table and split into elemets
        Elements departuresTable = mBahn.select("table[class=ovTable clicktable]").first().children();
        // remove headlines and footer stuff
        departuresTable = departuresTable.get(1).children();
        departuresTable.remove(5);

        departures = new ArrayList<Departure>();
        for (Element departureTable : departuresTable) {
            // split to sinlge departures
            Elements single = departureTable.children();
            Iterator<Element> elements = single.iterator();
            Departure departure = new Departure();
            String[] current = elements.next().text().split(" ");
            departure.setTimes(current[0], current[1]);
            departure.setLocations(start, dest);
            current = elements.next().text().split(" ");
            if (current.length > 2) {
                current[0] = current[0] + " " + current[1];
                current[1] = current[2] + " " + current[3];
            }
            if (current[0].trim().length() < 2) {
                current[0] = "-0";
            }
            departure.setDelay(current[0]);
            current = elements.next().text().split(" ");
            departure.setDuration(current[1]);
            departure.setType((elements.next().text().split(" "))[0]);
            departures.add(departure);
        }
    }

    /**
     * prepare and send request to mBahnUrl and save output in mBahn
     */
    private void sendRequest() {
        new Thread() {
            public void run() {
                try {
                    mBahn = Jsoup
                            .connect(mBahnUrl)
                            .data("queryPageDisplayed", "yes")
                            .data("REQ0JourneyStopsS0A", "1")
                            .data("REQ0JourneyStopsS0G", start)
                            // start
                            .data("REQ0JourneyStopsS0ID", "")
                            .data("locationErrorShownfrom", "yes")
                            .data("REQ0JourneyStopsZ0A", "1")
                            .data("REQ0JourneyStopsZ0G", dest)
                            // dest
                            .data("REQ0JourneyStopsZ0ID", "")
                            .data("locationErrorShownto", "yes")
                            .data("REQ0JourneyDate", date)
                            // date
                            .data("REQ0JourneyTime", time)
                            .data("REQ0HafasSearchForw", "1")
                            .data("REQ0Tariff_TravellerType.1", "E")
                            .data("REQ0Tariff_TravellerReductionClass.1", "0")
                            .data("REQ0Tariff_Class", "2")
                            .data("REQ0JourneyStops1.0A", "1")
                            .data("REQ0JourneyStops1.0G", "")
                            .data("REQ0JourneyStops2.0A", "1")
                            .data("REQ0JourneyStops2.0G", "")
                            .data("REQ0HafasChangeTime:0", "1")
                            .data("existOptimizePrice", "1")
                            .data("REQ0HafasOptimize1:0", "1")
                            .data("existOptionBits", "yes")
                            .data("immediateAvail", "ON").data("start", "Suchen")
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
