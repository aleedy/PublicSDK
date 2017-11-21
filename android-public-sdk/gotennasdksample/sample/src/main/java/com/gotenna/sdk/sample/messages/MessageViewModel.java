package com.gotenna.sdk.sample.messages;

import com.gotenna.sdk.sample.models.Message;
import com.gotenna.sdk.sample.MyApplication;
import com.gotenna.sdk.sample.R;

import com.gotenna.sdk.user.User;
import com.gotenna.sdk.user.UserDataStore;

/**
 * A view model for displaying messages.
 * <p>
 * Created on: 7/18/17.
 *
 * @author Thomas Colligan
 */

public class MessageViewModel
{
    //==============================================================================================
    // Class Property
    //==============================================================================================

    private Message message;
    private final UserDataStore userDataStore;

    //==============================================================================================
    // Constructor
    //==============================================================================================

    public MessageViewModel(Message message)
    {
        this.message = message;
        this.userDataStore = UserDataStore.getInstance();
    }

    //==============================================================================================
    // Class Instance Methods
    //==============================================================================================

    String getMessageText()
    {
        return message.getText();
    }

    String getCellInfoText()
    {
        int hopCount = message.getHopCount();

        if (message.getDetailInfo() == null)
        {
            if (hopCount == 0)
            {
                return message.getSentDate().toString();
            }
            else
            {
                return MyApplication.getAppContext().getString(R.string.chat_cell_with_hops,
                        message.getSentDate().toString(),
                        hopCount);
            }
        }
        else
        {
            if (hopCount == 0)
            {
                return MyApplication.getAppContext().getString(R.string.chat_cell_info,
                        message.getDetailInfo(),
                        message.getSentDate().toString());
            }
            else
            {
                return MyApplication.getAppContext().getString(R.string.chat_cell_info_hop,
                        message.getDetailInfo(),
                        message.getSentDate().toString(),
                        hopCount);
            }
        }
    }

    Message.MessageStatus getMessageStatus()
    {
        return message.getMessageStatus();
    }

    boolean currentUserIsSender()
    {
        User currentUser = userDataStore.getCurrentUser();
        return currentUser != null && message.getSenderGID() == currentUser.getGID();
    }

    boolean messageIsMine()
    {
        User currentUser = userDataStore.getCurrentUser();

        if (currentUser != null)
        {
            if (message.getSenderGID() == currentUser.getGID())
            {
                return true;
            }
        }

        return false;
    }
}
