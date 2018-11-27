package com.example.signalrclient

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), TextWatcher {
    private var mSocketService: SocketService? = null
    private var mIsBound = false
    val broadcastReceiver by lazy {
        NotificationBroadcastReceiver { doToast(it) }
    }

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mSocketService = (service as SocketService.LocalBinder).service
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mSocketService = null
        }

    }

    private fun doToast(message: String) {
        Log.i(TAG, "message=$message")
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnSend.isEnabled = etMessage.text.isNotEmpty()
        btnSend.setOnClickListener {
            val value = etMessage.text.toString()
            mSocketService?.sendMessage("SendMessage", value)
        }
        etMessage.addTextChangedListener(this)

        startAndBindService()
        val intentFilter = IntentFilter(NotificationBroadcastReceiver.FILTER_NOTIFICATION)
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun startAndBindService() {
        val socketServiceIntent = Intent(this, SocketService::class.java)
        startService(socketServiceIntent)
        bindService(socketServiceIntent, mConnection, Context.BIND_AUTO_CREATE)
        mIsBound = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mIsBound) {
            unbindService(mConnection)
            mIsBound = false
        }
        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(broadcastReceiver)
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        btnSend.isEnabled = count > 0
    }

    companion object {
        const val TAG = "Main Activity"
    }
}

class NotificationBroadcastReceiver(val callback: (message: String) -> Any) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra(EXTRA_NOTIFICATION)
        message?.let(callback)
    }

    companion object {
        const val EXTRA_NOTIFICATION = "EXTRA_NOTIFICATION"
        const val FILTER_NOTIFICATION = "FILTER_NOTIFICATION"
    }

}