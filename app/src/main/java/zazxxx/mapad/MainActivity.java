package zazxxx.mapad;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.maps.model.Polyline;
import java.util.ArrayList;
import java.util.List;
import zazxxx.modules.getNearPlace;
import zazxxx.modules.getDierctionPlace;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static GoogleMap mGoogleMap;
    public static List<Marker> pMarkers = new ArrayList<>(); // Biến danh sách các địa điểm
    public static Polyline directionPath; // Biến đường đi.
    GoogleApiClient mGoogleClient;
    LocationRequest mLocateRequest;
    Marker cMarker;
    LatLng currLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (googleServicesAvailable()) {
            setContentView(R.layout.activity_main);
            initMap();
        }
    }

    private void initMap() {
        MapFragment mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFrag.getMapAsync(this);
    }

    // Thực thi khi người dùng nhấn 1 loại category trong class Marker List.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Cập nhật mới tên category.
        setIntent(intent);
        switch (intent.getStringExtra("AcID")){
            case "from_MarkerList":
                String urlAdd = "http://demo.manage.vn/locationTest/getPlace.php";
                // Lấy giá trị String từ category
                String value = intent.getStringExtra("name");
                if (value != null) {
                    value = value.replace(" ", "%20");
                }
                // Cập nhật URL với biến category.
                urlAdd = urlAdd + "?cat=" + value;
                // Kiểm tra xem các marker cũ đã được xóa hay chưa.
                if(pMarkers != null){
                    for(int i=0; i<pMarkers.size();i++){
                        Marker temp = pMarkers.get(i);
                        temp.remove();
                    }
                }
                // Thêm các điểm trên bản đồ dựa vào thông tin từ URL.
                new getNearPlace().execute(urlAdd);
                break;
        }
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

            // Thực thi khi người dùng click vào nút tìm đường.
            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    String urlDirect = "https://maps.googleapis.com/maps/api/directions/json";
                    urlDirect = urlDirect
                            + "?origin="
                            + currLL.latitude
                            + ","
                            + currLL.longitude
                            + "&destination="
                            + marker.getPosition().latitude
                            + ","
                            + marker.getPosition().longitude
                            + "&key="
                            + getResources().getString(R.string.direct_api);

                    if(directionPath != null){
                        directionPath.remove();
                    }
                    new getDierctionPlace().execute(urlDirect);
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

        //Lấy location từ GPS thiết bị, và di chuyển tới vị trí của người dùng.
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleClient);
        if (mLastLocation != null) {
            //place marker at current position
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
        mLocateRequest.setInterval(15000);

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

    // Thực thi khi có thay đổi vị trí của người dùng hiện tại.
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
}
