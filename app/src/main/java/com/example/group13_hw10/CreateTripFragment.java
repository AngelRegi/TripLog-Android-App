package com.example.group13_hw10;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import com.example.group13_hw10.databinding.FragmentCreateTripBinding;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateTripFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateTripFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private FirebaseAuth mAuth;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    FragmentCreateTripBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Location startLocation;
    public CreateTripFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateTripFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateTripFragment newInstance(String param1, String param2) {
        CreateTripFragment fragment = new CreateTripFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_create_trip, container, false);

        binding = FragmentCreateTripBinding.inflate(inflater, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Create Trip");
        binding.textViewCurLocationStatus.setText("Loading...");
        binding.textViewCurLocationStatus.setTextColor(Color.RED);
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

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tripName = binding.editTripText.getText().toString();
                if(tripName.isEmpty()){
                    showAlertDialog("Enter a Valid Trip Name!");
                } else {
                    mAuth = FirebaseAuth.getInstance();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    HashMap<String, Object> trip = new HashMap<>();
                    trip.put("tripCreatedByName",  mAuth.getCurrentUser().getDisplayName());
                    trip.put("tripCreatedByUid", mAuth.getCurrentUser().getUid() );
                    trip.put("tripName", tripName);
                    Date startDate = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
                    String strDate = formatter.format(startDate);
                    trip.put("tripStartTime", strDate);
                    trip.put("tripStatus", "On Going");
                    trip.put("tripCompleteTime", "N/A");
                    trip.put("tripDistance", "0");
                    trip.put("startLocationLat", startLocation.getLatitude());
                    trip.put("startLocationLng", startLocation.getLongitude());
                    db.collection("trips").add(trip)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()) {
                                        Log.d("demo", "onSuccess: created");
                                        binding.editTripText.setText("");
                                        mListener.goBackToTrips();
                                    } else {
                                        showAlertDialog("Error Creating the trip");
                                    }
                                }
                            });
                }
            }
        });
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
                    Log.d("demo", "onComplete: " + location);
                    startLocation = location;
                    binding.textViewCurLocationStatus.setText("Success");
                    binding.textViewCurLocationStatus.setTextColor(Color.GREEN);
                } else {
                    task.getException().printStackTrace();
                }
            }
        });
        /*
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()) {
                    Location location = task.getResult();
                    Log.d("demo", "onComplete: " + location);
                } else {
                    task.getException().printStackTrace();
                }
            }
        });

         */
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

    public void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Create Trip Error!")
                .setMessage(message)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    CreateTripListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (CreateTripListener) context;
    }

    interface CreateTripListener {
        void goBackToTrips();
    }
}