package com.rael.daniel.drc.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.rael.daniel.drc.R;
import com.rael.daniel.drc.reddit_api.RedditAPICommon;

/**
 * Simple dialog for replying to comments
 * TODO: add error checking
 */
public class ReplyDialog extends Dialog{
    String parentId;

    public ReplyDialog(Activity act, Bundle params) {
        super(act);
        parentId = params.getString("parent_id");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reply_dialog_layout);
        findViewById(R.id.reply_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText replyText = (EditText) findViewById(R.id.reply_text);
                new RedditAPICommon(getContext()).reply(parentId,
                        replyText.getText().toString());
                dismiss();
            }
        });
        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
