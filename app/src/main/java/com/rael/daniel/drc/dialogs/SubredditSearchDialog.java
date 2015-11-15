package com.rael.daniel.drc.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.fragments.PostsRecyclerFragment;

/**
 * Dialog for subreddit access from navigation drawer
 */
public class SubredditSearchDialog extends Dialog {
    Context applicationContext;

    public SubredditSearchDialog(Activity act) {
        super(act);
        this.applicationContext = act;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subreddit_dialog_layout);
        findViewById(R.id.subreddit_view_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subredditQuery = ((EditText) findViewById(R.id.subreddit_query)).getText().toString();
                Fragment pf = PostsRecyclerFragment.newInstance(applicationContext, subredditQuery, null, "", false);

                ((AppCompatActivity)applicationContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragments_container, pf, subredditQuery)
                        .addToBackStack(subredditQuery)
                        .commit();
                dismiss();
            }
        });
        findViewById(R.id.subreddit_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
