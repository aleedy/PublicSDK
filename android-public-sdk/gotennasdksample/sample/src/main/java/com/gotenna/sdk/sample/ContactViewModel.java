package com.gotenna.sdk.sample;


import com.gotenna.sdk.sample.models.Contact;

import java.util.Locale;

/**
 * A view model for displaying contact info.
 * <p>
 * Created on: 7/18/17.
 *
 * @author Thomas Colligan
 */

public class ContactViewModel
{
    //==============================================================================================
    // Class Properties
    //==============================================================================================

    private Contact contact;

    //==============================================================================================
    // Constructor
    //==============================================================================================

    public ContactViewModel(Contact contact)
    {
        this.contact = contact;
    }

    //==============================================================================================
    // Class Instance Methods
    //==============================================================================================

    public Contact getContact()
    {
        return contact;
    }

    public String getContactInfoText()
    {
        return String.format(Locale.US, "%s - %d", contact.getName(), contact.getGid());
    }
}
