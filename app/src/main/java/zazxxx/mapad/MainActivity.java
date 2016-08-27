package zazxxx.mapad;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import zazxxx.modules.WebRequest;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    GoogleMap mGoogleMap;
    GoogleApiClient mGoogleClient;
    LocationRequest mLocateRequest;
    Marker cMarker;
    String urlAdd = "http://demo.manage.vn/locationTest/getPlace.php";
    String urlDirect = "https://maps.googleapis.com/maps/api/directions/json";
    List<Marker> pMarkers = new ArrayList<>();
    LatLng currLL, destLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (googleServicesAvailable()) {
            //Toast.makeText(this, "Testing", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_main);
            initMap();
        }
    }

    // Thực thi khi người dùng nhấn 1 loại category trong class Marker List.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Cập nhật mới tên category.
        setIntent(intent);

        //Bundle extra = getIntent().getExtras();

        switch (intent.getStringExtra("AcID")){
            case "from_MakerList":
                // Lấy giá trị String từ category
                String value = intent.getStringExtra("name");
                Toast.makeText(this, value, Toast.LENGTH_LONG).show();
                if (value != null) {
                    value = value.replace(" ", "%20");
                }
                // Cập nhật URL với biến category.
                String newUrl = urlAdd + "?cat=" + value;

                // Thêm các điểm trên bản đồ dựa vào thông tin từ URL.
                new getPlace().execute(newUrl);
                break;
        }
        /*
        if(extra != null) {
            // Lấy giá trị String từ category
            String value = extra.getString("name");
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
            if (value != null) {
                value = value.replace(" ", "%20");
            }
            // Cập nhật URL với biến category.
            String newUrl = urlAdd + "?cat=" + value;

            // Thêm các điểm trên bản đồ dựa vào thông tin từ URL.
            new getPlace().execute(newUrl);
        }
        */
    }

    private void initMap() {
        MapFragment mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFrag.getMapAsync(this);
    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvail = api.isGooglePlayServicesAvailable(this);
        if (isAvail == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvail)) {
            Dialog dialog = api.getErrorDialog(this, isAvail, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cant connect to Play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if(mGoogleMap != null){
            // Vẽ lại info window cho marker.
            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.info_window, null);

                    TextView locName = (TextView) v.findViewById(R.id.locName);
                    TextView locSnippet = (TextView) v.findViewById(R.id.locSnippet);

                    locName.setText(marker.getTitle());
                    locSnippet.setText(marker.getSnippet());

                    return v;
                }
            });

            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    //Toast.makeText(MainActivity.this,"CLICKED", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    String locName = marker.getTitle();
                    String locAdd = marker.getSnippet();
                    intent.putExtra("locName", locName);
                    intent.putExtra("locAdd", locAdd);

                    startActivity(intent);
                }
            });
            //mGoogleMap.setOnInfoWindowClickListener();
        }

        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleClient);
        if (mLastLocation != null) {
            //place marker at current position
            //mGoogleMap.clear();
            currLL = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currLL);
            markerOptions.title("Current Position");
            cMarker = mGoogleMap.addMarker(markerOptions);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currLL, 17);
            mGoogleMap.animateCamera(update);
        }

        mLocateRequest = LocationRequest.create();
        mLocateRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocateRequest.setInterval(10000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleClient, mLocateRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null) {
            Toast.makeText(this, "ABC", Toast.LENGTH_LONG).show();
        } else {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());

            if(cMarker != null){
                cMarker.remove();
            }

            MarkerOptions markerOpt = new MarkerOptions().title("You're here").position(ll);
            cMarker = mGoogleMap.addMarker(markerOpt);
        }
    }

    private class getPlace extends AsyncTask<String, Void, Void>{
        ArrayList<HashMap<String,String>> locations = null;
        Marker pMarker;

        @Override
        protected Void doInBackground(String... urlAddesses) {
            if(urlAddesses[0] != null) {
                try {
                    // Lấy data vị trí.
                    locations = new WebRequest().parseLocationList(urlAddesses[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("There is no URL");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);

            if(locations != null) {
                // Trường hợp đã tồn tại các marker thì xóa các marker cũ.
                if (pMarkers != null) {
                    for(int i = 0; i < pMarkers.size(); i++){
                        pMarker = pMarkers.get(i);
                        pMarker.remove();
                    }
                }

                // Thêm marker mới.
                for (int i = 0; i < locations.size(); i++) {
                    HashMap<String, String> temp = locations.get(i);
                    Double plat = Double.parseDouble(temp.get("lat"));
                    Double plng = Double.parseDouble(temp.get("lng"));
                    LatLng pll = new LatLng(plat, plng);
                    MarkerOptions pMarkerOpts = new MarkerOptions()
                            .title(temp.get("name"))
                            .snippet(temp.get("address"))
                            .position(pll);
                    pMarker = mGoogleMap.addMarker(pMarkerOpts);
                    pMarkers.add(pMarker);
                }
            } else {
                Toast.makeText(MainActivity.this, urlAdd, Toast.LENGTH_LONG).show();
            }
        }
    }

}
