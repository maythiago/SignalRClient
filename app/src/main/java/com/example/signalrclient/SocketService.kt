package com.example.signalrclient

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.smartarmenia.dotnetcoresignalrclientjava.HubConnection
import com.smartarmenia.dotnetcoresignalrclientjava.HubConnectionListener
import com.smartarmenia.dotnetcoresignalrclientjava.HubMessage
import com.smartarmenia.dotnetcoresignalrclientjava.WebSocketHubConnectionP2
import java.lang.Exception
import java.lang.RuntimeException

class SocketService : Service(), HubConnectionListener {
    private val myBinder = LocalBinder()
    private val connection: HubConnection =
        WebSocketHubConnectionP2("$PROTOCOL_TYPE://$SERVER_URL/notificationHub", "")

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "I am in Ibinder onBind method")
        return myBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        if (connection.isConnected) {
            connection.disconnect()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        println("I am in on start")
        //  Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
        val connect = connectSocket()
        Thread(connect).start()
        connection.addListener(this)
        return START_STICKY
    }

    fun sendMessage(event: String, message: String) {
        try {
            connection.invoke(event, "Android", message)
        } catch (e: RuntimeException) {
            onError(e)
        }
    }


    override fun onConnected() {
        Log.i(TAG, "Connected")
    }

    override fun onMessage(message: HubMessage?) {
        val intent = Intent(NotificationBroadcastReceiver.FILTER_NOTIFICATION)
        val result = "${message?.target}\n${Gson().toJson(message?.arguments)}"
        intent.putExtra(NotificationBroadcastReceiver.EXTRA_NOTIFICATION, result)
        LocalBroadcastManager
            .getInstance(this)
            .sendBroadcast(intent)
        Log.i(TAG, "Message: $result")
    }

    override fun onDisconnected() {
        Log.i(TAG, "Disconnected")
    }

    override fun onError(exception: Exception?) {
        Log.i(TAG, "Error: ${exception?.message}")
    }

    internal inner class connectSocket : Runnable {
        override fun run() {
            connection.connect()
        }
    }

    inner class LocalBinder : Binder() {
        val service: SocketService
            get() {
                Log.i(TAG, "I am in Localbinder ")
                return this@SocketService

            }
    }

    companion object {
        const val TAG = "SocketService"
        val SERVER_URL = "90.0.1.209:5000"
        val PROTOCOL_TYPE = "http"
    }


}