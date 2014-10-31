package com.jb.dailyplay.managers;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.jb.dailyplay.R;
import com.jb.dailyplay.fragments.LoginDialogFragment;
import com.jb.dailyplay.listeners.CheckUserCredentialsListener;
import com.jb.dailyplay.tasks.CheckUserCredentialsTask;
import com.jb.dailyplay.utils.DailyPlaySharedPrefUtils;
import com.jb.dailyplay.utils.SharedPref;

/**
 * Created by jordanbangia on 10/27/14.
 * Manager exists for a context.  It should only be used within the context its given.
 * Not sure if this is the correct paradigm.
 *
 *
 * Logic:
 *  - save password and username temporarily
 *  - try logging in
 *      - if it works
 *          - save info
 *      - else
 *          - prompt again
 *
 *
 */
public class LoginManager {
    private Activity mContext;

    /**
     * Gets the manager for doing login logic.
     * @param context
     * @return
     */
    public static LoginManager getManager(Activity context) {
        return new LoginManager(context);
    }

    private LoginManager(Activity context) {
        mContext = context;
    }


    /**
     * Starts the login task.  Will run on a background thread.
     * If login is successful, information will be saved, otherwise will be reprompted.
     * @param username
     * @param password
     */
    public void login(final String username, final String password) {
        CheckUserCredentialsListener listener = new CheckUserCredentialsListener() {
            @Override
            public void onComplete(boolean isSuccessful) {
                mContext.findViewById(R.id.progressBar).setVisibility(View.GONE);
                if (isSuccessful) {
                    SharedPref.setString(DailyPlaySharedPrefUtils.PASSWORD, password);
                    SharedPref.setString(DailyPlaySharedPrefUtils.USERNAME, username);
                    Toast.makeText(mContext, "Login Successful", Toast.LENGTH_LONG).show();
                } else {
                    promptForUserInformation(true);
                }
            }
        };
        new CheckUserCredentialsTask().execute(listener, username, password);
    }

    /**
     * Ignores the checks for current login information.  Should be used in the case of trying
     * to get new information (i.e switch accounts).
     */
    public void promptForNewUserInformation() {
        promptForUserInformation(false);
    }

    /**
     * Prompts the user for login information, if no login information exists.
     * Used at the beginning of the app to get login information.
     */
    public void promptForUserInformationIfNoneExists() {
        if (DailyPlaySharedPrefUtils.doesUserInformationExist()) {
            return;
        }
        promptForUserInformation(false);
    }

    /**
     * The basic prompt.  Will display prompt for getting password.
     * @param isTryAgain If true, then this is a "try again" at inputting password information.  Text will change.
     */
    public void promptForUserInformation(boolean isTryAgain) {
        LoginDialogFragment fragment = LoginDialogFragment.newInstance(isTryAgain);
        fragment.show(mContext.getFragmentManager(), "dialog");
    }
}
