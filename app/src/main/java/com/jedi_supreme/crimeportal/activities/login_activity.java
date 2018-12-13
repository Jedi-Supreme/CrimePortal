package com.jedi_supreme.crimeportal.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.jedi_supreme.crimeportal.R;
import com.jedi_supreme.crimeportal.common;

public class login_activity extends AppCompatActivity {

    TextInputEditText et_login_email, et_login_password;
    TextInputLayout input_login_password;

    ProgressBar pro_barlogin;
    ConstraintLayout const_login_layout;

    //============================================ON CREATE==========================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);

        et_login_email = findViewById(R.id.et_login_email);
        et_login_password = findViewById(R.id.et_login_password);

        pro_barlogin = findViewById(R.id.pro_bar_loogin);
        const_login_layout = findViewById(R.id.login_layout);

        input_login_password = findViewById(R.id.input_login_password);

        et_login_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                input_login_password.setPasswordVisibilityToggleEnabled(hasFocus);
            }
        });

    }
    //============================================ON CREATE==========================================

    //-----------------------------------------BUTTON CLICK LISTENER--------------------------------
    public void enforcer_login(View view) {
        // test inputs -> sign in
        test_inputs();
    }
    //-----------------------------------------BUTTON CLICK LISTENER--------------------------------

    //=========================================METHODS==============================================
    void test_inputs(){

        if (et_login_email.getText().toString().isEmpty() || et_login_email.getText().toString().equals("")){
            common.snackbar(const_login_layout,"Enter Valid Email").show();
        }else if (et_login_password.getText().toString().isEmpty() || et_login_password.getText().toString().equals("")){
            common.snackbar(const_login_layout,"Enter Password").show();
        }else {
            String email = et_login_email.getText().toString();
            String password = et_login_password.getText().toString();

            sign_in(email,password);
        }

    }

    void sign_in(String email, final String password){

        pro_barlogin.setVisibility(View.VISIBLE);

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                //if sign in is successful and password is same as default password
                // then display password update dialog
                if (task.isSuccessful()) {

                    if (password.equals(common.default_pass())){

                        pro_barlogin.setVisibility(View.GONE);

                        AlertDialog.Builder update_pass_DialogBuilder = new AlertDialog.Builder(login_activity.this);

                        update_pass_DialogBuilder.setCancelable(false);

                        View pass_update_view =  LayoutInflater.from(login_activity.this)
                                .inflate(R.layout.frag_pass_update_layout,const_login_layout,false);

                        final TextInputEditText et_new_pass = pass_update_view.findViewById(R.id.et_new_pass);
                        final TextInputEditText et_conf_new_pass = pass_update_view.findViewById(R.id.et_conf_new_pass);

                        final TextInputLayout input_new_pass = pass_update_view.findViewById(R.id.inputlayout_new_pass),
                                input_conf_new_pass = pass_update_view.findViewById(R.id.inputlayout_conf_new_pass);

                        et_new_pass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                input_new_pass.setPasswordVisibilityToggleEnabled(hasFocus);
                            }
                        });

                        et_conf_new_pass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                input_conf_new_pass.setPasswordVisibilityToggleEnabled(hasFocus);
                            }
                        });
                        update_pass_DialogBuilder.setTitle("Update Password");
                        update_pass_DialogBuilder.setView(pass_update_view);

                        update_pass_DialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (et_new_pass.getText().toString().equals(et_conf_new_pass.getText().toString())){

                                    if (et_conf_new_pass.getText().toString().length() >= 8){

                                        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
                                            FirebaseAuth.getInstance().getCurrentUser()
                                                    .updatePassword(et_conf_new_pass.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){

                                                                String msg = "Password Changed Successfully. Please login to proceed";
                                                                common.snackbar(const_login_layout,msg).show();
                                                            }else {
                                                                String msg = "Password update failed. Please try again later";
                                                                common.snackbar(const_login_layout,msg).show();
                                                            }
                                                        }
                                                    });
                                        }

                                    }else {
                                        et_login_password.setText("");
                                        common.snackbar(const_login_layout,
                                                "Password too short. Minimum of 8 characters required.").show();
                                    }
                                }else {
                                    et_login_password.setText("");
                                    common.snackbar(const_login_layout,"Passwords do not match").show();
                                }
                            }
                        });

                        update_pass_DialogBuilder.create().show();
                    }else {
                        //login user
                        toDashboard();
                    }



                } else {
                    // If sign in fails, display a message to the user.
                    //Log.w(TAG, "signInWithEmail:failure", task.getException());

                    pro_barlogin.setVisibility(View.GONE);

                    if (task.getException() instanceof FirebaseAuthInvalidUserException){
                        common.snackbar(const_login_layout,"Email not Found").show();

                    }else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        common.snackbar(const_login_layout,"Wrong Password").show();
                    }

                }

            }
        });

    }

    void toDashboard(){

        pro_barlogin.setVisibility(View.GONE);

        Intent dashboard_intent = new Intent(getApplicationContext(),Dashboard.class);
        startActivity(dashboard_intent);
        super.finish();
    }

    //=========================================METHODS==============================================
}
