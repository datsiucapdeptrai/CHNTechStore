package com.example.calculatorapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    Button btnLogin;
    TextView tvGoToRegister;
    CheckBox cbRememberMe;
    FirebaseFirestore db;
    SharedPreferences prefs;

    @Override
    //tạo giao diện , tự động điền tên đăng nhập đã lưu trong SP
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        db = FirebaseFirestore.getInstance();

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String savedUsername = prefs.getString("last_typed_username", "");
        if (!savedUsername.isEmpty()) {
            edtUsername.setText(savedUsername);
        }

        btnLogin.setOnClickListener(v -> {
            String user = edtUsername.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else {
                db.collection("users").document(user).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists() && pass.equals(documentSnapshot.getString("password"))) {
                                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                                SharedPreferences.Editor editor = prefs.edit();
                                if (cbRememberMe.isChecked()) {
                                    editor.putString("username", user);
                                    editor.putBoolean("isRemembered", true);
                                } else {
                                    editor.remove("username");
                                    editor.putBoolean("isRemembered", false);
                                }
                                editor.apply();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Lỗi kết nối Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        tvGoToRegister.setOnClickListener(v -> {
            String usernameHienTai = edtUsername.getText().toString().trim();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("last_typed_username", usernameHienTai);
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            intent.putExtra("USERNAME_GUI_SANG", usernameHienTai);
            startActivity(intent);
        });
    }
}