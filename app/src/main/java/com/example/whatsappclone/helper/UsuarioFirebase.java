package com.example.whatsappclone.helper;

import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.google.firebase.auth.FirebaseAuth;

public class UsuarioFirebase {

    public static String getIdentificadorUsuario(){

        FirebaseAuth usuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String email = usuario.getCurrentUser().getEmail();
        String identificadorUsusario = Base64Custom.codificarBase64(email);

        return identificadorUsusario;
    }
}
