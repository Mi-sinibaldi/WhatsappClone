package com.example.whatsappclone.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requestCode) {

        if (Build.VERSION.SDK_INT >= 23) { //solicito somente para versoes maiores ou igual a versaos 23

            List<String> listaPermissoes = new ArrayList<>();
            //percorre as permissoes passadas e verifica cada uma
            for (String permissao : permissoes) {
                boolean temPermissao = ContextCompat.checkSelfPermission(activity, permissao) ==
                        PackageManager.PERMISSION_GRANTED;
                if (!temPermissao) listaPermissoes.add(permissao);
            }

            if (listaPermissoes.isEmpty()) return true;

            //Converte o List para um Array
            String[] novasPermissoes = new String[listaPermissoes.size()];
            listaPermissoes.toArray(novasPermissoes);

            //solicitar permissoes
            ActivityCompat.requestPermissions(activity, novasPermissoes, requestCode);
        }
        return true;
    }
}
