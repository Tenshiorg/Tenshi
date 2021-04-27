package io.github.shadow578.tenshi.ui.oobe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.shadow578.tenshi.databinding.FragmentFetchFinishBinding;

/**
 * passive fragment that just shows a loading message while user data is fetched.
 * <p>
 * does not call events
 */
public class FetchFinishFragment extends OnboardingFragment {

    private FragmentFetchFinishBinding b;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentFetchFinishBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
    }
}
