package com.example.group13_hw10;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.group13_hw10.databinding.FragmentTripsBinding;
import com.example.group13_hw10.databinding.TripRowItemBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TripsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripsFragment extends Fragment {

    FragmentTripsBinding binding;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TripsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TripsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TripsFragment newInstance(String param1, String param2) {
        TripsFragment fragment = new TripsFragment();
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
        binding = FragmentTripsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Trips");
        binding.buttonCreateTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.createTrip();
            }
        });

        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.logout();
            }
        });

        binding.recyclerViewTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        tripsAdapter = new TripsAdapter();
        binding.recyclerViewTrips.setAdapter(tripsAdapter);
        getTrips();
    }

    TripsAdapter tripsAdapter;
    ArrayList<Trip> mTrips = new ArrayList<>();

    class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.TripsViewHolder> {
        @NonNull
        @Override
        public TripsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TripRowItemBinding binding = TripRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new TripsViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull TripsViewHolder holder, int position) {
            Trip trip = mTrips.get(position);
            holder.setupUI(trip);
        }

        @Override
        public int getItemCount() {
            return mTrips.size();
        }

        class TripsViewHolder extends RecyclerView.ViewHolder {
            TripRowItemBinding mBinding;
            Trip mTrip;
            public TripsViewHolder(TripRowItemBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void setupUI(Trip trip){
                mTrip = trip;
                mBinding.textViewTrip.setText(trip.getTripName());
                mBinding.textViewStartTime.setText("Started At : " + trip.getTripStartTime());
                mBinding.textViewCompletedTime.setText("Completed At : " + trip.getTripCompleteTime());
                mBinding.textViewStatus.setText(trip.getTripStatus());
                if(trip.getTripStatus().equals("On Going")) {
                    mBinding.textViewStatus.setTextColor(Color.RED);
                } else {
                    mBinding.textViewStatus.setTextColor(Color.GREEN);
                }
                if(!trip.getTripDistance().equals("0")) {
                    mBinding.textViewMiles.setText(trip.getTripDistance() + "");
                } else {
                    mBinding.textViewMiles.setText("");
                }

                mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.openTripDetails(mTrip);
                    }
                });

            }
        }

    }

    //get posts

    private void getTrips() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //realtime data
        db.collection("trips")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        mTrips.clear();
                        for(QueryDocumentSnapshot document: value) {
                            Trip trip = new Trip();
                            trip.setTripId(document.getId());
                            trip.setTripName(document.getString("tripName"));
                            trip.setTripStatus(document.getString("tripStatus"));
                            trip.setTripDistance(document.getString("tripDistance"));
                            trip.setTripCreatedByName(document.getString("tripCreatedByName"));
                            trip.setTripCreatedByUid(document.getString("tripCreatedByUid"));
                            trip.setTripCompleteTime(document.getString("tripCompleteTime"));
                            trip.setTripStartTime(document.getString("tripStartTime"));
                           trip.setStartLocationLat(document.getDouble("startLocationLat"));
                           trip.setStartLocationLng(document.getDouble("startLocationLng"));
                            mTrips.add(trip);

                           // mTrips.add(new Trip(document.getString("created_by_name"), document.getId(), document.getString("created_by_uid"),document.getString("post_text"), document.getString("created_at")  ));
                        }
                        tripsAdapter.notifyDataSetChanged();
                    }
                });

    }

    TripsListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (TripsListener) context;
    }

    interface TripsListener{
        void logout();
        void createTrip();
        void openTripDetails(Trip trip);
    }
}