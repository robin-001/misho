package com.scanner.misho.ui.login;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.scanner.misho.data.LoginDataSource;
import com.scanner.misho.data.LoginRepository;
import com.scanner.misho.data.UserDataSource;

import java.util.concurrent.Executor;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(LoginRepository.getInstance(new UserDataSource(), new Executor() {
                @Override
                public void execute(Runnable command) {

                }
            }));
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}