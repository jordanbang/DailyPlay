package com.daily.play.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.daily.play.R;
import com.daily.play.managers.LoginManager;
import com.daily.play.utils.DailyPlaySharedPrefUtils;

/**
 * Created by Jordan on 1/10/2015.
 */
public class InformationDialogFragment extends DialogFragment {

    private static class BundleArgs {
        static final String SHOW_LOGIN = "show_login";
    }

    public static InformationDialogFragment newInstance(boolean showLogin) {
        InformationDialogFragment fragment = new InformationDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(BundleArgs.SHOW_LOGIN, showLogin);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        boolean isShowLogin = getArguments().getBoolean(BundleArgs.SHOW_LOGIN);
        View view = inflater.inflate(R.layout.activity_information, container);

        if (!isShowLogin) {
            View infoContainer = view.findViewById(R.id.information_login_content);
            infoContainer.setVisibility(View.GONE);
        } else {
            Button loginButton = (Button) view.findViewById(R.id.login_button);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    DailyPlaySharedPrefUtils.setIsFirstOpen();
                    LoginManager.getManager(getActivity()).promptForUserInformationIfNoneExists();
                }
            });
            getDialog().setCancelable(false);
        }
        getDialog().setTitle(R.string.info_title);

        return view;
    }
}
