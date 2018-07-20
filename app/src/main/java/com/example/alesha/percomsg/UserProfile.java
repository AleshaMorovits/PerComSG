package com.example.alesha.percomsg;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.SampleRate;

import java.lang.ref.WeakReference;




public class UserProfile extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView firstTv,lastTv,bioTv,hobbyTv,chainTv,gestureTv;
    private Button editProfileBtn;
    private DatabaseReference mDatabase;
   // private String TAG = "UserProfile";
    private BandClient client = null;
    private static final String TAG = "BroadcastTest";
    private Intent intent;

    //private UserData user;

  /*  private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
            if(bandHeartRateEvent !=null){
                //appenToUI(String.format("Heart Rate = %d beats per minute\n"+"Quality = %s\n", bandHeartRateEvent.getHeartRate(),bandHeartRateEvent.getQuality()));
            }
        }
    };*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        firstTv = (TextView) findViewById(R.id.firstTv);
        lastTv = (TextView) findViewById(R.id.lastTv);
        bioTv = (TextView)findViewById(R.id.bioTv);
        hobbyTv = (TextView) findViewById(R.id.hobbyTv);
        editProfileBtn = (Button) findViewById(R.id.editProfileBtn);
        gestureTv = (TextView)findViewById(R.id.gestureTv);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        intent = new Intent(this, BandComm.class);

        // Write a message to the database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (user != null) {
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserData data = dataSnapshot.getValue(UserData.class);

                    if (data != null) {
                        firstTv.setText(data.getFirst_name());
                        lastTv.setText(data.getLast_name());
                        bioTv.setText(data.getBio());
                        hobbyTv.setText(data.getHobbyArr().toString());
                    }


                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                }
            };
            mDatabase.child("data").child(user.getUid().toString()).addListenerForSingleValueEvent(postListener);

        };

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserProfile.this, UserProfileData.class);
                startActivity(i);
            }
        });
       Intent intent = new Intent(this, BandComm.class);
        startService(intent);

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        startService(intent);
        registerReceiver(broadcastReceiver, new IntentFilter(BandComm.BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        stopService(intent);
    }
    private void updateUI(Intent intent) {
        String gesture = intent.getStringExtra("gesture");
        String chain = intent.getStringExtra("chain");
        Log.d(TAG, gesture);
        Log.d(TAG, chain);

        TextView gestureTv = (TextView) findViewById(R.id.gestureTv);
        TextView chainTv = (TextView) findViewById(R.id.chainTv);
        gestureTv.setText(gesture);
        chainTv.setText(chain);
    }


   /* private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params){
            try{
                if(getConnectedBandClient()){
                    if(client.getSensorManager().getCurrentHeartRateConsent()== UserConsent.GRANTED){
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                    }else {
                        Toast.makeText(UserProfile.this,"You have not given this application access to heart rate data yet; please press consent button to continue.\n",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(UserProfile.this,"Band is not connected, please make sure bluetooth is on and in range.\n",Toast.LENGTH_SHORT).show();
                }
            } catch (BandException e){
                Toast.makeText(UserProfile.this, e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                return false;
            }

            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }
        return ConnectionState.CONNECTED == client.connect().await();
    }
    private void appenToUI(final String string){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtStatus.setText(string);
            }
        });
    }

    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {


            if (event != null) {
                appenToUI(System.currentTimeMillis()+", " + String.valueOf(event.getAccelerationX()) + ", " +
                        String.valueOf(event.getAccelerationY()) + ", " + String.valueOf(event.getAccelerationZ()) + ", " + "\n");
            }
            else
            {
                //Toast.makeText(getApplicationContext(),"Band isn't connected. Please make sure bluetooth is on and the band is in range.", Toast.LENGTH_LONG).show();
            }
        }
    };

    private class AccelerometerSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {

                if (getConnectedBandClient()) {
                    //Toast.makeText(getApplicationContext(),"Band is connected.", Toast.LENGTH_LONG).show();
                    client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS16);
                } else {
                    //Toast.makeText(getApplicationContext(),"Band isn't connected. Please make sure bluetooth is on and the band is in range.", Toast.LENGTH_LONG).show();
                }
            } catch (BandException e) {
                String exceptionMessage = "";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                Toast.makeText(getApplicationContext(),exceptionMessage, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                    e.getMessage();
            }
            return null;
        }
    }
*/



















}



