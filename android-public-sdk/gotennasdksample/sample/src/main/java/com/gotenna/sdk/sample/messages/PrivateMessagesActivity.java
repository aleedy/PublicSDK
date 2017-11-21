package com.gotenna.sdk.sample.messages;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gotenna.sdk.sample.models.Contact;
import com.gotenna.sdk.sample.ContactViewModel;
import com.gotenna.sdk.sample.R;
import com.gotenna.sdk.sample.Utils;
import com.gotenna.sdk.sample.gid.SetGidActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A screen that allows a user to send and receive private messages.
 * <p>
 * Created on: 7/14/17.
 *
 * @author Thomas Colligan
 */

public class PrivateMessagesActivity extends AppCompatActivity implements PrivateMessagesPresenter.PrivateMessagesView
{
    //==============================================================================================
    // Class Properties
    //==============================================================================================

    /**
     * Byte limit on text to make sure we don't go over goTenna's payload limit.
     * Made the limit here lower than the actual limit since text messages get inflated
     * by a few bytes when serialized later.
     */
    private static final int MESSAGE_BYTE_LIMIT = 227;

    private TextView receiverTextView;
    private ListView messagesListView;
    private EditText sendMessageEditText;
    private MessagesArrayAdapter messagesArrayAdapter;
    private List<MessageViewModel> messages;
    private PrivateMessagesPresenter presenter;

    //==============================================================================================
    // Static Class Methods
    //==============================================================================================

    public static void start(Context context)
    {
        Intent starter = new Intent(context, PrivateMessagesActivity.class);
        context.startActivity(starter);
    }

    //==============================================================================================
    // Life-Cycle Methods
    //==============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_messages);

        receiverTextView = (TextView) findViewById(R.id.receiverTextView);
        messagesListView = (ListView) findViewById(R.id.messagesListView);
        sendMessageEditText = (EditText) findViewById(R.id.sendMessageEditText);

        presenter = new PrivateMessagesPresenter();
        presenter.attachView(this);
        presenter.onCreate();

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sendMessageEditText.addTextChangedListener(messageLengthLimitWatcher);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy()
    {
        sendMessageEditText.removeTextChangedListener(messageLengthLimitWatcher);
        presenter.detachView();
        presenter = null;
        super.onDestroy();
    }

    //==============================================================================================
    // Button Click Methods
    //==============================================================================================

    public void onChooseReceiverButtonClicked(View v)
    {
        presenter.onChooseReceiver();
    }

    public void onSendMessageButtonClicked(View v)
    {
        String messageText = sendMessageEditText.getText().toString();
        presenter.onSendMessage(messageText);
    }

    //==============================================================================================
    // TextWatcher
    //==============================================================================================

    private final TextWatcher messageLengthLimitWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
        }

        @Override
        public void afterTextChanged(Editable editable)
        {
            // We can only send so much data at once per message using the goTenna SDK,
            // so limit the amount of text the user can input.
            String message = sendMessageEditText.getText().toString();
            int byteLength = message.getBytes().length;

            if (byteLength > MESSAGE_BYTE_LIMIT)
            {
                sendMessageEditText.removeTextChangedListener(this);
                editable.clear();
                editable.append(Utils.truncateUtf8String(message, MESSAGE_BYTE_LIMIT));
                sendMessageEditText.addTextChangedListener(this);
            }
        }
    };

    //==============================================================================================
    // PrivateMessagesView Implementation
    //==============================================================================================

    @Override
    public void showMustSelectUserFirstErrorMessage()
    {
        Toast toast = Toast.makeText(getApplicationContext(), R.string.must_choose_user_toast_text,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void showReceiverUnselected()
    {
        receiverTextView.setText(getString(R.string.receiver_text, getString(R.string.none)));
    }

    @Override
    public void showReceiverSelected(Contact receiverContact)
    {
        receiverTextView.setText(getString(R.string.receiver_text, receiverContact.getName()));
    }

    @Override
    public void showSelectReceiverDialog(final List<ContactViewModel> viewModels)
    {
        CharSequence[] choicesArray = new CharSequence[viewModels.size()];

        for (int i = 0; i < viewModels.size(); i++)
        {
            ContactViewModel viewModel = viewModels.get(i);
            choicesArray[i] = viewModel.getContactInfoText();
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_user_dialog_title)
                .setSingleChoiceItems(choicesArray, -1, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ContactViewModel selectedContactViewModel = viewModels.get(which);
                        presenter.onReceiverSelected(selectedContactViewModel);
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void showReceiverNotSelectedErrorMessage()
    {
        Toast toast = Toast.makeText(getApplicationContext(), R.string.invalid_receiver_toast_text,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void showSelectGidScreen()
    {
        SetGidActivity.start(this);
    }

    @Override
    public void showEmptyMessageError()
    {
        Toast toast = Toast.makeText(getApplicationContext(), R.string.empty_message,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void clearSendMessageText()
    {
        sendMessageEditText.setText("");
    }

    @Override
    public void showGotennaDisconnectedErrorMessage()
    {
        Toast toast = Toast.makeText(getApplicationContext(), R.string.gotenna_disconnected,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void showMessages(List<MessageViewModel> messages)
    {
        if (messagesArrayAdapter == null)
        {
            this.messages = new ArrayList<>();
            this.messages.addAll(messages);

            messagesArrayAdapter = new MessagesArrayAdapter(this, this.messages);
            messagesArrayAdapter.setWillDisplayAckConfirmation(true);
            messagesListView.setAdapter(messagesArrayAdapter);
        }
        else
        {
            this.messages.clear();
            this.messages.addAll(messages);
            messagesArrayAdapter.notifyDataSetChanged();
        }
    }
}
