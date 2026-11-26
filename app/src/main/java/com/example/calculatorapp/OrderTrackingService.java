package com.example.calculatorapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * Service thực thi các kiến thức từ Slide Android Services (and4.pdf):
 * 1. Foreground Service: Chạy ẩn kèm thông báo hệ thống (Music, Đơn hàng, GPS)
 * 2. Service Lifecycle: onCreate() -> onStartCommand() -> onBind() -> onDestroy()
 * 3. Bound Service: Cho phép Activity kết nối (bindService) và tương tác với Service
 */
public class OrderTrackingService extends Service {

    public static final String CHANNEL_ID = "CHN_TECH_ORDER_SERVICE_CHANNEL";
    public static final int NOTIFICATION_ID = 1001;
    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    private static final String TAG = "OrderTrackingService";

    private final IBinder binder = new LocalBinder();
    private Handler handler;
    private Runnable progressRunnable;
    private int progress = 0;
    private boolean isRunning = false;
    private String currentOrderInfo = "Đơn hàng CHN Tech";

    // Binder class cho Bound Service
    public class LocalBinder extends Binder {
        public OrderTrackingService getService() {
            return OrderTrackingService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Service được khởi tạo");
        createNotificationChannel();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Nhận lệnh bắt đầu Service");

        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_STOP_SERVICE.equals(action)) {
                stopTrackingService();
                return START_NOT_STICKY;
            }

            String orderName = intent.getStringExtra("ORDER_NAME");
            if (orderName != null && !orderName.isEmpty()) {
                currentOrderInfo = orderName;
            }
        }

        // Tạo Notification cho Foreground Service
        Notification notification = buildNotification("Đang xử lý & đóng gói đơn hàng...", progress);
        startForeground(NOTIFICATION_ID, notification);
        isRunning = true;

        // Bắt đầu mô phỏng tiến trình chạy ngầm
        startProgressSimulation();

        return START_STICKY; // Đảm bảo Service tự động khôi phục nếu hệ thống kill
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: Activity đã kết nối (bindService)");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: Activity đã ngắt kết nối (unbindService)");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (handler != null && progressRunnable != null) {
            handler.removeCallbacks(progressRunnable);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        Log.d(TAG, "onDestroy: Service bị hủy");
        super.onDestroy();
    }

    // --- Phương thức công khai cho Bound Service gọi ---
    public int getProgress() {
        return progress;
    }

    public boolean isServiceRunning() {
        return isRunning;
    }

    public String getCurrentOrderInfo() {
        return currentOrderInfo;
    }

    public void stopTrackingService() {
        Toast.makeText(this, "Đã dừng Dịch vụ theo dõi đơn hàng", Toast.LENGTH_SHORT).show();
        stopSelf();
    }

    // Mô phỏng tiến trình cập nhật đơn hàng chạy ngầm
    private void startProgressSimulation() {
        if (progressRunnable != null) {
            handler.removeCallbacks(progressRunnable);
        }

        progress = 0;
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;

                progress += 10;
                String statusText = "Đang giao hàng (" + progress + "%)";
                if (progress >= 100) {
                    statusText = "Giao hàng thành công!";
                    updateNotification(statusText, 100);
                    Toast.makeText(OrderTrackingService.this, "Đơn hàng " + currentOrderInfo + " đã được giao thành công!", Toast.LENGTH_LONG).show();
                    return;
                }

                updateNotification(statusText, progress);
                handler.postDelayed(this, 3000); // Cập nhật mỗi 3 giây
            }
        };
        handler.post(progressRunnable);
    }

    private void updateNotification(String statusText, int currentProgress) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            try {
                manager.notify(NOTIFICATION_ID, buildNotification(statusText, currentProgress));
            } catch (SecurityException ignored) {
                // Ignore if POST_NOTIFICATIONS permission not granted
            }
        }
    }

    private Notification buildNotification(String statusText, int currentProgress) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent stopIntent = new Intent(this, OrderTrackingService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 0, stopIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("CHN Tech - " + currentOrderInfo)
                .setContentText(statusText)
                .setSmallIcon(R.drawable.ic_truck)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setProgress(100, currentProgress, false)
                .addAction(R.drawable.ic_delete, "Dừng Dịch Vụ", stopPendingIntent)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Kênh Dịch Vụ Theo Dõi Đơn Hàng CHN Tech",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Hiển thị thông báo trạng thái đơn hàng chạy ngầm");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}