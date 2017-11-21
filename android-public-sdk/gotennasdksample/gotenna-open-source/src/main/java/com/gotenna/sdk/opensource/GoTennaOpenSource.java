package com.gotenna.sdk.opensource;

import android.content.Context;

import com.gotenna.sdk.GoTenna;
import com.gotenna.sdk.exceptions.GTInvalidAppTokenException;

/**
 * Created by adamleedy on 11/20/17.
 */

public class GoTennaOpenSource {
    public static void setApplicationToken(Context context, String applicationToken) throws GTInvalidAppTokenException {
        GoTenna.setApplicationToken(context, applicationToken);
    }
}
