package myron.com.crimereporter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//https://www.youtube.com/watch?v=V0ZrnL-i77Q
public class MainActivity extends AppCompatActivity {

    EditText emailID, password;
    Button btnSignIn;
    TextView btnSignUp;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        emailID = findViewById(R.id.input_Email);
        password = findViewById(R.id.input_Password);

        btnSignIn = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btn_Register);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();

                if (mFirebaseUser!= null){
                    Toast.makeText(MainActivity.this, "You are Logged in", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(i);

                }else{
                    Toast.makeText(MainActivity.this, "Login failed! Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailID.getText().toString();
                String pwd = password.getText().toString();

                if(email.isEmpty()){
                    emailID.setError("Please enter Email");
                    emailID.requestFocus();

                }else if(pwd.isEmpty()){
                    password.setError("Please enter Password");
                    password.requestFocus();

                }else if (email.isEmpty() && pwd.isEmpty()){
                    Toast.makeText(MainActivity.this, "Fields are empty", Toast.LENGTH_SHORT).show();

                }else if (!email.isEmpty() && !pwd.isEmpty()){
                    mFirebaseAuth.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Login Unsuccessful, Try again", Toast.LENGTH_SHORT).show();
                            }else{
                                Log.d("LoginPage","login successful");
                                Intent LoginToHome = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(LoginToHome);
                            }
                        }
                    });
                }else{
                    Log.d("LoginPage","login Unsuccessful");
                    Toast.makeText(MainActivity.this, "Error Occurred, Try again", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent (MainActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
}
