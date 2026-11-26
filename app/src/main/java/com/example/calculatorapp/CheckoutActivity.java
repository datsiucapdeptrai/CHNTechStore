package com.example.calculatorapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CheckoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // 1. Ánh xạ View
        EditText edtName = findViewById(R.id.edtCheckoutName);
        EditText edtPhone = findViewById(R.id.edtCheckoutPhone);
        EditText edtAddress = findViewById(R.id.edtCheckoutAddress);
        TextView tvProductName = findViewById(R.id.tvCheckoutProductName);
        TextView tvPrice = findViewById(R.id.tvCheckoutPrice);
        TextView tvTotal = findViewById(R.id.tvTotalAmount);
        Button btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        // Ánh xạ nút Back
        ImageView imgBackCheckout = findViewById(R.id.imgBackCheckout);

        // 2. Nhận dữ liệu đơn hàng
        String productName = getIntent().getStringExtra("PRODUCT_NAME");
        String productPrice = getIntent().getStringExtra("PRODUCT_PRICE");

        tvProductName.setText(productName);
        tvPrice.setText(productPrice);
        tvTotal.setText("Tổng cộng: " + productPrice);

        // 3. Tự động nạp thông tin người dùng từ SharedPreferences (nếu có sẵn trong Profile)
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedFullName = prefs.getString("user_fullname", "");
        String savedPhone = prefs.getString("user_phone", "");
        String savedAddress = prefs.getString("user_address", "");
        String savedUsername = prefs.getString("username", "");

        // Ưu tiên nạp Họ và tên, nếu chưa có thì lấy tên tài khoản username
        if (!savedFullName.isEmpty()) {
            edtName.setText(savedFullName);
        } else if (!savedUsername.isEmpty()) {
            edtName.setText(savedUsername);
        }

        if (!savedPhone.isEmpty()) {
            edtPhone.setText(savedPhone);
        }

        if (!savedAddress.isEmpty()) {
            edtAddress.setText(savedAddress);
        }

        imgBackCheckout.setOnClickListener(v -> finish());

        // 4. Xử lý sự kiện bấm Đặt hàng
        btnPlaceOrder.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin giao hàng!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Bắt đầu Android Foreground Service để theo dõi đơn hàng ngầm (theo Slide and4.pdf)
            Intent serviceIntent = new Intent(this, OrderTrackingService.class);
            serviceIntent.putExtra("ORDER_NAME", productName != null ? productName : "Đơn hàng CHN Tech");
            ContextCompat.startForegroundService(this, serviceIntent);

            new AlertDialog.Builder(this)
                    .setTitle("Đặt Hàng Thành Công")
                    .setMessage("Cảm ơn " + name + "!\nĐơn hàng của bạn đang được xử lý ngầm (Android Foreground Service) và có thông báo hiển thị trên hệ thống!\n\n📍 Địa chỉ giao hàng: " + address + "\n📞 SĐT: " + phone + "\n💰 Tổng thanh toán: " + productPrice)
                    .setPositiveButton("Hoàn Tất", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        });
    }
}