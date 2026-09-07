package com.example.sumayerestaurant.ui.login;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.sumayerestaurant.data.model.LoginResponse;
import com.example.sumayerestaurant.data.repository.AuthRepository;
import com.example.sumayerestaurant.util.ConnectivityUtil;

public class LoginViewModel extends AndroidViewModel {
    private AuthRepository authRepository;
    public MutableLiveData<String> username = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public MutableLiveData<LoginResponse> loginSuccess = new MutableLiveData<>();

    public LoginViewModel(Application application) {
        super(application);
        this.authRepository = new AuthRepository(application.getApplicationContext());
    }

    public void login() {
        String user = username.getValue();
        String pass = password.getValue();

        // Validation
        if (user == null || user.isEmpty()) {
            errorMessage.setValue("Tafadhali ingiza jina la mtumiaji");
            return;
        }

        if (pass == null || pass.isEmpty()) {
            errorMessage.setValue("Tafadhali ingiza nenosiri");
            return;
        }

        // Check network
        if (!ConnectivityUtil.isNetworkAvailable(getApplication())) {
            errorMessage.setValue("Hakuna muunganisho wa mtandao. Tafadhali angalia muunganisho wako.");
            return;
        }

        isLoading.setValue(true);
        authRepository.login(user, pass, new AuthRepository.LoginCallback() {
            @Override
            public void onLoginSuccess(LoginResponse response) {
                isLoading.setValue(false);
                loginSuccess.setValue(response);
            }

            @Override
            public void onLoginError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}
