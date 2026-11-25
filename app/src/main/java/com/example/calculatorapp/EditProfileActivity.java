package com.example.calculatorapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        EditText edtFullName = findViewById(R.id.edtNewFullName);
        EditText edtEmail = findViewById(R.id.edtNewEmail);
        EditText edtPhone = findViewById(R.id.edtNewPhone);
        EditText edtAddress = findViewById(R.id.edtNewAddress);

        Button btnSave = findViewById(R.id.btnSaveChanges);
        Button btnCancel = findViewById(R.id.btnCancelEdit);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Load dữ liệu hiện tại
        String username = prefs.getString("username", "");
        edtFullName.setText(prefs.getString("user_fullname", ""));
        edtEmail.setText(prefs.getString("user_email", ""));
        edtPhone.setText(prefs.getString("user_phone", ""));
        edtAddress.setText(prefs.getString("user_address", ""));

        btnSave.setOnClickListener(v -> {
            String newFullName = edtFullName.getText().toString().trim();
            String newEmail = edtEmail.getText().toString().trim();
            String newPhone = edtPhone.getText().toString().trim();
            String newAddress = edtAddress.getText().toString().trim();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_fullname", newFullName);
            editor.putString("user_email", newEmail);
            editor.putString("user_phone", newPhone);
            editor.putString("user_address", newAddress);
            editor.apply();

            // Đồng bộ lên Firebase Firestore nếu đã đăng nhập
            if (!username.isEmpty()) {
                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("fullname", newFullName);
                userUpdates.put("email", newEmail);
                userUpdates.put("phone", newPhone);
                userUpdates.put("address", newAddress);

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(username)
                        .update(userUpdates);
            }

            Toast.makeText(this, "Đã cập nhật thông tin hồ sơ!", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}