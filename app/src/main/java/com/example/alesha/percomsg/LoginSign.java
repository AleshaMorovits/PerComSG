package com.example.alesha.percomsg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.microsoft.band.sensors.SampleRate;

public class LoginSign extends AppCompatActivity {

    private String TAG = "LOGIN";
    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button signInBtn, signUpBtn;
    private BandClient client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign);
        final WeakReference<Activity> reference = new WeakReference<Activity>(this);

        // Views
        mEmailField = findViewById(R.id.field_email);
        mPasswordField = findViewById(R.id.field_password);

        // Buttons
        signInBtn = (Button) findViewById(R.id.emailSignInBtn);
        signUpBtn = (Button) findViewById(R.id.emailSignUpBtn);

        mAuth = FirebaseAuth.getInstance();

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new HeartRateConsentTask().execute(reference);


            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginSign.this, SignUpActivity.class);
                startActivity(i);
            }
        });
}
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            //Find paired Bands
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

    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
        @Override
        protected Void doInBackground(WeakReference<Activity>... params) {

            try {

                if (getConnectedBandClient()) {

                    if (params[0].get() != null) {
                        client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean consentGiven) {
                                if(consentGiven== true){

                                    Toast.makeText(LoginSign.this, "Email: "+mEmailField.getText().toString()+" Password: "+ mPasswordField.getText().toString(),
                                            Toast.LENGTH_SHORT).show();

                                    mAuth.signInWithEmailAndPassword(mEmailField.getText().toString(), mPasswordField.getText().toString())
                                            .addOnCompleteListener(LoginSign.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {

                                                    if (task.isSuccessful()) {
                                                        // Sign in success, update UI with the signed-in user's information
                                                        Log.d(TAG, "signInWithEmail:success");
                                                        FirebaseUser user = mAuth.getCurrentUser();
                                                        if (user != null) {
                                                            String uid = user.getUid();
                                                            Toast.makeText(LoginSign.this, "UID: " + uid,
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                        Toast.makeText(LoginSign.this, "Authentication good.",
                                                                Toast.LENGTH_SHORT).show();
                                                        //updateUI(user);
                                                        Intent i = new Intent(LoginSign.this, UserProfile.class);
                                                        startActivity(i);
                                                        finish();
                                                    } else {
                                                        // If sign in fails, display a message to the user.
                                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                                        Toast.makeText(LoginSign.this, "Authentication failed.",
                                                                Toast.LENGTH_SHORT).show();
                                                        //updateUI(null);
                                                    }
                                                }
                                            });
                                }
                                else {
                                    Toast.makeText(LoginSign.this, "Consent is Needed", Toast.LENGTH_LONG).show();

                                }
                            }
                        });
                    }
                } else  {
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
                //Toast.makeText(getApplicationContext(),exceptionMessage, Toast.LENGTH_LONG).show();
            } catch (Exception e) {

            }
            return null;
        }
    }
}

