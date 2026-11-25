package com.example.calculatorapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvFullName, tvEmail, tvPhone, tvAddress;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUsername = findViewById(R.id.tvProfileUsername);
        tvFullName = findViewById(R.id.tvProfileFullName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvPhone = findViewById(R.id.tvProfilePhone);
        tvAddress = findViewById(R.id.tvProfileAddress);

        Button btnBack = findViewById(R.id.btnBackFromProfile);
        Button btnEdit = findViewById(R.id.btnEditProfile);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String user = prefs.getString("username", "Khách");
        String fullName = prefs.getString("user_fullname", "");
        String email = prefs.getString("user_email", "");
        String phone = prefs.getString("user_phone", "");
        String address = prefs.getString("user_address", "");

        tvUsername.setText(user);
        tvFullName.setText(fullName.isEmpty() ? "Chưa cập nhật" : fullName);
        tvEmail.setText(email.isEmpty() ? "Chưa cập nhật" : email);
        tvPhone.setText(phone.isEmpty() ? "Chưa cập nhật" : phone);
        tvAddress.setText(address.isEmpty() ? "Chưa cập nhật" : address);
    }
}