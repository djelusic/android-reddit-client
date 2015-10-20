package com.rael.daniel.drc.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class ImprovedWebViewFragment extends Fragment {

    private WebView mWebView;
    private boolean mIsWebViewAvailable;
    private String mUrl = null;

    /**
     * Creates a new fragment which loads the supplied url as soon as it can
     * @param url the url to load once initialised
     */
    public ImprovedWebViewFragment() {
        super();
    }

    public static Fragment newInstance(String url) {
        ImprovedWebViewFragment iwvf = new ImprovedWebViewFragment();
        iwvf.mUrl = url;
        return iwvf;
    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mWebView != null) {
            mWebView.destroy();
        }
        mWebView = new WebView(getActivity());
        mWebView.loadUrl(mUrl);
        mIsWebViewAvailable = true;
        return mWebView;
    }

    /**
     * Convenience method for loading a url. Will fail if {@link View} is not initialised (but won't throw an {@link Exception})
     * @param url
     */
    public void loadUrl(String url) {
        if (mIsWebViewAvailable) getWebView().loadUrl(mUrl = url);
        else Log.w("ImprovedWebViewFragment", "WebView cannot be found. Check the view and fragment have been loaded.");
    }

    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    @Override
    public void onResume() {
        mWebView.onResume();
        super.onResume();
    }

    /**
     * Called when the WebView has been detached from the fragment.
     * The WebView is no longer available after this time.
     */
    @Override
    public void onDestroyView() {
        mIsWebViewAvailable = false;
        super.onDestroyView();
    }

    /**
     * Called when the fragment is no longer in use. Destroys the internal state of the WebView.
     */
    @Override
    public void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    /**
     * Gets the WebView.
     */
    public WebView getWebView() {
        return mIsWebViewAvailable ? mWebView : null;
    }

}