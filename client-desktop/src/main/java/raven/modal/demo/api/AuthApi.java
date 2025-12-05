package raven.modal.demo.api;

import com.fasterxml.jackson.core.type.TypeReference;
import raven.modal.demo.dto.request.LoginRequest;
import raven.modal.demo.dto.request.RegisterRequest;
import raven.modal.demo.dto.response.ApiResponse;
import raven.modal.demo.dto.response.AuthResponse;

public class AuthApi {



    public static ApiResponse<AuthResponse> login(String email, String password) {
        try {
            LoginRequest req = new LoginRequest(email, password);

            TypeReference<ApiResponse<AuthResponse>> type =
                    new TypeReference<>() {};

            return Http.post("/auth/login", req, type);
        } catch (Exception e) {
            e.printStackTrace();
            // tuỳ style, có thể trả ApiResponse error
            return new ApiResponse<>("Lỗi kết nối: " + e.getMessage(), null, 500);
        }
    }

    public  static ApiResponse<AuthResponse> register(String email, String password, String fullName){
        try {
            RegisterRequest req = new RegisterRequest(email, fullName, password);
            TypeReference<ApiResponse<AuthResponse>> type =
                    new TypeReference<>() {};

            return Http.post("/auth/register", req, type);
        }catch (Exception e){
            e.printStackTrace();
            return new ApiResponse<>("Lỗi kết nối: " + e.getMessage(), null, 500);
        }
    }
}
