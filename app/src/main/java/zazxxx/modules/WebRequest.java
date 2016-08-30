package zazxxx.modules;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by nhapt on 8/26/2016.
 * Các hàm truy xuất data từ web (makeWebServiceCall) và biến String đó thành ArrayList (parseLocationList).
 */
public class WebRequest {

    public final static int GET = 1;
    public final static int POST = 2;

    //Constructor with no parameter
    public WebRequest() {

    }

    /**
     * Making web service call
     *
     * !url - url to make request
     * !requestmethod - http request method
     */
    public String makeWebServiceCall(String url, int requestmethod) {
        return this.makeWebServiceCall(url, requestmethod, null);
    }

    /**
     * Making service call
     *
     * !url - url to make request
     * !requestmethod - http request method
     * !params - http request params
     */
    public String makeWebServiceCall(String urladdress, int requestmethod, HashMap<String, String> params) {
        URL url;
        String response = "";
        try {
            url = new URL(urladdress);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);

            if (requestmethod == POST) {
                conn.setRequestMethod("POST");
            } else if (requestmethod == GET) {
                conn.setRequestMethod("GET");
            }

            if (params != null) {
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));

                StringBuilder result = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (first)
                        first = false;
                    else
                        result.append("&");

                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }

                writer.write(result.toString());

                writer.flush();
                writer.close();
                os.close();
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public ArrayList<HashMap<String, String>> parseLocationList(String urlAddress) throws JSONException {

        if (urlAddress != null) {
            JSONArray listLoc = new JSONArray(makeWebServiceCall(urlAddress, GET));
            ArrayList<HashMap<String, String>> listLocs = new ArrayList<>();

            for (int i = 0; i < listLoc.length(); i++) {
                JSONObject c = listLoc.getJSONObject(i);

                HashMap<String, String> Location = new HashMap<>();
                Location.put("id", c.getString("id"));
                Location.put("name", c.getString("name"));
                Location.put("address", c.getString("address"));
                Location.put("lat", c.getString("lat"));
                Location.put("lng", c.getString("lng"));

                listLocs.add(Location);
            }
            return listLocs;
        } else {
            Log.d("Error", "lỗi ko có URL");
            return null;
        }
    }

    public List<LatLng> parseDirectionList(String urlAddr){

        try {
            JSONObject jObject = new JSONObject(makeWebServiceCall(urlAddr, GET));
            String temp = jObject.toString();
            if(temp.contains("overview_polyline")) {
                JSONArray jRoutes = jObject.getJSONArray("routes");
                JSONObject jRoute = jRoutes.getJSONObject(0);
                JSONObject oPolyLine = jRoute.getJSONObject("overview_polyline");
                String encodePoint = oPolyLine.getString("points");
                return decodePoly(encodePoint);
            } else {
                Log.d("Error: ", temp);
                return null;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy từ link phía dưới.
     * Method to decode polyline points
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encodedPoly) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encodedPoly.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encodedPoly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encodedPoly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
