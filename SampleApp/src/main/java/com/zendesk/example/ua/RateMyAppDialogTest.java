package com.zendesk.example.ua;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.zendesk.sdk.R;
import com.zendesk.sdk.feedback.BaseZendeskFeedbackConfiguration;
import com.zendesk.sdk.network.SubmissionListenerAdapter;
import com.zendesk.sdk.rating.ui.RateMyAppDialog;

import java.io.Serializable;

/**
 * This activity demonstrates the RateMyApp dialog.
 * <p>
 *     The RateMyApp dialog's main function is to allow your users to rate your app.  It can also
 *     be configured to have other buttons that you can define yourself.
 * </p>
 */
public class RateMyAppDialogTest extends FragmentActivity implements Serializable {

    private RateMyAppDialog mRateMyAppDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_my_app_dialog_test);

        /**
         * This builder will allow you to customise the RateMyApp dialog.
         */

        mRateMyAppDialog = new RateMyAppDialog.Builder(this)
                // Adds a button. When tapped goes to this App's Play Store page
                .withAndroidStoreRatingButton()

                // Adds a button. When tapped opens a dialog to send feedback.
                .withSendFeedbackButton(new BaseZendeskFeedbackConfiguration() {
                    @Override
                    public String getRequestSubject() {
                        return "RMA";
                    }
                }, new ReshowFeedbackListener())

                // Adds a button. When tapped dismisses the dialog and sets a flag to not show it again
                .withDontRemindMeAgainButton()
                .build();

        // Shows the dialog if the configuration permits it
        mRateMyAppDialog.show(this);
    }

    /**
     * This class is used in conjunction with the send feedback button. If the user dismisses the
     * feedback icon this listener is used to show the RateMyApp dialog again
     */
    class ReshowFeedbackListener extends SubmissionListenerAdapter {

        @Override
        public void onSubmissionCancel() {
            if (mRateMyAppDialog != null) {
                mRateMyAppDialog.show(RateMyAppDialogTest.this, true);
            }
        }
    }
}
