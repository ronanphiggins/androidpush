package de.andreasschrade.androidtemplate.activities.peripheral;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.andreasschrade.androidtemplate.R;
import de.andreasschrade.androidtemplate.activities.core.LoginActivity;
import de.andreasschrade.androidtemplate.activities.core.SettingsActivity;
import de.andreasschrade.androidtemplate.backendless.Tender;
import de.andreasschrade.androidtemplate.activities.base.BaseActivity;
import de.andreasschrade.androidtemplate.utilities.CustomDialogClass;
import de.andreasschrade.androidtemplate.utilities.SaveSharedPreference;
import de.andreasschrade.androidtemplate.utilities.StringUtil;
import de.andreasschrade.androidtemplate.utilities.Wrapper;
import de.andreasschrade.androidtemplate.utilities.bitmapUtil;

/**
 * Activity demonstrates some GUI functionalities from the Android support library.
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class HomeActivity extends BaseActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Bitmap theBitmap;

    Double userLat;
    Double userLng;

    Boolean hasTender = false;

    String dateType;

    String dateObjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        setupToolbar();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Backendless.Persistence.of( Tender.class).find(new AsyncCallback<BackendlessCollection<Tender>>() {
            @Override
            public void handleResponse(final BackendlessCollection<Tender> foundTenders) {

                Iterator<Tender> iterator = foundTenders.getCurrentPage().iterator();

                while (iterator.hasNext()) {

                    final Tender tender = iterator.next();

                    final String ownerId = tender.getOwnerId();
                    final Double tenderLat = tender.getLatitude();
                    final Double tenderLong = tender.getLongitude();

                    if (ownerId.equalsIgnoreCase(Backendless.UserService.CurrentUser().getObjectId())) {

                        Log.i("info", "hasTender");

                        hasTender = true;

                        dateObjectId = tender.getObjectId();

                        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

                        fab.hide();



                    }


                    final String url = StringUtil.splitString(ownerId);


                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            if (Looper.myLooper() == null)
                            {
                                Looper.prepare();
                            }
                            try {
                                theBitmap = Glide.
                                        with(HomeActivity.this).
                                        load("https://api.backendless.com/A0819152-C875-C222-FF18-0516AB9ACC00/v1/files/media/" + url + ".png").
                                        asBitmap().
                                        into(200, 200).
                                        get();
                            } catch (final ExecutionException e) {

                            } catch (final InterruptedException e) {

                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void dummy) {
                            if (null != theBitmap) {

                                Log.i("info", "bitmap success");

                                LatLng userPosition = new LatLng(tenderLat, tenderLong);
                                Bitmap newBitmap = bitmapUtil.getCircularBitmap(theBitmap);
                                int theColor = Color.parseColor("#C63D0F");
                                Bitmap newnewBitmap = bitmapUtil.addBorderToCircularBitmap(newBitmap, 15, theColor);
                                mMap.addMarker(new MarkerOptions().position(userPosition).icon(BitmapDescriptorFactory.fromBitmap(newnewBitmap)).snippet(ownerId));

                            }

                        }
                    }.execute();


                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // an error has occurred, the error code can be retrieved with fault.getCode()
            }
        });

    }





    @OnClick(R.id.fab)
    public void onFabClicked(View view) {

        if (hasTender != true) {


            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.custom_dialog);




            /*final EditText eReminderTime = (EditText) dialog.findViewById(R.id.editText3);
            eReminderTime.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(HomeActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            eReminderTime.setText( selectedHour + ":" + selectedMinute);
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                }
            });*/



            final CheckBox chk1 = (CheckBox) dialog.findViewById(R.id.checkBox1);
            final CheckBox chk2 = (CheckBox) dialog.findViewById(R.id.checkBox2);
            final CheckBox chk3 = (CheckBox) dialog.findViewById(R.id.checkBox3);



            chk1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        chk2.setChecked(false);
                        chk3.setChecked(false);
                        dateType = ((CheckBox) v).getTag().toString();
                    }


                }
            });

            chk2.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        chk1.setChecked(false);
                        chk3.setChecked(false);
                        dateType = ((CheckBox) v).getTag().toString();
                    }


                }
            });

            chk3.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (((CheckBox) v).isChecked()) {
                        chk1.setChecked(false);
                        chk2.setChecked(false);
                        dateType = ((CheckBox) v).getTag().toString();
                    }


                }
            });


            TextView dialogButton = (TextView) dialog.findViewById(R.id.textViewsubmit);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.i("info", dateType);

                    createTender(dateType);
                    dialog.dismiss();
                }
            });

            dialog.show();




        } else {

            Log.i("info", "tender already exists");

            Toast.makeText(HomeActivity.this, "You already have a pending offer",
                    Toast.LENGTH_LONG).show();
        }


    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_home;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng myLoc;


        Log.i("info", "inside map ready");

        if (ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            if(!mMap.isMyLocationEnabled())
                mMap.setMyLocationEnabled(true);

            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (myLocation == null) {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                String provider = lm.getBestProvider(criteria, true);
                myLocation = lm.getLastKnownLocation(provider);
            }

            if(myLocation!=null){
                Log.i("info", "not null location");

                userLat = myLocation.getLatitude();
                userLng = myLocation.getLongitude();

                myLoc = new LatLng(userLat, userLng);


            } else {

                myLoc = new LatLng(53.342842, -6.262122);

            }

            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11.0f));


        }

        mMap.setOnMarkerClickListener(this);

    }




    @Override
    public boolean onMarkerClick(final Marker marker) {

        Log.i("info", "marker clicked");

        Log.i("info", marker.getId());

        Log.i("info", marker.getSnippet());


        //String part = StringUtil.splitString(Backendless.UserService.CurrentUser().getObjectId());

        if (marker.getSnippet().equalsIgnoreCase(Backendless.UserService.CurrentUser().getObjectId())) {

            Log.i("info", "current user clicked");

            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.custom_dialog_delete);

            TextView dialogButton = (TextView) dialog.findViewById(R.id.textViewdelete);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Tender tender = new Tender();
                    tender.setObjectId(dateObjectId);

                    Backendless.Persistence.of(Tender.class).remove(tender,
                            new AsyncCallback<Long>() {
                                public void handleResponse(Long response) {
                                    Log.i("info", "delete success");

                                    marker.remove();
                                    hasTender = false;

                                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

                                    fab.show();
                                }

                                public void handleFault(BackendlessFault fault) {

                                    Log.i("info", "delete failed" + fault);

                                }
                            } );

                    dialog.dismiss();
                }
            });

            dialog.show();



        }




        /*Intent detailIntent = new Intent(this, ProfileActivity.class);
        detailIntent.putExtra(ProfileDetailFragment.ARG_ITEM_ID, 0);
        startActivity(detailIntent);

        Wrapper.markerId = marker.getTitle();

        Log.i("info", "get=" + marker.getTitle());*/




        /*Bid bid = new Bid();
        bid.setPickupline("Dinner?");
        bid.setTender(marker.getTitle());


        Backendless.Persistence.of(Bid.class).save(bid, new AsyncCallback<Bid>() {
            @Override
            public void handleResponse(Bid bid) {


                Log.i("info", "success post");


            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {

                Log.i("info", "failed post" + backendlessFault);
            }
        });*/




        return true;
    }


    public void createTender(String type) {

        Tender tender = new Tender();
        tender.setLongitude(userLng);
        tender.setLatitude(userLat);
        tender.setType(type);


        Backendless.Persistence.of(Tender.class).save(tender, new AsyncCallback<Tender>() {
            @Override
            public void handleResponse(final Tender tender) {


                Log.i("info", "success post");


                String currentId = Backendless.UserService.CurrentUser().getObjectId();
                String[] parts = currentId.split("-");
                final String part = parts[4];

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        if (Looper.myLooper() == null)
                        {
                            Looper.prepare();
                        }
                        try {
                            theBitmap = Glide.
                                    with(HomeActivity.this).
                                    load("https://api.backendless.com/A0819152-C875-C222-FF18-0516AB9ACC00/v1/files/media/" + part + ".png").
                                    asBitmap().
                                    into(200, 200).
                                    get();
                        } catch (final ExecutionException e) {

                        } catch (final InterruptedException e) {

                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void dummy) {
                        if (null != theBitmap) {

                            Log.i("info", "bitmap success");

                            LatLng userPosition = new LatLng(userLat, userLng);

                            Bitmap newBitmap = bitmapUtil.getCircularBitmap(theBitmap);
                            int theColor = Color.parseColor("#C63D0F");
                            Bitmap newnewBitmap = bitmapUtil.addBorderToCircularBitmap(newBitmap, 15, theColor);
                            mMap.addMarker(new MarkerOptions().position(userPosition).icon(BitmapDescriptorFactory.fromBitmap(newnewBitmap)).snippet(Backendless.UserService.CurrentUser().getObjectId()));

                            dateObjectId = tender.getObjectId();

                            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

                            fab.hide();
                        }
                        ;
                    }
                }.execute();




                hasTender = true;

            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {

                Log.i("info", "failed post" + backendlessFault);
            }
        });
    }




}
