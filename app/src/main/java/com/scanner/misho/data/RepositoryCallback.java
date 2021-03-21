package com.scanner.misho.data;

import com.scanner.misho.data.model.LoggedInUser;

public interface RepositoryCallback {
    void onComplete(Result<LoggedInUser> result);
}
