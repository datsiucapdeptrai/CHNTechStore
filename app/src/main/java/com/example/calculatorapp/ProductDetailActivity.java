package com.example.calculatorapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private int quantity = 1;
    private boolean isFavorite = false;
    private String selectedColor = "Titan Tự Nhiên";
    private String selectedCapacity = "256 GB";

    private TextView tvQuantity;
    private ImageView imgFavoriteIcon;

    private TextView chipColor1, chipColor2, chipColor3;
    private TextView chipCap1, chipCap2, chipCap3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = FirebaseFirestore.getInstance();

        ImageView img = findViewById(R.id.imgDetailProduct);
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvOldPrice = findViewById(R.id.tvDetailOldPrice);
        TextView tvDesc = findViewById(R.id.tvDetailDesc);

        View btnBack = findViewById(R.id.btnBackFromDetail);
        ImageView btnShareHeader = findViewById(R.id.btnShareHeader);
        View btnCartHeader = findViewById(R.id.btnCartHeader);
        View btnFavorite = findViewById(R.id.btnFavorite);
        imgFavoriteIcon = findViewById(R.id.imgFavoriteIcon);

        View btnQuantityMinus = findViewById(R.id.btnQuantityMinus);
        View btnQuantityPlus = findViewById(R.id.btnQuantityPlus);
        tvQuantity = findViewById(R.id.tvQuantity);

        chipColor1 = findViewById(R.id.chipColor1);
        chipColor2 = findViewById(R.id.chipColor2);
        chipColor3 = findViewById(R.id.chipColor3);

        chipCap1 = findViewById(R.id.chipCap1);
        chipCap2 = findViewById(R.id.chipCap2);
        chipCap3 = findViewById(R.id.chipCap3);

        Button btnAddToCart = findViewById(R.id.btnAddToCart);
        Button btnBuyNow = findViewById(R.id.btnBuyNow);

        View btnBottomChat = findViewById(R.id.btnBottomChat);
        Button btnChatShop = findViewById(R.id.btnChatShop);
        Button btnViewShop = findViewById(R.id.btnViewShop);
        TextView btnCollectVoucher = findViewById(R.id.btnCollectVoucher);

        // Strikethrough cho giá cũ
        if (tvOldPrice != null) {
            tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        Intent incomingIntent = getIntent();
        String name = incomingIntent.getStringExtra("PRODUCT_NAME");
        String price = incomingIntent.getStringExtra("PRODUCT_PRICE");
        String desc = incomingIntent.getStringExtra("PRODUCT_DESC");
        int imageResId = incomingIntent.getIntExtra("PRODUCT_IMAGE", R.mipmap.ic_launcher);

        if (name != null) tvName.setText(name);
        if (price != null) tvPrice.setText(price);
        if (desc != null && !desc.isEmpty()) {
            tvDesc.setText(desc);
        } else {
            tvDesc.setText("Sản phẩm chính hãng với thiết kế hiện đại, hiệu năng mạnh mẽ, bảo hành 12 tháng tại hệ thống cửa hàng trên toàn quốc.");
        }
        img.setImageResource(imageResId);

        // Nút quay lại
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Nút chia sẻ
        if (btnShareHeader != null) {
            btnShareHeader.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, name);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Tham khảo ngay sản phẩm " + name + " bán tại CHN Tech với giá " + price);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ sản phẩm"));
            });
        }

        // Nút Giỏ hàng trên Header
        if (btnCartHeader != null) {
            btnCartHeader.setOnClickListener(v -> {
                Intent cartIntent = new Intent(ProductDetailActivity.this, CartActivity.class);
                startActivity(cartIntent);
            });
        }

        // Nút Yêu thích (Favorite)
        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                isFavorite = !isFavorite;
                if (isFavorite) {
                    imgFavoriteIcon.setColorFilter(Color.parseColor("#FF385C"));
                    Toast.makeText(this, "Đã thêm vào danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                } else {
                    imgFavoriteIcon.setColorFilter(Color.parseColor("#94A3B8"));
                    Toast.makeText(this, "Đã bỏ khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Tăng/Giảm số lượng
        if (btnQuantityMinus != null) {
            btnQuantityMinus.setOnClickListener(v -> {
                if (quantity > 1) {
                    quantity--;
                    tvQuantity.setText(String.valueOf(quantity));
                }
            });
        }

        if (btnQuantityPlus != null) {
            btnQuantityPlus.setOnClickListener(v -> {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
            });
        }

        // Chọn Màu sắc
        setupColorSelection();

        // Chọn Dung lượng
        setupCapacitySelection();

        // Nút lưu voucher
        if (btnCollectVoucher != null) {
            btnCollectVoucher.setOnClickListener(v ->
                Toast.makeText(this, "Đã lưu tất cả mã giảm giá vào kho voucher!", Toast.LENGTH_SHORT).show()
            );
        }

        // Nút Chat Shop
        View.OnClickListener chatListener = v ->
            Toast.makeText(this, "Đang kết nối với nhân viên tư vấn CHN Tech...", Toast.LENGTH_SHORT).show();

        if (btnBottomChat != null) btnBottomChat.setOnClickListener(chatListener);
        if (btnChatShop != null) btnChatShop.setOnClickListener(chatListener);

        if (btnViewShop != null) {
            btnViewShop.setOnClickListener(v ->
                Toast.makeText(this, "Đang mở cửa hàng CHN Tech Official Store", Toast.LENGTH_SHORT).show()
            );
        }

        // Thêm vào giỏ hàng
        btnAddToCart.setOnClickListener(v -> {
            Map<String, Object> cartItem = new HashMap<>();
            String fullName = (name != null ? name : "Sản phẩm") + " (" + selectedColor + ", " + selectedCapacity + ")";
            cartItem.put("product_name", fullName);
            cartItem.put("product_price", price != null ? price : "0 VNĐ");
            cartItem.put("quantity", quantity);

            db.collection("cart").add(cartItem)
                    .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Đã thêm " + quantity + " x " + name + " vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi thêm vào giỏ: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // Mua ngay
        btnBuyNow.setOnClickListener(v -> {
            Intent checkoutIntent = new Intent(ProductDetailActivity.this, CheckoutActivity.class);
            String fullName = (name != null ? name : "Sản phẩm") + " (" + selectedColor + ", " + selectedCapacity + " - SL: " + quantity + ")";
            checkoutIntent.putExtra("PRODUCT_NAME", fullName);
            checkoutIntent.putExtra("PRODUCT_PRICE", price);
            startActivity(checkoutIntent);
        });
    }

    private void setupColorSelection() {
        if (chipColor1 == null || chipColor2 == null || chipColor3 == null) return;

        chipColor1.setOnClickListener(v -> {
            selectedColor = chipColor1.getText().toString();
            updateChipUI(chipColor1, chipColor2, chipColor3);
        });

        chipColor2.setOnClickListener(v -> {
            selectedColor = chipColor2.getText().toString();
            updateChipUI(chipColor2, chipColor1, chipColor3);
        });

        chipColor3.setOnClickListener(v -> {
            selectedColor = chipColor3.getText().toString();
            updateChipUI(chipColor3, chipColor1, chipColor2);
        });
    }

    private void setupCapacitySelection() {
        if (chipCap1 == null || chipCap2 == null || chipCap3 == null) return;

        chipCap1.setOnClickListener(v -> {
            selectedCapacity = chipCap1.getText().toString();
            updateChipUI(chipCap1, chipCap2, chipCap3);
        });

        chipCap2.setOnClickListener(v -> {
            selectedCapacity = chipCap2.getText().toString();
            updateChipUI(chipCap2, chipCap1, chipCap3);
        });

        chipCap3.setOnClickListener(v -> {
            selectedCapacity = chipCap3.getText().toString();
            updateChipUI(chipCap3, chipCap1, chipCap2);
        });
    }

    private void updateChipUI(TextView selected, TextView unselected1, TextView unselected2) {
        selected.setBackgroundResource(R.drawable.bg_chip_selected);
        selected.setTextColor(Color.WHITE);

        unselected1.setBackgroundResource(R.drawable.bg_chip_unselected);
        unselected1.setTextColor(Color.parseColor("#1D1B20"));

        unselected2.setBackgroundResource(R.drawable.bg_chip_unselected);
        unselected2.setTextColor(Color.parseColor("#1D1B20"));
    }
}