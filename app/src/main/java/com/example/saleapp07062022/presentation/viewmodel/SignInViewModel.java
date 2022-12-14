package com.example.saleapp07062022.presentation.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.saleapp07062022.common.AppConstant;
import com.example.saleapp07062022.data.local.AppCache;
import com.example.saleapp07062022.data.model.AppResource;
import com.example.saleapp07062022.data.model.User;
import com.example.saleapp07062022.data.remote.dto.AppResponse;
import com.example.saleapp07062022.data.remote.dto.UserDTO;
import com.example.saleapp07062022.data.repository.AuthenticationRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInViewModel extends ViewModel {
    private final MutableLiveData<AppResource<User>> resourceUser = new MutableLiveData<>();
    private Context context;
    private AuthenticationRepository repository;

    public SignInViewModel(Context context) {
        this.context = context;
        repository = new AuthenticationRepository(context);
    }

    public LiveData<AppResource<User>> getResourceUser() {
        return resourceUser;
    }

    public void signIn(String email, String password) {
        resourceUser.setValue(new AppResource.Loading(null));
        repository.signIn(email, password)
                .enqueue(new Callback<AppResponse<UserDTO>>() {
                    @Override
                    public void onResponse(Call<AppResponse<UserDTO>> call, Response<AppResponse<UserDTO>> response) {
                        if (response.isSuccessful()) {
                            AppResponse<UserDTO> responseUser = response.body();
                            UserDTO userDTO = responseUser.data;
                            AppCache.getInstance(context).setValue(AppConstant.TOKEN_KEY, userDTO.getToken()).commit();
                            resourceUser.setValue(
                                    new AppResource.Success<>(
                                            new User(
                                                    userDTO.getEmail(),
                                                    userDTO.getName(),
                                                    userDTO.getPhone(),
                                                    userDTO.getToken()
                                            )
                                    )
                            );
                        } else {
                            try {
                                JSONObject jsonObject = new JSONObject(response.errorBody().string());
                                String message = jsonObject.getString("message");
                                resourceUser.setValue(new AppResource.Error<>(message));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AppResponse<UserDTO>> call, Throwable t) {
                        resourceUser.setValue(new AppResource.Error<>(t.getMessage()));
                    }
                });
    }
}
