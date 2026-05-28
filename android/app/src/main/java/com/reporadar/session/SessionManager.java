package com.reporadar.session;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Guarda el JWT del usuario logueado en SharedPreferences.
 *
 * El acceso anonimo a la app sigue funcionando sin tocar nada: las pantallas
 * publicas (listado, detalle, busqueda) no usan esta clase. Solo se usa para
 * los favoritos y para mostrar/ocultar opciones de menu segun haya o no
 * sesion activa.
 */
public class SessionManager {

    private static final String PREFS_NAME = "repo_radar_session";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_EMAIL = "user_email";

    private static SessionManager instance;

    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveSession(String token, String email) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
