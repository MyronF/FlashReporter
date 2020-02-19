package myron.com.crimereporter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText emailID, password;
    Button btnSignUp;
    TextView tvSignIn;
    FirebaseAuth mFirebaseAuth;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        emailID = findViewById(R.id.input_EmailR);
        password = findViewById(R.id.input_passwordR);

        btnSignUp = findViewById(R.id.btnRegister);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
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
                    Toast.makeText(RegisterActivity.this, "Fields are empty", Toast.LENGTH_SHORT).show();

                }else if (!email.isEmpty() && pwd.isEmpty()){
                    mFirebaseAuth.createUserWithEmailAndPassword(email,pwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Registration Unsuccessful, Try again", Toast.LENGTH_SHORT).show();
                            }else{
                                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            }
                        }
                    });
                }else{
                    Toast.makeText(RegisterActivity.this, "Error Occurred, Try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        tvSignIn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(RegisterActivity.this, HomeActivity.class);
//                startActivity(i);
//            }
//        });

    }
}
