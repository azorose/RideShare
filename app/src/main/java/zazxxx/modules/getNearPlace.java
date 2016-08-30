package zazxxx.modules;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import zazxxx.mapad.MainActivity;

/**
 * Created by nhapt on 8/28/2016.
 * Class lấy địa diểm gần nhất.
 */
public class getNearPlace extends AsyncTask<String, Integer, ArrayList<HashMap<String, String>>> {

    @Override
    protected ArrayList<HashMap<String, String>> doInBackground(String... strings) {
        if(strings != null){
            try {
                return new WebRequest().parseLocationList(strings[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
        Marker pMarker;
        if(result != null) {
            for (int i = 0; i < result.size(); i++) {
                HashMap<String, String> temp = result.get(i);
                LatLng pll = new LatLng(
                        Double.parseDouble(temp.get("lat")),
                        Double.parseDouble(temp.get("lng")));
                MarkerOptions pMarkerOpts = new MarkerOptions()
                        .title(temp.get("name"))
                        .snippet(temp.get("address"))
                        .position(pll);
                pMarker = MainActivity.mGoogleMap.addMarker(pMarkerOpts);

                if(pMarker != null) {
                    MainActivity.pMarkers.add(pMarker);
                } else {
                    Log.d("Error:" , "pMarker is null");
                }
            }
        } else {
            Log.d("Error: ", "Result is empty");
        }
    }
}
