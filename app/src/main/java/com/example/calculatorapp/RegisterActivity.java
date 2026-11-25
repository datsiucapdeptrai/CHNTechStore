package com.example.calculatorapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    Button btnRegister;
    TextView tvBackToLogin;
    FirebaseFirestore db;

    @Override
    //nhận tên đăng nhập từ login,kiểm tra thông tin và tạo tk moi tren firestore
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtUsername = findViewById(R.id.edtRegUsername);
        edtPassword = findViewById(R.id.edtRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        db = FirebaseFirestore.getInstance();

        String tenTuTrangLogin = getIntent().getStringExtra("USERNAME_GUI_SANG");
        if (tenTuTrangLogin != null && !tenTuTrangLogin.isEmpty()) {
            edtUsername.setText(tenTuTrangLogin);
        }

        tvBackToLogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String user = edtUsername.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("username", user);
                userMap.put("password", pass);

                db.collection("users").document(user).set(userMap)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            prefs.edit().putString("last_typed_username", user).apply();

                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi đăng ký: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}