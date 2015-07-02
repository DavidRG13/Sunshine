package com.android.sunshine.app.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

public class Authenticator extends AbstractAccountAuthenticator {

    public Authenticator(final Context context) {
        super(context);
    }

    @Override
    public Bundle editProperties(final AccountAuthenticatorResponse r, final String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(final AccountAuthenticatorResponse r, final String s, final String s2, final String[] strings, final Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle confirmCredentials(final AccountAuthenticatorResponse r, final Account account, final Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(final AccountAuthenticatorResponse r, final Account account, final String s, final Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthTokenLabel(final String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(final AccountAuthenticatorResponse r, final Account account, final String s, final Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(final AccountAuthenticatorResponse r, final Account account, final String[] strings) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}
