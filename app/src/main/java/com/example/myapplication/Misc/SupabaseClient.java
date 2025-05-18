package com.example.myapplication.Misc;

import android.content.Context;


import okhttp3.*;
import java.io.File;
import java.io.IOException;

public class SupabaseClient {
    private static OkHttpClient client = new OkHttpClient();

    public static String uploadImage(Context context, File imageFile, String uploadPath) throws IOException {
        String supabaseUrl = "https://nebhmhytchxhfraprthm.supabase.co";
        String supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5lYmhtaHl0Y2h4aGZyYXBydGhtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc1ODk0MzksImV4cCI6MjA2MzE2NTQzOX0.sLae3GUYRG0aQHaJ7N_b3_ONCBUm9uzBd6_jN5PMJx4";
        String bucketName = "ingredient-images";

        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + uploadPath;

        MediaType mediaType = MediaType.parse("image/*");
        RequestBody body = RequestBody.create(imageFile, mediaType);

        Request request = new Request.Builder()
                .url(uploadUrl)
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", "image/*")
                .put(body)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            String responseBody = response.body() != null ? response.body().string() : "empty";
            throw new IOException("Upload failed: " + response + ", body: " + responseBody);
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + uploadPath;
    }
}
