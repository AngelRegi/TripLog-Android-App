package com.example.group13_hw10;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.group13_hw10.databinding.FragmentTripDetailsBinding;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TripDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripDetailsFragment extends Fragment implements OnMapReadyCallback {

    private final OkHttpClient client = new OkHttpClient();
    private GoogleMap mMap;
    private MapView mapView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String ARG_PARAM_TRIP = "ARG_PARAM_TRIP";
    private Trip trip;
    private FusedLocationProviderClient fusedLocationClient;
    FragmentTripDetailsBinding binding;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TripDetailsFragment() {
        // Required empty public constructor
    }


    public static TripDetailsFragment newInstance(Trip trip) {
        TripDetailsFragment fragment = new TripDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_TRIP, trip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trip = (Trip) getArguments().getSerializable(ARG_PARAM_TRIP);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTripDetailsBinding.inflate(inflater, container, false);
        binding.textViewTripName.setText(trip.getTripName());
        binding.textViewTripStartTime.setText("Started At : " + trip.getTripStartTime());
        binding.textViewTripStatus.setText(trip.getTripStatus());
        if(trip.getTripStatus().equals("On Going")) {
            binding.textViewTripDistance.setVisibility(View.INVISIBLE);
            binding.buttonCompleteTrip.setVisibility(View.VISIBLE);
            binding.textViewTripStatus.setTextColor(Color.RED);
            binding.textViewTripCompleteTime.setText("Completed At : N/A" );
        } else {
            binding.buttonCompleteTrip.setVisibility(View.INVISIBLE);
            binding.textViewTripDistance.setVisibility(View.VISIBLE);
            binding.textViewTripStatus.setTextColor(Color.GREEN);
            binding.textViewTripCompleteTime.setText("Completed At : " + trip.getTripCompleteTime() );
            binding.textViewTripDistance.setText(trip.getTripDistance());
        }

        binding.buttonCompleteTrip.setOnClickListener(v -> {
            if(hasLocationPermission()) {
                getLastLocation();
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showCustomDialog("Location Permission", "This app needs the location permission to track your location", "ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentLocationPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
                        }
                    }, "Cancel", null);
                } else {
                    currentLocationPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
                }
            }
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        getActivity().setTitle("Trip Details");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng latLng = new LatLng(trip.getStartLocationLat(), trip.getStartLocationLng());
        Marker startMarker =  googleMap.addMarker(new MarkerOptions().position(latLng).title("I am here!"));
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        markers.add(startMarker);
    }

    private ActivityResultLauncher<String[]> currentLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            boolean finePermissionAllowed = true;
            if(result.get(Manifest.permission.ACCESS_FINE_LOCATION) != null) {
                finePermissionAllowed = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                if(finePermissionAllowed) {
                    getLastLocation();
                } else {
                    if(!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showCustomDialog("location Permission", "The app needs the location  permission to function, please go and allow this permission in the app settings", "Goto settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                                startActivity(intent);
                            }
                        }, "Cancel", null);
                    }
                }
            }
        }
    });

    @SuppressLint("NewApi")
    private boolean hasLocationPermission() {
        return getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {

        CurrentLocationRequest currentLocationRequest = new CurrentLocationRequest.Builder()
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(5000)
                .setMaxUpdateAgeMillis(0)
                .build();

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();


        fusedLocationClient.getCurrentLocation(currentLocationRequest, cancellationTokenSource.getToken()).addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()) {
                    Location location = task.getResult();
                    setCurrentLocationInMap(location);
                    Log.d("demo", "onComplete: " + location);
                    //startLocation = location;

                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }
    ArrayList<Marker> markers = new ArrayList<Marker>();
    private void setCurrentLocationInMap(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Marker endMarker =  mMap.addMarker(new MarkerOptions().position(latLng).title("I am here!"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
       // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        markers.add(endMarker);


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        mMap.animateCamera(cu);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        String url  = "https://maps.googleapis.com/maps/api/directions/json?destination=" + location.getLatitude() + ", " + location.getLongitude() + "&origin="+trip.getStartLocationLat()+","+ trip.getStartLocationLng() + "&units=imperial&key=AIzaSyC2lofnGc0Y5MH0OPrEb7uhpAlQLU6GUXM";
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        JSONObject json = new JSONObject(body);
                        JSONArray routes = json.getJSONArray("routes");
                        JSONObject routesObj = routes.getJSONObject(0);
                        JSONObject distanceObj = routesObj.getJSONArray("legs").getJSONObject(0).getJSONObject("distance");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    //realtime data
                                    HashMap<String, Object> tripObj = new HashMap<>();
                                    tripObj.put("tripDistance", distanceObj.getString("text"));
                                    Date completeDate = new Date();
                                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
                                    String strDate = formatter.format(completeDate);
                                    tripObj.put("tripStatus", "Completed");
                                    tripObj.put("tripCompleteTime", strDate);
                                    db.collection("trips").document(trip.getTripId())
                                            .update(tripObj)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        Log.d("demo", "onComplete: delete");
                                                        binding.buttonCompleteTrip.setVisibility(View.INVISIBLE);
                                                        binding.textViewTripDistance.setVisibility(View.VISIBLE);
                                                        binding.textViewTripCompleteTime.setText("Completed At : " + strDate);
                                                        binding.textViewTripStatus.setText("Completed");
                                                        binding.textViewTripStatus.setTextColor(Color.GREEN);
                                                        try {
                                                            binding.textViewTripDistance.setText(distanceObj.getString("text"));

                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }

                                                        //getPosts();
                                                    } else {
                                                        Toast.makeText(getActivity(), "Error deleting post", Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            });


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } catch(JSONException e) {

                    }

                } else {
                    //error handler
                    ResponseBody responseBody = response.body();
                    String body = responseBody.string();
                    Log.d("demo", "error: " + body);
                }
            }
        });
    }
    public void showCustomDialog(String title, String message, String positiveBtntitle, DialogInterface.OnClickListener positiveListener,
                                 String negativeBtnTitle, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveBtntitle, positiveListener)
                .setNegativeButton(negativeBtnTitle, negativeListener);
        builder.create().show();
    }


}