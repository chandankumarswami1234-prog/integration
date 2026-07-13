package com.socialapp.service.impl;

import com.socialapp.entity.Notification;
import com.socialapp.service.PushNotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Placeholder implementation: logs instead of actually pushing to a device.
 *
 * To wire up real push delivery via Firebase Cloud Messaging:
 * 1. Add the `firebase-admin` dependency to pom.xml
 * 2. Create a Firebase project, generate a service account JSON key
 * 3. Add a DeviceToken entity (user_id, fcm_token, platform) + registration endpoint
 *    so clients can register their device token after login
 * 4. Replace this class with one that calls FirebaseMessaging.getInstance().send(...)
 *
 * None of this can be fabricated without real Firebase project credentials, which
 * only the project owner can provide - see backend/README.md.
 */
@Service
@Slf4j
public class NoOpPushNotificationSender implements PushNotificationSender {

    @Override
    public void send(Notification notification) {
        log.debug("Push notification stub - would send {} notification to user {} (id={})",
                notification.getType(), notification.getRecipient().getId(), notification.getId());
    }
}
