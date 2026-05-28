package com.reporadar;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.reporadar.session.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit;
    private static Retrofit authenticatedRetrofit;

    //sin token, para proyectos, categorias, auth
    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = buildRetrofit(null);
        }
        return retrofit;
    }

    //con token explicito, para favoritos
    public static Retrofit getAuthenticatedInstance(String token) {
        authenticatedRetrofit = buildRetrofit(token);
        return authenticatedRetrofit;
    }

    //atajo: usa el token guardado en el SessionManager si lo hay
    //evita tener que pasar el token desde cada pantalla
    public static Retrofit getAuthenticatedInstance(Context context) {
        String token = SessionManager.getInstance(context).getToken();
        return buildRetrofit(token);
    }

    private static Retrofit buildRetrofit(String token) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(logging);

        //si hay token se añade un interceptor, que atrapa la peticion original y le pega una etiqueta llamada
        //authorization, con el valor del token
        if (token != null) {
            clientBuilder.addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(request);
            });
        }

        //Gson no sabe deserializar LocalDateTime por defecto. Spring Boot lo serializa
        //como string ISO-8601 ("2024-01-15T10:30:45"), asi que registramos un
        //deserializador que lo parsea con ISO_LOCAL_DATE_TIME.
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, typeOfT, ctx) ->
                                LocalDateTime.parse(json.getAsString(),
                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .create();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(clientBuilder.build())
                .build();
    }
}
