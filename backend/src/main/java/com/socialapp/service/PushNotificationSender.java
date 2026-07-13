package com.socialapp.service;

import com.socialapp.entity.Notification;

/**
 * Abstraction for actually pushing a notification to a device (FCM, APNs, etc).
 * Kept separate from NotificationService so the in-app notification list works
 * fully on its own regardless of whether push delivery is wired up.
 */
public interface PushNotificationSender {
    void send(Notification notification);
}
