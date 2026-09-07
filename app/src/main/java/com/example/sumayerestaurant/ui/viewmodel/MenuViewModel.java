package com.example.sumayerestaurant.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.sumayerestaurant.data.local.CartManager;
import com.example.sumayerestaurant.data.model.CartItem;
import com.example.sumayerestaurant.data.model.MenuCategory;
import com.example.sumayerestaurant.data.model.MenuItem;
import com.example.sumayerestaurant.data.repository.MenuRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenuViewModel extends AndroidViewModel {
    private final MenuRepository menuRepository;
    private final CartManager cartManager;

    private final MutableLiveData<List<MenuCategory>> categoriesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<MenuItem>> allMenuItemsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<MenuItem>> filteredMenuItemsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    private Long selectedCategoryId = null; // null means All categories
    private String searchQuery = "";

    public MenuViewModel(@NonNull Application application) {
        super(application);
        this.menuRepository = new MenuRepository(application);
        this.cartManager = CartManager.getInstance();
    }

    public LiveData<List<MenuCategory>> getCategories() {
        return categoriesLiveData;
    }

    public LiveData<List<MenuItem>> getFilteredMenuItems() {
        return filteredMenuItemsLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    public LiveData<Integer> getCartItemCount() {
        return cartManager.getItemCountLiveData();
    }

    public LiveData<Double> getCartSubtotal() {
        return cartManager.getSubtotalLiveData();
    }

    public void loadMenu(Long branchId) {
        if (branchId == null) {
            branchId = 1L;
        }

        isLoadingLiveData.setValue(true);
        errorMessageLiveData.setValue(null);

        Long finalBranchId = branchId;
        menuRepository.getCategories(finalBranchId, new MenuRepository.MenuCallback<>() {
            @Override
            public void onSuccess(List<MenuCategory> categories) {
                categoriesLiveData.postValue(categories);
                loadItems(finalBranchId);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.postValue(false);
                errorMessageLiveData.postValue(message);
            }
        });
    }

    private void loadItems(Long branchId) {
        menuRepository.getMenuItems(branchId, new MenuRepository.MenuCallback<>() {
            @Override
            public void onSuccess(List<MenuItem> items) {
                isLoadingLiveData.postValue(false);
                allMenuItemsLiveData.postValue(items);
                applyFilter(items);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.postValue(false);
                errorMessageLiveData.postValue(message);
            }
        });
    }

    public void filterByCategory(Long categoryId) {
        this.selectedCategoryId = categoryId;
        List<MenuItem> all = allMenuItemsLiveData.getValue();
        if (all != null) {
            applyFilter(all);
        }
    }

    public void search(String query) {
        this.searchQuery = query != null ? query.trim().toLowerCase() : "";
        List<MenuItem> all = allMenuItemsLiveData.getValue();
        if (all != null) {
            applyFilter(all);
        }
    }

    private void applyFilter(List<MenuItem> items) {
        List<MenuItem> filtered = items.stream().filter(item -> {
            boolean matchesCategory = (selectedCategoryId == null) ||
                    (item.getCategory() != null && selectedCategoryId.equals(item.getCategory().getId()));
            boolean matchesSearch = searchQuery.isEmpty() ||
                    (item.getName() != null && item.getName().toLowerCase().contains(searchQuery)) ||
                    (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchQuery));
            return matchesCategory && matchesSearch;
        }).collect(Collectors.toList());

        filteredMenuItemsLiveData.postValue(filtered);
    }

    public void addItemToCart(MenuItem item, int quantity, String specialInstructions) {
        cartManager.addItem(item, quantity, specialInstructions);
    }

    public int getItemQuantityInCart(Long menuItemId) {
        for (CartItem ci : cartManager.getItems()) {
            if (ci.getMenuItem().getId().equals(menuItemId)) {
                return ci.getQuantity();
            }
        }
        return 0;
    }
}
