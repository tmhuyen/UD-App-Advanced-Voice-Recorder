package com.example.udapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NetworkCheck {

    public static void checkInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            pushNotification(context);
        }
    }

    private static void pushNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.drawable.ic_launcher_background) // replace with your own icon
                .setContentTitle("No Internet Connection")
                .setContentText("Please check your internet connection.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(0, builder.build());
    }
}