package com.scanner.misho.data;

import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.scanner.misho.App;
import com.scanner.misho.R;
import com.scanner.misho.data.model.LoggedInUser;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.Executor;

import static android.content.Context.MODE_PRIVATE;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static final String TAG = "LoginRepository";
    private static volatile LoginRepository instance;
    private Executor executor;

    private UserDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(UserDataSource dataSource) {
        this.dataSource = dataSource;
        this.executor = new Executor() {
            @Override
            public void execute(Runnable command) {

            }
        };
    }

    public static LoginRepository getInstance(UserDataSource dataSource,Executor executor) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }


    public void makeLoginRequest(final String username, final String password, final RepositoryCallback callback, final Handler resultHandler) {
        Log.d(TAG,"makeLoginRequest");

        App.executorService.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"executor.execute.run");
                try {
                    Result<LoggedInUser> result = makeSynchronousLoginRequest(username,password);
                    notifyResult(result, callback, resultHandler);
                } catch (IOException e) {
                    Result<LoggedInUser> errorResult = new Result.Error(e);
                    notifyResult(errorResult, callback, resultHandler);
                    e.printStackTrace();
                } catch (JSONException e) {
                    Result<LoggedInUser> errorResult = new Result.Error(e);
                    notifyResult(errorResult, callback, resultHandler);
                    e.printStackTrace();
                }
            }
        });
    }

    public Result<LoggedInUser> makeSynchronousLoginRequest(String username,String password) throws IOException, JSONException {
        Log.d(TAG,"makeSynchronousLoginRequest");
        UserDataSource conn = new UserDataSource();
        conn.login(username,password);

        SharedPreferences sharedPreferences = App.getContext().getSharedPreferences(App.getContext().getString(R.string.app_name), MODE_PRIVATE);
        final String name = sharedPreferences.getString("name",null);
        final String userid = sharedPreferences.getString("id",null);

        if(name!=null) {
            Log.d(TAG,"Loggin Success "+name);
            LoggedInUser user = new LoggedInUser(userid, name);
            return new Result.Success<LoggedInUser>(user);
        }
        else{
            return new Result.Error(new Exception("Login Error"));
        }


    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    private void notifyResult(
            final Result<LoggedInUser> result,
            final RepositoryCallback callback,
            final Handler resultHandler
            ) {
        resultHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onComplete(result)

                ;
            }
        });
    }

}