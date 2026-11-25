package com.example.calculatorapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView rvCartItems;
    private View tvEmptyCart;
    private Button btnCheckout;
    private CartAdapter adapter;
    private List<CartItem> cartItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();

        ImageView btnBack = findViewById(R.id.btnBackFromCart);
        ImageView btnClearAll = findViewById(R.id.btnClearAllCart);
        rvCartItems = findViewById(R.id.rvCartItems);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        btnCheckout = findViewById(R.id.btnCheckout);

        rvCartItems.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        btnClearAll.setOnClickListener(v -> {
            if (cartItemList == null || cartItemList.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Xóa toàn bộ giỏ hàng")
                    .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng Firestore?")
                    .setPositiveButton("Xóa tất cả", (dialog, which) -> clearAllCart())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
        //chuyển sang thanh toán
        btnCheckout.setOnClickListener(v -> {
            if (cartItemList == null || cartItemList.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent checkoutIntent = new Intent(CartActivity.this, CheckoutActivity.class);
            CartItem firstItem = cartItemList.get(0);
            if (cartItemList.size() == 1) {
                checkoutIntent.putExtra("PRODUCT_NAME", firstItem.getProductName());
                checkoutIntent.putExtra("PRODUCT_PRICE", firstItem.getPrice());
            } else {
                checkoutIntent.putExtra("PRODUCT_NAME", firstItem.getProductName() + " (và " + (cartItemList.size() - 1) + " sản phẩm khác)");
                checkoutIntent.putExtra("PRODUCT_PRICE", calculateTotalPrice());
            }
            startActivity(checkoutIntent);
        });

        loadCartFromFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartFromFirestore();
    }
    //đọc du lieu
    private void loadCartFromFirestore() {
        db.collection("cart").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItemList = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String id = doc.getId();
                            String name = doc.getString("product_name");
                            String price = doc.getString("product_price");
                            cartItemList.add(new CartItem(id, name, price));
                        }
                    }

                    if (cartItemList.isEmpty()) {
                        tvEmptyCart.setVisibility(View.VISIBLE);
                        rvCartItems.setVisibility(View.GONE);
                    } else {
                        tvEmptyCart.setVisibility(View.GONE);
                        rvCartItems.setVisibility(View.VISIBLE);

                        adapter = new CartAdapter(cartItemList, new CartAdapter.OnCartItemActionListener() {
                            @Override
                            public void onDeleteItem(CartItem item) {
                                deleteSingleCartItem(item);
                            }

                            @Override
                            public void onEditItem(CartItem item) {
                                showEditCartItemDialog(item);
                            }
                        });
                        rvCartItems.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Lỗi nạp giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
    //xóa sp
    private void deleteSingleCartItem(CartItem item) {

        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn muốn xóa \"" + item.getProductName() + "\" khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("cart").document(item.getDocumentId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đã xóa sản phẩm khỏi giỏ hàng!", Toast.LENGTH_SHORT).show();
                                loadCartFromFirestore();
                            })
                            .addOnFailureListener(e ->
                                Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    //sua sp
    private void showEditCartItemDialog(CartItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa thông tin sản phẩm");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText edtName = new EditText(this);
        edtName.setHint("Tên sản phẩm");
        edtName.setText(item.getProductName());
        layout.addView(edtName);

        final EditText edtPrice = new EditText(this);
        edtPrice.setHint("Giá sản phẩm");
        edtPrice.setText(item.getPrice());
        layout.addView(edtPrice);

        builder.setView(layout);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String newName = edtName.getText().toString().trim();
            String newPrice = edtPrice.getText().toString().trim();

            if (newName.isEmpty() || newPrice.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("product_name", newName);
            updates.put("product_price", newPrice);

            db.collection("cart").document(item.getDocumentId()).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã cập nhật sản phẩm!", Toast.LENGTH_SHORT).show();
                        loadCartFromFirestore();
                    })
                    .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void clearAllCart() {
        db.collection("cart").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) return;

                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã xóa toàn bộ giỏ hàng trên Firestore!", Toast.LENGTH_SHORT).show();
                        loadCartFromFirestore();
                    });
                });
    }
    //tinh tien
    private String calculateTotalPrice() {
        long total = 0;
        if (cartItemList != null) {
            for (CartItem item : cartItemList) {
                try {
                    String cleanPrice = item.getPrice().replaceAll("[^0-9]", "");
                    if (!cleanPrice.isEmpty()) {
                        total += Long.parseLong(cleanPrice);
                    }
                } catch (Exception ignored) {}
            }
        }
        if (total <= 0) return "0 VNĐ";
        return String.format("%,d", total).replace(',', '.') + " VNĐ";
    }
}