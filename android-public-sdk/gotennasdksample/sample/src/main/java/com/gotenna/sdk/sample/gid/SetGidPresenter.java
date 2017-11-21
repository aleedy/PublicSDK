package com.gotenna.sdk.sample.gid;

import com.gotenna.sdk.commands.GTCommand.GTCommandResponseListener;
import com.gotenna.sdk.commands.GTCommandCenter;
import com.gotenna.sdk.commands.GTError;
import com.gotenna.sdk.interfaces.GTErrorListener;
import com.gotenna.sdk.responses.GTResponse;
import com.gotenna.sdk.sample.managers.ContactsManager;
import com.gotenna.sdk.sample.models.Contact;
import com.gotenna.sdk.sample.ContactViewModel;

import com.gotenna.sdk.types.GTDataTypes;
import com.gotenna.sdk.user.User;
import com.gotenna.sdk.user.UserDataStore;


import java.util.ArrayList;
import java.util.List;

/**
 * A presenter for {@link SetGidActivity}.
 * <p>
 * Created on: 7/11/17.
 *
 * @author Thomas Colligan
 */

class SetGidPresenter
{
    //==============================================================================================
    // Class Properties
    //==============================================================================================

    private SetGidView view;
    private final UserDataStore userDataStore;
    private final ContactsManager contactsManager;
    private final GTCommandCenter gtCommandCenter;

    //==============================================================================================
    // Constructor
    //==============================================================================================

    SetGidPresenter()
    {
        userDataStore = UserDataStore.getInstance();
        contactsManager = ContactsManager.getInstance();
        gtCommandCenter = GTCommandCenter.getInstance();
    }

    //==============================================================================================
    // Class Instance Methods
    //==============================================================================================

    void attachView(SetGidView view)
    {
        this.view = view;
    }

    void detachView()
    {
        this.view = null;
    }

    private void refreshUserInfo()
    {
        User currentUser = userDataStore.getCurrentUser();

        if (currentUser == null)
        {
            view.showEmptyUserInfo();
        }
        else
        {
            view.showUserInfo(currentUser.getName(), currentUser.getGID());
        }
    }

    private void sendSetGidCommand(String username, long gid)
    {
        // The UserDataStore automatically saves the user's basic info after setGoTennaGID is called
        gtCommandCenter.setGoTennaGID(gid, username, new GTCommandResponseListener()
        {
            @Override
            public void onResponse(GTResponse response)
            {
                if (response.getResponseCode() == GTDataTypes.GTCommandResponseCode.POSITIVE)
                {
                    view.showSetGidSuccessMessage();
                }
                else
                {
                    view.showSetGidFailureMessage();
                }
            }
        }, new GTErrorListener()
        {
            @Override
            public void onError(GTError error)
            {
                view.showSetGidFailureMessage();
            }
        });
    }

    //==============================================================================================
    // Presenter Action Methods
    //==============================================================================================

    void onCreate()
    {
        refreshUserInfo();
    }

    void onChangeUser()
    {
        final List<Contact> contacts = contactsManager.getDemoContactsExcludingSelf();
        final List<ContactViewModel> viewModels = new ArrayList<>(contacts.size());

        for (Contact contact : contacts)
        {
            viewModels.add(new ContactViewModel(contact));
        }

        view.showUserSelectionDialog(viewModels);
    }

    void onUserSelected(ContactViewModel selectedContactViewModel)
    {
        Contact selectedContact = selectedContactViewModel.getContact();
        sendSetGidCommand(selectedContact.getName(), selectedContact.getGid());
        refreshUserInfo();
    }

    void onSetGid()
    {
        User currentUser = userDataStore.getCurrentUser();

        if (currentUser == null)
        {
            view.showMustSelectUserMessage();
        }
        else
        {
            sendSetGidCommand(currentUser.getName(), currentUser.getGID());
        }
    }

    void onDoneClicked()
    {
        User currentUser = userDataStore.getCurrentUser();

        if (currentUser == null)
        {
            view.showMustSelectUserMessage();
        }
        else
        {
            view.finish();
        }
    }

    //==============================================================================================
    // SetGidView Interface
    //==============================================================================================

    interface SetGidView
    {
        void showEmptyUserInfo();

        void showUserInfo(String username, long gid);

        void showUserSelectionDialog(final List<ContactViewModel> contactViewModels);

        void showSetGidSuccessMessage();

        void showSetGidFailureMessage();

        void showMustSelectUserMessage();

        void finish();
    }
}
