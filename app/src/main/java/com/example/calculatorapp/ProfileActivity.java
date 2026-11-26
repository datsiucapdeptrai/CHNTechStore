package com.example.calculatorapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvFullName, tvEmail, tvPhone, tvAddress, tvServiceStatus;
    private SharedPreferences prefs;

    private OrderTrackingService orderTrackingService;
    private boolean isBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            OrderTrackingService.LocalBinder binder = (OrderTrackingService.LocalBinder) service;
            orderTrackingService = binder.getService();
            isBound = true;

            if (tvServiceStatus != null && orderTrackingService != null) {
                tvServiceStatus.setText("Trạng thái: Đã kết nối Bound Service (Tiến trình: " + orderTrackingService.getProgress() + "%)");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            orderTrackingService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUsername = findViewById(R.id.tvProfileUsername);
        tvFullName = findViewById(R.id.tvProfileFullName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvPhone = findViewById(R.id.tvProfilePhone);
        tvAddress = findViewById(R.id.tvProfileAddress);
        tvServiceStatus = findViewById(R.id.tvServiceStatus);

        Button btnBack = findViewById(R.id.btnBackFromProfile);
        Button btnEdit = findViewById(R.id.btnEditProfile);

        Button btnStartService = findViewById(R.id.btnStartServiceDemo);
        Button btnCheckService = findViewById(R.id.btnCheckServiceStatus);
        Button btnStopService = findViewById(R.id.btnStopServiceDemo);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Nút Khởi chạy Foreground Service
        if (btnStartService != null) {
            btnStartService.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrderTrackingService.class);
                intent.putExtra("ORDER_NAME", "Đơn hàng thử nghiệm Service");
                ContextCompat.startForegroundService(this, intent);
                tvServiceStatus.setText("Trạng thái: Bắt đầu Foreground Service...");
                Toast.makeText(this, "Đã khởi chạy OrderTrackingService!", Toast.LENGTH_SHORT).show();

                // Kết nối dạng Bound Service
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            });
        }

        // Nút Kiểm tra Bound Service
        if (btnCheckService != null) {
            btnCheckService.setOnClickListener(v -> {
                if (isBound && orderTrackingService != null) {
                    int currentProgress = orderTrackingService.getProgress();
                    String info = orderTrackingService.getCurrentOrderInfo();
                    tvServiceStatus.setText("Trạng thái: Đang theo dõi '" + info + "' (" + currentProgress + "%)");
                    Toast.makeText(this, "Đơn hàng: " + info + " - Tiến trình: " + currentProgress + "%", Toast.LENGTH_SHORT).show();
                } else {
                    tvServiceStatus.setText("Trạng thái: Chưa kết nối Bound Service");
                    Toast.makeText(this, "Service chưa được bật hoặc chưa kết nối!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Nút Dừng Service
        if (btnStopService != null) {
            btnStopService.setOnClickListener(v -> {
                if (isBound) {
                    unbindService(serviceConnection);
                    isBound = false;
                }
                Intent intent = new Intent(this, OrderTrackingService.class);
                stopService(intent);
                tvServiceStatus.setText("Trạng thái: Dịch vụ đã bị dừng");
                Toast.makeText(this, "Đã dừng Dịch vụ thành công!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, OrderTrackingService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
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