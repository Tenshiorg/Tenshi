package io.github.shadow578.tenshi.ui.oobe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.shadow578.tenshi.databinding.FragmentInitialConfigurationBinding;
import io.github.shadow578.tenshi.mal.model.User;
import io.github.shadow578.tenshi.util.DateHelper;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;

/**
 * fragment that handles initial app configuration.
 * after the user presses "Done", OnSuccess is invoked
 */
public class InitialConfigurationFragment extends OnboardingFragment {

    private FragmentInitialConfigurationBinding b;
    private boolean userIsLegalAge = false;

    @Nullable
    private User user = null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentInitialConfigurationBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // update ui
        updateViews();
    }

    /**
     * set the user details
     *
     * @param usr the user details
     */
    public void setUserDetails(@NonNull User usr) {
        // check if user is legal age, by default assume not
        userIsLegalAge = false;
        with(usr.birthday,
                birthday -> userIsLegalAge = DateHelper.getYearsToNow(birthday.toLocalDate()) >= 18);

        // set user ref
        user = usr;

        // update views
        if(isAdded())
            updateViews();
    }

    /**
     * update views related to user data
     */
    private void updateViews(){
        // user details
        with(user, usr -> {
            // TODO: debugging stuff
            b.dbg.setText(fmt("NAME: %s%nlegal_age: %s%n BDAY: %s",
                    usr.name,
                    userIsLegalAge ? "Y" : "N",
                    usr.birthday == null ? "NULL": usr.birthday.toString()));
        });
    }
}
