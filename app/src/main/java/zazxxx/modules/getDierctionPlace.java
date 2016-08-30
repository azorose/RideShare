package zazxxx.modules;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import zazxxx.mapad.MainActivity;

/**
 * Created by nhapt on 8/28/2016.
 * Class tìm đường đi từ A sang B.
 */
public class getDierctionPlace extends AsyncTask<String, String, List<LatLng>> {

    @Override
    protected List<LatLng> doInBackground(String... strings) {
        if(strings != null){
            return new WebRequest().parseDirectionList(strings[0]);
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<LatLng> result){
        if(result != null){
            Log.d("Debug: ", "Start set polyline");
            PolylineOptions opts = new PolylineOptions()
                    .addAll(result)
                    .color(Color.RED);
            MainActivity.directionPath = MainActivity.mGoogleMap.addPolyline(opts);
        } else {
            Log.d("Error: ", "result is null");
        }

    }
}
