package com.example.group13_hw10;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener, SignUpFragment.SignUpListener, TripsFragment.TripsListener, CreateTripFragment.CreateTripListener{

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth  = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.containerView, new LoginFragment())
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.containerView, new TripsFragment())
                    .commit();
        }
    }

    @Override
    public void createNewAccount() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new SignUpFragment())
                .commit();
    }

    @Override
    public void goToPostsFragment(String name) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new TripsFragment())
                .commit();
    }

    @Override
    public void login() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new LoginFragment())
                .commit();
    }

    @Override
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new LoginFragment())
                .commit();
    }
    @Override
    public void createTrip() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new CreateTripFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openTripDetails(Trip trip) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, TripDetailsFragment.newInstance(trip))
                .addToBackStack(null)
                .commit();
    }
    @Override
    public void goBackToTrips() {
        getSupportFragmentManager().popBackStack();
    }

}