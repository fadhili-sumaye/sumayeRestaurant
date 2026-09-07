# Phase 2: Android Login & Dashboard - Complete Implementation

## Overview

This phase builds a complete Android login and role-based dashboard that connects to your running Spring Boot backend.

### What We're Building:
1. ✅ Login screen (Kiswahili UI)
2. ✅ Connect to /api/auth/login endpoint
3. ✅ Secure JWT token storage
4. ✅ Automatic JWT token injection in requests
5. ✅ Role-based dashboard routing
6. ✅ Error handling (network, auth, validation)
7. ✅ Logout functionality

### Architecture:
```
LoginActivity (UI)
        ↓
LoginViewModel (State Management)
        ↓
AuthRepository (Business Logic)
        ↓
Retrofit API Service (Network)
        ↓
OkHttp Interceptor (JWT Token Injection)
        ↓
Spring Boot Backend (/api/auth/login)
```

---

## File Structure

All files will be created in:

```
app/src/main/
├── java/com/example/sumayerestaurant/
│   ├── ui/
│   │   ├── login/
│   │   │   ├── LoginActivity.java          ← Login screen
│   │   │   └── LoginViewModel.java         ← Login state
│   │   ├── dashboard/
│   │   │   ├── DashboardActivity.java      ← Role-based dashboard
│   │   │   ├── OwnerDashboardActivity.java
│   │   │   ├── AdminDashboardActivity.java
│   │   │   ├── ManagerDashboardActivity.java
│   │   │   ├── WaiterDashboardActivity.java
│   │   │   ├── CashierDashboardActivity.java
│   │   │   └── KitchenDashboardActivity.java
│   ├── data/
│   │   ├── api/
│   │   │   ├── RetrofitClient.java         ← API setup
│   │   │   ├── ApiService.java             ← API endpoints
│   │   │   ├── TokenInterceptor.java       ← JWT injection
│   │   │   └── response/
│   │   │       ├── LoginResponse.java
│   │   │       └── ApiResponse.java
│   │   ├── repository/
│   │   │   └── AuthRepository.java         ← Login logic
│   │   ├── local/
│   │   │   ├── TokenManager.java           ← Token storage
│   │   │   └── PreferencesManager.java     ← User preferences
│   │   └── model/
│   │       ├── LoginRequest.java
│   │       ├── User.java
│   │       └── AuthResponse.java
│   ├── util/
│   │   ├── Constants.java                  ← API URLs, constants
│   │   ├── ConnectivityUtil.java           ← Network check
│   │   └── Result.java                     ← Response wrapper
│   ├── common/
│   │   ├── BaseActivity.java               ← Base activity
│   │   └── BaseViewModel.java              ← Base viewmodel
│   └── MainActivity.java                   ← Launcher activity
│
└── res/
    ├── layout/
    │   ├── activity_login.xml
    │   ├── activity_dashboard_owner.xml
    │   ├── activity_dashboard_admin.xml
    │   ├── activity_dashboard_manager.xml
    │   ├── activity_dashboard_waiter.xml
    │   ├── activity_dashboard_cashier.xml
    │   └── activity_dashboard_kitchen.xml
    ├── values/
    │   ├── strings.xml                     ← Kiswahili strings
    │   ├── colors.xml
    │   ├── dimens.xml
    │   └── styles.xml
    ├── values-night/
    │   └── colors.xml
    └── drawable/
        └── (icons and drawables)
```

---

## Step 1: Update build.gradle.kts

**File Path**: `app/build.gradle.kts`

Replace the entire file with the version below. This adds:
- Retrofit for REST API
- OkHttp for HTTP client
- Room for local database
- ViewModel & LiveData
- Navigation Component
- Material Design 3
- Gson for JSON parsing

---

## Step 2: Create Java Data Models

All in `app/src/main/java/com/example/sumayerestaurant/`

---

## Step 3: Create Network Layer

API client, interceptors, and service definitions

---

## Step 4: Create Data & Repository Layer

Local storage and business logic

---

## Step 5: Create UI Layer

Activities and ViewModels for login and dashboard

---

## Step 6: Update Resources

Strings (Kiswahili), colors, dimensions, and layouts

---

## Complete Implementation Files

See the files below for exact code to create.
