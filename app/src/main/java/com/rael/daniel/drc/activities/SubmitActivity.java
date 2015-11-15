package com.rael.daniel.drc.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.reddit_api.RedditAPICommon;

/**
 * Form for submitting new posts
 * TODO: this is pretty bare-bones, make it prettier
 */
public class SubmitActivity extends AppCompatActivity {
    private String subreddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_layout);
        subreddit = getIntent().getExtras().getString("subreddit");
        findViewById(R.id.submit_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = ((EditText) findViewById(R.id.submit_title))
                                .getText().toString();
                        String text = ((EditText) findViewById(R.id.submit_text))
                                .getText().toString();
                        String url = ((EditText) findViewById(R.id.submit_url))
                                .getText().toString();
                        if (title.length() == 0) {
                            Toast.makeText(getApplicationContext(), "Error, posts need to have a title",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (text.length() > 0 && url.length() > 0) {
                            Toast.makeText(getApplicationContext(), "Error, posts can only contain a" +
                                    "link or self text, but not both", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String kind = null;
                        if (text.length() > 0) kind = "self";
                        if (url.length() > 0) kind = "link";
                        new RedditAPICommon(getApplicationContext()).submit("test", kind,
                                SubmitActivity.this);
                    }
                }
        );
    }
}
