package com.gotenna.sdk.sample.groups;

import com.gotenna.sdk.bluetooth.GTConnectionManager;
import com.gotenna.sdk.sample.models.Message;
import com.gotenna.sdk.sample.managers.IncomingMessagesManager;
import com.gotenna.sdk.sample.messages.MessageViewModel;
import com.gotenna.sdk.sample.messages.SendMessageInteractor;
import com.gotenna.sdk.user.User;
import com.gotenna.sdk.user.UserDataStore;

import java.util.ArrayList;
import java.util.List;

/**
 * A presenter for {@link GroupMessagesActivity}.
 * <p>
 * Created on: 7/13/17.
 *
 * @author Thomas Colligan
 */

class GroupMessagesPresenter implements IncomingMessagesManager.IncomingMessageListener
{
    //==============================================================================================
    // Class Properties
    //==============================================================================================

    private static final boolean WILL_ENCRYPT_MESSAGES = true; // Can optionally encrypt messages using SDK
    private GroupMessagesView view;
    private long selectedGroupGID;
    private final List<Message> messages;
    private final SendMessageInteractor sendMessageInteractor;
    private final UserDataStore userDataStore;
    private final GTConnectionManager gtConnectionManager;
    private final IncomingMessagesManager incomingMessagesManager;

    //==============================================================================================
    // Constructor
    //==============================================================================================

    GroupMessagesPresenter()
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

    void attachView(GroupMessagesView view)
    {
        this.view = view;
        incomingMessagesManager.addIncomingMessageListener(this);
    }

    void detachView()
    {
        this.view = null;
        incomingMessagesManager.removeIncomingMessageListener(this);
    }

    //==============================================================================================
    // Presenter Action Methods
    //==============================================================================================

    void onCreate()
    {
        view.showSelectGroupUi();
    }

    void onSelectGroup()
    {
        User currentUser = userDataStore.getCurrentUser();

        if (currentUser == null)
        {
            view.showMustSelectUserFirstErrorMessage();
            view.showSelectGidScreen();
            return;
        }

        final List<Long> groupGIDs = currentUser.getGroupGIDs();

        if (groupGIDs.size() == 0)
        {
            view.showNoGroupsErrorMessage();
            view.showCreateGroupScreen();
            return;
        }

        view.showSelectGroupDialog(groupGIDs);
    }

    void onGroupSelected(long selectedGroupGID)
    {
        this.selectedGroupGID = selectedGroupGID;
        view.showSelectedGroupUi(selectedGroupGID);
    }

    void onSendMessage(String messageText)
    {
        User currentUser = userDataStore.getCurrentUser();

        if (currentUser == null)
        {
            view.showMustSelectUserFirstErrorMessage();
            view.showSelectGidScreen();
            return;
        }

        if (selectedGroupGID == 0)
        {
            view.showGroupNotSelectedErrorMessage();
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
                selectedGroupGID,
                messageText);

        messages.add(messageToSend);
        view.showMessages(createMessageViewModels());

        sendMessageInteractor.sendMessage(messageToSend, WILL_ENCRYPT_MESSAGES,
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
    // IncomingMessageListener Implementation
    //==============================================================================================

    @Override
    public void onIncomingMessage(Message incomingMessage)
    {
        if (incomingMessage.getReceiverGID() == selectedGroupGID)
        {
            messages.add(incomingMessage);

            if (view != null)
            {
                view.showMessages(createMessageViewModels());
            }
        }
    }

    //==============================================================================================
    // GroupMessagesView
    //==============================================================================================

    interface GroupMessagesView
    {
        void showSelectGroupUi();

        void showSelectedGroupUi(long selectedGroupGID);

        void showMustSelectUserFirstErrorMessage();

        void showSelectGidScreen();

        void showNoGroupsErrorMessage();

        void showCreateGroupScreen();

        void showSelectGroupDialog(final List<Long> groupGIDs);

        void showGroupNotSelectedErrorMessage();

        void showEmptyMessageError();

        void clearSendMessageText();

        void showGotennaDisconnectedErrorMessage();

        void showMessages(List<MessageViewModel> messages);
    }
}
