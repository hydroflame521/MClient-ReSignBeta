package com.apk.editor.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.RecycleViewAppsAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppData;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ApplicationsFragment extends Fragment {

    private AppCompatEditText mSearchWord;
    private AsyncTask<Void, Void, List<String>> mLoader;
    private boolean mExit;
    private Handler mHandler = new Handler();
    private LinearLayout mProgress;
    private MaterialTextView mAppTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewAppsAdapter mRecycleViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_main, container, false);

        mAppTitle = mRootView.findViewById(R.id.app_title);
        mSearchWord = mRootView.findViewById(R.id.search_word);
        mProgress = mRootView.findViewById(R.id.progress_layout);
        AppCompatImageButton mSearchButton = mRootView.findViewById(R.id.search_button);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mAppTitle.setText(getString(R.string.apps_installed));
        mTabLayout.setVisibility(View.VISIBLE);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.all)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.system)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.user)));

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(requireActivity()))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = APKEditorUtils.getString("appTypes", "all", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("all")) {
                            APKEditorUtils.saveString("appTypes", "all", requireActivity());
                            loadApps(requireActivity());
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("system")) {
                            APKEditorUtils.saveString("appTypes", "system", requireActivity());
                            loadApps(requireActivity());
                        }
                        break;
                    case 2:
                        if (!mStatus.equals("user")) {
                            APKEditorUtils.saveString("appTypes", "user", requireActivity());
                            loadApps(requireActivity());
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mSearchButton.setOnClickListener(v -> {
            if (mSearchWord.getVisibility() == View.VISIBLE) {
                mSearchWord.setVisibility(View.GONE);
                mAppTitle.setVisibility(View.VISIBLE);
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                mSearchWord.requestFocus();
                mAppTitle.setVisibility(View.GONE);
            }
        });

        mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                AppData.mSearchText = s.toString().toLowerCase();
                loadApps(requireActivity());
            }
        });

        loadApps(requireActivity());

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (AppData.mSearchText != null) {
                    mSearchWord.setText(null);
                    AppData.mSearchText = null;
                    return;
                }
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    mSearchWord.setVisibility(View.GONE);
                    mAppTitle.setVisibility(View.VISIBLE);
                    return;
                }
                if (mExit) {
                    mExit = false;
                    requireActivity().finish();
                } else {
                    APKEditorUtils.snackbar(requireActivity().findViewById(android.R.id.content), getString(R.string.press_back));
                    mExit = true;
                    mHandler.postDelayed(() -> mExit = false, 2000);
                }
            }
        });

        return mRootView;
    }

    private int getTabPosition(Activity activity) {
        String mStatus = APKEditorUtils.getString("appTypes", "all", activity);
        if (mStatus.equals("user")) {
            return 2;
        } else if (mStatus.equals("system")) {
            return 1;
        } else {
            return 0;
        }
    }

    private void loadApps(Activity activity) {
        if (mLoader == null) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, List<String>>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            mRecyclerView.setVisibility(View.GONE);
                            mProgress.setVisibility(View.VISIBLE);
                        }

                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewAppsAdapter(AppData.getData(activity));
                            return null;
                        }

                        @Override
                        protected void onPostExecute(List<String> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            mRecyclerView.setAdapter(mRecycleViewAdapter);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mProgress.setVisibility(View.GONE);
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (AppData.mSearchText != null) {
            mSearchWord.setText(null);
            AppData.mSearchText = null;
        }
    }
    
}