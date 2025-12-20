package raven.modal.demo.api;

import com.fasterxml.jackson.core.type.TypeReference;
import raven.modal.demo.dto.request.MovieRequest;
import raven.modal.demo.dto.response.ApiResponse;
import raven.modal.demo.dto.response.MovieResponse;

import java.util.List;

public class MovieApi {

    public static ApiResponse<List<MovieResponse>> getAllMovies() {
        try {
            TypeReference<ApiResponse<List<MovieResponse>>> type = new TypeReference<>() {};
            return Http.get("/movies", type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Lỗi kết nối: " + e.getMessage(), null, 500);
        }
    }

    public static ApiResponse<MovieResponse> getMovieById(Long id) {
        try {
            TypeReference<ApiResponse<MovieResponse>> type = new TypeReference<>() {};
            return Http.get("/movies/" + id, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Lỗi kết nối: " + e.getMessage(), null, 500);
        }
    }

    public static ApiResponse<MovieResponse> createMovie(MovieRequest request) {
        try {
            TypeReference<ApiResponse<MovieResponse>> type = new TypeReference<>() {};
            return Http.post("/movies", request, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Lỗi kết nối: " + e.getMessage(), null, 500);
        }
    }

    public static ApiResponse<MovieResponse> updateMovie(Long id, MovieRequest request) {
        try {
            TypeReference<ApiResponse<MovieResponse>> type = new TypeReference<>() {};
            return Http.put("/movies/" + id, request, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Lỗi kết nối: " + e.getMessage(), null, 500);
        }
    }

    public static ApiResponse<Void> deleteMovie(Long id) {
        try {
            TypeReference<ApiResponse<Void>> type = new TypeReference<>() {};
            return Http.delete("/movies/" + id, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Lỗi kết nối: " + e.getMessage(), null, 500);
        }
    }

    public static ApiResponse<MovieResponse> reprocessMovie(Long id) {
        try {
            TypeReference<ApiResponse<MovieResponse>> type = new TypeReference<>() {};
            return Http.post("/movies/" + id + "/reprocess", null, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Lỗi kết nối: " + e.getMessage(), null, 500);
        }
    }
}
