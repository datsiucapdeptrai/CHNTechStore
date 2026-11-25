package com.example.calculatorapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imgProfile = findViewById(R.id.imgProfileIcon);
        ImageView imgCart = findViewById(R.id.imgCartIcon);

        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        imgCart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        List<Product> productList = new ArrayList<>();

        // Nạp các hình ảnh thực tế từ res/drawable (iphone15_promax.png, samsung_s24.png, xiaomi14.png)
        productList.add(new Product("iPhone 15 Pro Max", "30.000.000 VNĐ", R.drawable.iphone15_promax, "Điện thoại cao cấp nhất của Apple với khung Titan và camera 48MP."));
        productList.add(new Product("Samsung Galaxy S24", "25.000.000 VNĐ", R.drawable.samsung_s24, "Màn hình phẳng, tích hợp bút S-Pen và nhiều tính năng AI thông minh."));
        productList.add(new Product("Xiaomi 14", "18.000.000 VNĐ", R.drawable.xiaomi14, "Hiệu năng mạnh mẽ với Snapdragon 8 Gen 3, camera Leica."));
        productList.add(new Product("Oppo Find X7", "20.000.000 VNĐ", R.drawable.ic_phone, "Thiết kế sang trọng, sạc nhanh 100W và camera tele chất lượng."));

        ProductAdapter adapter = new ProductAdapter(productList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}