package com.gotenna.sdk.sample.messages;

import com.gotenna.sdk.bluetooth.GTConnectionManager;
import com.gotenna.sdk.sample.managers.ContactsManager;
import com.gotenna.sdk.sample.models.Contact;
import com.gotenna.sdk.sample.models.Message;
import com.gotenna.sdk.sample.ContactViewModel;
import com.gotenna.sdk.sample.managers.IncomingMessagesManager;
import com.gotenna.sdk.user.User;
import com.gotenna.sdk.user.UserDataStore;

import java.util.ArrayList;
import java.util.List;

/**
 * A presenter for {@link PrivateMessagesActivity}.
 * <p>
 * Created on: 7/18/17.
 *
 * @author Thomas Colligan
 */

class PrivateMessagesPresenter implements IncomingMessagesManager.IncomingMessageListener
{
    //==============================================================================================
    // Class Properties
    //==============================================================================================

    private static final boolean WILL_ENCRYPT_MESSAGES = true; // Can optionally encrypt messages using SDK
    private PrivateMessagesView view;
    private final List<Message> messages;
    private Contact receiverContact;
    private final SendMessageInteractor sendMessageInteractor;
    private final ContactsManager contactsManager;
    private final UserDataStore userDataStore;
    private final GTConnectionManager gtConnectionManager;
    private final IncomingMessagesManager incomingMessagesManager;

    //==============================================================================================
    // Constructor
    //==============================================================================================

    PrivateMessagesPresenter()
    {
        messages = new ArrayList<>();
        sendMessageInteractor = new SendMessageInteractor();
        contactsManager = ContactsManager.getInstance();
        userDataStore = UserDataStore.getInstance();
        gtConnectionManager = GTConnectionManager.getInstance();
        incomingMessagesManager = IncomingMessagesManager.getInstance();
    }

    //==============================================================================================
    // Class Instance Methods
    //==============================================================================================

    public void attachView(PrivateMessagesView view)
    {
        this.view = view;
        incomingMessagesManager.addIncomingMessageListener(this);
    }

    public void detachView()
    {
        view = null;
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

    void onCreate()
    {
        view.showReceiverUnselected();
    }

    void onChooseReceiver()
    {
        final List<Contact> contacts = contactsManager.getDemoContactsExcludingSelf();
        final List<ContactViewModel> viewModels = new ArrayList<>(contacts.size());

        for (Contact contact : contacts)
        {
            viewModels.add(new ContactViewModel(contact));
        }

        view.showSelectReceiverDialog(viewModels);
    }

    void onReceiverSelected(ContactViewModel selectedContactViewModel)
    {
        this.receiverContact = selectedContactViewModel.getContact();
        view.showReceiverSelected(receiverContact);
    }

    void onSendMessage(String messageText)
    {
        if (receiverContact == null)
        {
            view.showReceiverNotSelectedErrorMessage();
            return;
        }

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
                receiverContact.getGid(),
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

    //==============================================================================================
    // IncomingMessageListener Implementation
    //==============================================================================================

    @Override
    public void onIncomingMessage(Message incomingMessage)
    {
        User currentUser = userDataStore.getCurrentUser();

        if (receiverContact != null &&
                currentUser != null &&
                incomingMessage.getSenderGID() == receiverContact.getGid() &&
                incomingMessage.getReceiverGID() == currentUser.getGID())
        {
            messages.add(incomingMessage);

            if (view != null)
            {
                view.showMessages(createMessageViewModels());
            }
        }
    }

    //==============================================================================================
    // PrivateMessagesView
    //==============================================================================================

    interface PrivateMessagesView
    {
        void showReceiverUnselected();

        void showReceiverSelected(Contact receiverContact);

        void showSelectReceiverDialog(final List<ContactViewModel> contactViewModels);

        void showReceiverNotSelectedErrorMessage();

        void showMustSelectUserFirstErrorMessage();

        void showSelectGidScreen();

        void showEmptyMessageError();

        void clearSendMessageText();

        void showGotennaDisconnectedErrorMessage();

        void showMessages(List<MessageViewModel> messages);
    }
}
