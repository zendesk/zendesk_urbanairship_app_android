package com.zendesk.example.ua;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.zendesk.logger.Logger;
import com.zendesk.sdk.feedback.impl.BaseZendeskFeedbackConfiguration;
import com.zendesk.sdk.feedback.ui.ContactZendeskActivity;
import com.zendesk.sdk.model.network.Identity;
import com.zendesk.sdk.model.network.PushRegistrationResponse;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.sdk.requests.RequestActivity;
import com.zendesk.sdk.support.SupportActivity;

import java.util.Locale;

import retrofit.client.Response;

import com.urbanairship.UAirship;
import com.urbanairship.google.PlayServicesUtils;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;
import com.zendesk.util.StringUtils;

/**
 * This activity is a springboard that you can use to launch various parts of the Zendesk SDK.
 */
public class MainActivity extends FragmentActivity {

    private static final String LOG_TAG = "MainActivity";

    private Identity getZendeskIdentity(){
        Identity user = null;

        // Anonymous:
        // user = new AnonymousIdentity.Builder()
        //      .build();

        // JWT:
        // user = new JwtIdentity("<jwt_id>");

        return user;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (PlayServicesUtils.isGooglePlayStoreAvailable()) {
            PlayServicesUtils.handleAnyPlayServicesError(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialises the SDK
        ZendeskConfig.INSTANCE.init(
                this,
                getString(R.string.zd_url),
                getString(R.string.zd_appid),
                getString(R.string.zd_oauth)
        );

        final Identity zendeskIdentity = getZendeskIdentity();
        if(zendeskIdentity == null){
            throw new RuntimeException(
                    "No valid identity configured. Please update 'MainActivity#getZendeskIdentity()'."
            );
        }
        ZendeskConfig.INSTANCE.setIdentity(getZendeskIdentity());

        //Setting Configuration for contact component
        ZendeskConfig.INSTANCE.setContactConfiguration(new SampleContactConfiguration(this));

        /**
         * This will make the RateMyApp dialog activity.
         */
        findViewById(R.id.main_btn_rate_my_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RateMyAppDialogTest.class));
            }
        });

        /**
         * This will make a full-screen feedback screen appear. It is very similar to how
         * the feedback dialog works but it is hosted in an activity.
         */
        findViewById(R.id.main_btn_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ContactZendeskActivity.class);
                startActivity(intent);
            }
        });

        /**
         * This will launch an Activity that will show the current Requests that a
         * user has opened.
         */
        findViewById(R.id.main_btn_request_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RequestActivity.class));
            }
        });

        final EditText supportEdittext = (EditText) findViewById(R.id.main_edittext_support);

        findViewById(R.id.main_btn_support).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String labels = supportEdittext.getText().toString();
                String[] labelsArray = null;

                if (StringUtils.hasLength(labels)) {
                    labelsArray = labels.split(",");
                }

                if (labelsArray != null) {

                    if (labelsArray.length == 1 && labelsArray[0].matches("-?\\d+")) {
                        SupportActivity.startActivity(MainActivity.this, Long.parseLong(labelsArray[0]));

                    } else {
                        SupportActivity.startActivity(MainActivity.this, labelsArray);

                    }

                } else {
                    Intent intent = new Intent(MainActivity.this, SupportActivity.class);
                    startActivity(intent);
                }
            }
        });

        final EditText devicePushToken = (EditText) findViewById(R.id.main_edittext_push);

        findViewById(R.id.main_btn_push_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZendeskConfig.INSTANCE.enablePushWithUAChannelId(devicePushToken.getText().toString(), new ZendeskCallback<PushRegistrationResponse>() {
                    @Override
                    public void onSuccess(PushRegistrationResponse result) {
                        Toast.makeText(getApplicationContext(), "Registration success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(ErrorResponse error) {
                        Toast.makeText(getApplicationContext(), "Registration failure: " + error.getReason(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        findViewById(R.id.main_btn_push_unregister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZendeskConfig.INSTANCE.disablePush(devicePushToken.getText().toString(), new ZendeskCallback<Response>() {
                    @Override
                    public void onSuccess(Response result) {
                        Toast.makeText(getApplicationContext(), "Deregistration success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(ErrorResponse error) {
                        Toast.makeText(getApplicationContext(), "Deregistration failure: " + error.getReason(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        /*
            The channelId could be null on initial app start:

            According to the Urban Airship docs:
                > The Channel ID is your application’s unique push identifier, and
                > is required in order to target a specific device when sending a push notification.
                > Don’t worry if this value initially comes back as null on your app’s first run
                > (or after clearing the application data),
                > as the Channel ID will be created and persisted during registration.
                > (http://docs.urbanairship.com/platform/android.html)
         */

        final String channelId = UAirship.shared().getPushManager().getChannelId();
        Logger.d(LOG_TAG, String.format(Locale.US, "Urban Airship - Channel Id %s", channelId));
        ((EditText) findViewById(R.id.main_edittext_push)).setText(channelId);
    }
}

/**
 * This class will configure the feedback dialog with the minimum amount of options that
 * are required.intent
 */
class SampleContactConfiguration extends BaseZendeskFeedbackConfiguration {

    public final transient Context mContext;

    public SampleContactConfiguration(Context context) {
        this.mContext = context;
    }

    @Override
    public String getRequestSubject() {

        /**
         * A request will normally have a shorter subject and a longer description. Here we are
         * specifying the subject that will be on the request that is created by the feedback
         * dialog.
         */
        return mContext.getString(R.string.rate_my_app_dialog_feedback_request_subject);
    }
}
