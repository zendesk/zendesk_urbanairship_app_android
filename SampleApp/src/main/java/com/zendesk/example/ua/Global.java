package com.zendesk.example.ua;

import android.app.Application;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.notifications.DefaultNotificationFactory;
import com.zendesk.logger.Logger;


public class Global extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.setLoggable(true);
        initUrbanAirship();
    }

    private void initUrbanAirship() {

        // Load Urban Airship configuration from 'airshipconfig.properties'
        final AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(this);

        // Initialize Urban Airship
        UAirship.takeOff(this, options);

        // Enable push notification
        UAirship.shared().getPushManager().setUserNotificationsEnabled(true);

        // 'ic_conversations' should be displayed as notification icon
        final DefaultNotificationFactory defaultNotificationFactory = new DefaultNotificationFactory(getApplicationContext());
        defaultNotificationFactory.setSmallIconId(R.drawable.ic_conversations);
        UAirship.shared().getPushManager().setNotificationFactory(defaultNotificationFactory);

    }

}