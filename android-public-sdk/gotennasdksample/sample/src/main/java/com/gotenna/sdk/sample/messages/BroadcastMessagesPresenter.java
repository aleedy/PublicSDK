package com.gotenna.sdk.sample.messages;

import com.gotenna.sdk.bluetooth.GTConnectionManager;
import com.gotenna.sdk.gids.GIDManager;
import com.gotenna.sdk.sample.models.Message;
import com.gotenna.sdk.sample.managers.IncomingMessagesManager;
import com.gotenna.sdk.user.User;
import com.gotenna.sdk.user.UserDataStore;

import java.util.ArrayList;
import java.util.List;

/**
 * A presenter for {@link BroadcastMessagesActivity}.
 * <p>
 * Created on: 7/14/17.
 *
 * @author Thomas Colligan
 */

class BroadcastMessagesPresenter implements IncomingMessagesManager.IncomingMessageListener
{
    //==============================================================================================
    // Class Properties
    //==============================================================================================

    private BroadcastMessagesView view;
    private final List<Message> messages;
    private final SendMessageInteractor sendMessageInteractor;
    private final UserDataStore userDataStore;
    private final GTConnectionManager gtConnectionManager;
    private final IncomingMessagesManager incomingMessagesManager;

    //==============================================================================================
    // Constructor
    //==============================================================================================

    BroadcastMessagesPresenter()
    {
        messages = new ArrayList<>();
        sendMessageInteractor = new SendMessageInteractor();
        userDataStore = UserDataStore.getInstance();
        gtConnectionManager = GTConnectionManager.getInstance();
        incomingMessagesManager = IncomingMessagesManager.getInstance();
    }

    //==============================================================================================
    // Class Instance Methods
    //==============================================================================================

    void attachView(BroadcastMessagesView view)
    {
        this.view = view;
        incomingMessagesManager.addIncomingMessageListener(this);
    }

    void detachView()
    {
        this.view = null;
        incomingMessagesManager.removeIncomingMessageListener(this);
    }

    private List<MessageViewModel> createMessageViewModels()
    {
        List<MessageViewModel> viewModelList = new ArrayList<>();

        for (Message message : messages)
        {
            viewModelList.add(new MessageViewModel(message));
        }

        return viewModelList;
    }

    //==============================================================================================
    // Presenter Action Methods
    //==============================================================================================

    void onSendMessage(String messageText)
    {
        User currentUser = userDataStore.getCurrentUser();

        if (currentUser == null)
        {
            view.showMustSelectUserFirstErrorMessage();
            view.showSelectGidScreen();
            return;
        }

        if (messageText.isEmpty())
        {
            view.showEmptyMessageError();
            return;
        }

        if (!gtConnectionManager.isConnected())
        {
            view.showGotennaDisconnectedErrorMessage();
            return;
        }

        view.clearSendMessageText();

        Message messageToSend = Message.createReadyToSendMessage(currentUser.getGID(),
                GIDManager.SHOUT_GID,
                messageText);

        messages.add(messageToSend);
        view.showMessages(createMessageViewModels());

        sendMessageInteractor.sendBroadcastMessage(messageToSend,
                new SendMessageInteractor.SendMessageListener()
                {
                    @Override
                    public void onMessageResponseReceived()
                    {
                        if (view != null)
                        {
                            view.showMessages(createMessageViewModels());
                        }
                    }
                });
    }

    //==============================================================================================
    // IncomingMessageListener Implementation
    //==============================================================================================

    @Override
    public void onIncomingMessage(Message incomingMessage)
    {
        if (incomingMessage.getReceiverGID() == GIDManager.SHOUT_GID)
        {
            messages.add(incomingMessage);

            if (view != null)
            {
                view.showMessages(createMessageViewModels());
            }
        }
    }

    //==============================================================================================
    // BroadcastMessagesView
    //==============================================================================================

    interface BroadcastMessagesView
    {
        void showMustSelectUserFirstErrorMessage();

        void showSelectGidScreen();

        void showEmptyMessageError();

        void clearSendMessageText();

        void showGotennaDisconnectedErrorMessage();

        void showMessages(List<MessageViewModel> messages);
    }
}
