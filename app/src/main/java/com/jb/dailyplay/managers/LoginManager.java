package com.jb.dailyplay.managers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.jb.dailyplay.R;
import com.jb.dailyplay.listeners.CheckUserCredentialsListener;
import com.jb.dailyplay.tasks.CheckUserCredentialsTask;
import com.jb.dailyplay.utils.DailyPlaySharedPrefUtils;
import com.jb.dailyplay.utils.SharedPref;
import com.jb.dailyplay.utils.StringUtils;

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
    private void login(final String username, final String password) {
        CheckUserCredentialsListener listener = new CheckUserCredentialsListener() {
            @Override
            public void onComplete(boolean isSuccessful) {
                if (isSuccessful) {
                    SharedPref.setString(DailyPlaySharedPrefUtils.PASSWORD, password);
                    SharedPref.setString(DailyPlaySharedPrefUtils.USERNAME, username);
                    Toast.makeText(mContext, "Login Successful", Toast.LENGTH_SHORT);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String title = !isTryAgain ? "Login" : "Login Failed.  Try Again";
        builder.setTitle(title);
        LayoutInflater inflater = mContext.getLayoutInflater();
        View view = inflater.inflate(R.layout.login_dialog, null);
        final EditText usernameEditText = (EditText) view.findViewById(R.id.login_email);
        final EditText passwordEditText = (EditText) view.findViewById(R.id.login_password);

        builder.setView(view);
        builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (StringUtils.isEmptyString(username) || !StringUtils.isValidEmail(username)) {
                    usernameEditText.setError("Please enter valid email address.");
                } else if (StringUtils.isEmptyString(password)) {
                    passwordEditText.setError("Please enter your password.");
                } else {
                    dialogInterface.dismiss();
                    login(username, password);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }
}
