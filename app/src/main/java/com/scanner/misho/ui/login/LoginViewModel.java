package com.scanner.misho.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.os.Handler;
import android.util.Log;
import android.util.Patterns;

import com.scanner.misho.data.LoginRepository;
import com.scanner.misho.data.RepositoryCallback;
import com.scanner.misho.data.Result;
import com.scanner.misho.data.model.LoggedInUser;
import com.scanner.misho.R;

public class LoginViewModel extends ViewModel {

    private static final String TAG = "LoginViewModel";
    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;
    private Handler resultHandler  = new Handler();

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void makeLoginRequest(String username, String password) {
        // can be launched in a separate asynchronous job
        Log.d(TAG,"makeLoginRequest");
        loginRepository.makeLoginRequest(username, password, new RepositoryCallback() {
            @Override
            public void onComplete(Result<LoggedInUser> result) {
                Log.d(TAG,"makeLoginRequest.oncomplete");
                Log.d(TAG,result.toString());
                if (result instanceof Result.Success) {
                    LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                    loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
                } else {
                    loginResult.setValue(new LoginResult(R.string.login_failed));
                }
            }
        },resultHandler);

    }


    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}