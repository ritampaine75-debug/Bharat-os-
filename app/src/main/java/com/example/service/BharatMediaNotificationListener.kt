package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.util.DiagnosticsTracker
import com.example.util.MediaSessionTracker

/**
 * System NotificationListenerService required by Android's MediaSessionManager
 * to observe active media controllers across music and streaming applications.
 */
class BharatMediaNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        DiagnosticsTracker.recordEvent("MediaListener", "NotificationListenerService connected")
        MediaSessionTracker.onNotificationListenerConnected(applicationContext)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        DiagnosticsTracker.recordEvent("MediaListener", "NotificationListenerService disconnected")
        MediaSessionTracker.onNotificationListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        MediaSessionTracker.refreshSessions(applicationContext)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        MediaSessionTracker.refreshSessions(applicationContext)
    }
}
