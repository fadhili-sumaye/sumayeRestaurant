package com.example.sumayerestaurant.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.sumayerestaurant.data.model.RestaurantTable;
import com.example.sumayerestaurant.data.repository.TableRepository;

import java.util.List;

public class TableViewModel extends AndroidViewModel {
    private final TableRepository tableRepository;
    private final MutableLiveData<List<RestaurantTable>> tablesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public TableViewModel(@NonNull Application application) {
        super(application);
        this.tableRepository = new TableRepository(application);
    }

    public LiveData<List<RestaurantTable>> getTables() {
        return tablesLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    public void loadTables(Long branchId) {
        if (branchId == null) {
            branchId = 1L; // default
        }
        isLoadingLiveData.setValue(true);
        errorMessageLiveData.setValue(null);

        tableRepository.getTables(branchId, new TableRepository.TableCallback<>() {
            @Override
            public void onSuccess(List<RestaurantTable> result) {
                isLoadingLiveData.postValue(false);
                tablesLiveData.postValue(result);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.postValue(false);
                errorMessageLiveData.postValue(message);
            }
        });
    }
}
