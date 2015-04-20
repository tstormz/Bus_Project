package sunyoswego.centrotr;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class BusRoute {

    String routeName;
    ArrayList<BusStop> busStops = new ArrayList<BusStop>();
    ArrayList<LatLng> routePoints = new ArrayList<LatLng>();

    public BusRoute(String routeName){
        this.routeName = routeName;
    }

    public String getRouteName() {
        return routeName;
    }

    public ArrayList<BusStop> getBusStops() {
        return busStops;
    }

    public ArrayList<LatLng> getRoutePoints() {
        return routePoints;
    }

    public void setRoutePoints(ArrayList<LatLng> routePoints) {
        this.routePoints = routePoints;
    }

    public void setBusStops(ArrayList<BusStop> busStops) {
        this.busStops = busStops;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    // a class to wrap one string & one map because THERE MUST BE ONLY ONE PARAMETER
    private class Wrapper {
        public String s;
        public GoogleMap map;

        public Wrapper(String s, GoogleMap map) {
            this.s = s;
            this.map = map;
        }
    }

    public void loadRoute(GoogleMap map) throws IOException {
        // Download the stops from a server (using Async)
        DownloadStopsTask downloadTask = new DownloadStopsTask();
        downloadTask.execute(map);
    }

    private class DownloadStopsTask extends AsyncTask<GoogleMap, Void, Wrapper> {
        @Override
        protected Wrapper doInBackground(GoogleMap... map) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl("http://www.oswego.edu/~hafner/bus_stops.txt");
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return new Wrapper(data, map[0]);
        }

        @Override
        protected void onPostExecute(Wrapper w) {
            super.onPostExecute(w);
            Scanner sc = new Scanner(w.s);
            while (sc.hasNext()) {
                String line = sc.nextLine();
                String[] params = line.split(",");
                LatLng coordinates = new LatLng(Double.parseDouble(params[1]),Double.parseDouble(params[2]));
                busStops.add(new BusStop(params[0],coordinates));
            }
            for (BusStop stop : getBusStops()) {
                w.map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.appicon))
                        .title(stop.getName())
                        .snippet("2*2")
                        .position(stop.getCoordinates()));
            }
        }
    }

    public class HttpConnection {
        public String readUrl(String xmlUrl) throws IOException {
            String data = "", color = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(xmlUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        iStream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    if (line.length() > 4 && line.substring(0,5).equals("route")) {
                        color = line.substring(6);
                    } else if (color.equals("blue")) {
                        sb.append(line + "\n");
                    }
                }
                data = sb.toString();
                br.close();
            } catch (Exception e) {
                Log.d("Exception!!", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

}
