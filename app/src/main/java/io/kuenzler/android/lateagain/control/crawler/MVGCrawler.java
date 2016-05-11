package io.kuenzler.android.lateagain.control.crawler;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import io.kuenzler.android.lateagain.control.RequestLoop;
import io.kuenzler.android.lateagain.model.Departure;

/**
 * Crawler for munich mvg (tram, bus, ubahn).
 *
 * @author Leonhard Künzler
 * @version 0.1
 * @date 17.02.16
 */
public class MVGCrawler extends Crawler {

    private static final String MVG_URL = "http://www.mvg-live.de/ims/dfiStaticAnzeige.svc";
    private Departure source;
    private boolean u, bus, tram, sbahn;

    public MVGCrawler(RequestLoop requestLoop) {
        super(requestLoop, MVG_URL);
    }

    @Override
    public ArrayList<Departure> getDepartures(String date, String time, String start, String dest, String filter) {
        return null;
    }

    public Departure getDelayByDeparture(Departure dep) {
        this.source = dep;
        this.start = dep.getLocStart();
        this.time = dep.getTimeStart();
        this.dest = dep.getLocDestination(); //TODO used? filter?
        u = bus = tram = sbahn = false;

        switch (dep.getType()) {
            case "U":
                u = true;
                break;
            case "S":
                sbahn = true; //should not happen
                //TODO return here, wrong request
                break;
            case "bus":
                bus = true;
                break;
            case "tram":
                tram = true;
                break;
            default:
        }
        sendRequest();
        cleanAndParseAlternativeLocations();
        Departure result = dep;
        if (!departures.isEmpty()) {
            for (Departure currentDep : departures) {
                if (isDesiredDeparture(currentDep)) {
                    int resultDelay = 0;
                    //TODO calculate Delay by startTime
                    //resultDelay = mvgDelay - (srcStartTime-CurrentTime)
                    result.setDelay("+" + resultDelay);
                    break;
                }
            }
        }
        if (result.getDelay().equals("-0")) {
            //no departure found
        }
        return result;
    }

    private boolean isDesiredDeparture(Departure currentDep) {
        //source == currentDep //TODO
        return true;
    }

    @Override
    protected boolean cleanAndParseAlternativeLocations() {
        //TODO use db alternative locations here and recheck if they exist in the mvg net
        while (mDocument == null) {
            //wait for request
        }
        if (mDocument.text().contains("Die eingegebene Haltestelle wurde nicht gefunden!")) {
            if (cleanAndParseAlternativeLocations()) {
                departures = null;//TODO
                return false; //TODO return;
            }
            mDocument = null;
            sendRequest();
        }
        while (mDocument == null) {
            //wait for request
        }
        if (mDocument.text().contains("//TODO checkt text")) {//TODO
            throw new NoSuchElementException(
                    "No departures in near future!");
        }
        //TODO upper stuff needed?
        Elements contentTable = mDocument.getElementsByClass("departureTable")
                .get(1).children().get(0).children();
        contentTable.remove(0); //remove first and last entry (header & footer)
        contentTable.remove(contentTable.size() - 1);
        departures = new ArrayList<>();
        for (Element e : contentTable) {
            Departure dep = new Departure();

            Elements departure = e.children();
            String type = departure.get(1).text().trim();
            if (type.toLowerCase().startsWith("u")) {
                type = "u";
            } else if (type.toLowerCase().startsWith("s")) {
                type = "s";
            } else if (type.length() == 2) {
                type = "tram";
            } else if (type.length() == 3) {
                type = "bus";
            } else {
                // dont know
                //TODO: return here
            }
            dep.setType(type);
            dep.setDelay(departure.get(2).text().trim());
            dep.setLocDestination(departure.get(1).text().substring(0, departure.get(1).text().length() - 2));

            System.out.print("Dep: " + departure.get(0).text().trim());
            System.out.print(" " + departure.get(1).text().substring(0, departure.get(1).text().length() - 2));
            System.out.println(" in " + departure.get(2).text().trim()
                    + " minutes");
            departures.add(dep);
        }
        return false;
    }

    @Override
    protected void cleanAndParseResults() {

    }

    @Override
    protected void sendRequest() {
        Log.i("LateAgainRequest", "Trying input:" + start + ", date:" + date + ", time:" + time);
        String stop = start;
        stop = "Barthstraße"; //Todo change later
        u = true;
        bus = true;
        tram = true;
        sbahn = false;
        //?haltestelle=Barthstra%DFe&ubahn=checked&bus=checked&tram=checked&sbahn=checked
        final String request;
        String tmpRequest = mBahnUrl + "?";
        //does not need to be "checked", will be checked in every case but =""
        tmpRequest += "haltestelle=";
        tmpRequest += stop; //stop here
        tmpRequest += "&ubahn=";
        tmpRequest += u ? "checked" : ""; //show u
        tmpRequest += "&bus=";
        tmpRequest += bus ? "checked" : ""; //show buses
        tmpRequest += "&tram=";
        tmpRequest += tram ? "checked" : ""; //show buses
        tmpRequest += "&sbahn=";
        tmpRequest += sbahn ? "checked" : ""; //show sbahn, should always be false
        request = tmpRequest;
        new Thread() {
            public void run() {
                try {
                    mDocument = Jsoup
                            .connect(request)
                            .userAgent("Mozilla").post();
                } catch (UnknownHostException e) {
                    Log.e("LateAgain", "No connection!!");
                } catch (IOException | NoSuchElementException e) {
                    Log.e("LateAgain", "Request failed:\n" + e.toString());
                } catch (IllegalArgumentException e) {
                    Log.e("LateAgain", "Request failed: Nullvalue in post");
                }
            }
        }.start();
    }
}
