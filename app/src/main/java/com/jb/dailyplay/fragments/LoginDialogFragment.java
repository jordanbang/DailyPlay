package com.jb.dailyplay.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jb.dailyplay.R;
import com.jb.dailyplay.managers.LoginManager;
import com.jb.dailyplay.utils.StringUtils;

/**
 * Created by jordanbangia on 10/28/14.
 */
public class LoginDialogFragment extends DialogFragment {
    private EditText mPasswordEditText;
    private EditText mUserEditText;

    private static class BundleArgs {
        static final String TRY_AGAIN = "tryagain";
    }

    public static LoginDialogFragment newInstance(boolean isTryAgain) {
        LoginDialogFragment fragment = new LoginDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(BundleArgs.TRY_AGAIN, isTryAgain);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isTryAgain = getArguments().getBoolean(BundleArgs.TRY_AGAIN);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String title = !isTryAgain ? "Login" : "Login Failed.  Try Again";
        builder.setTitle(title);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.login_dialog, null);
        mUserEditText = (EditText) view.findViewById(R.id.login_email);
        mPasswordEditText = (EditText) view.findViewById(R.id.login_password);

        builder.setView(view);
        builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = mUserEditText.getText().toString();
                    String password = mPasswordEditText.getText().toString();

                    if (StringUtils.isEmptyString(username) || !StringUtils.isValidEmail(username)) {
                        mUserEditText.setError("Please enter valid email address.");
                    } else if (StringUtils.isEmptyString(password)) {
                        mPasswordEditText.setError("Please enter your password.");
                    } else {
                        getActivity().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                        dismiss();
                        LoginManager.getManager(getActivity()).login(username, password);
                    }
                }
            });
        }
    }
}
