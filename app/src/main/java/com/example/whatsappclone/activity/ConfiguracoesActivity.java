package com.example.whatsappclone.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;

import com.example.whatsappclone.R;
import com.example.whatsappclone.helper.Permissao;

import de.hdodenhof.circleimageview.CircleImageView;


public class ConfiguracoesActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private ImageButton imageButtonCamera, imageButtonGaleria;
    private CircleImageView circleImageViewFotoPerfil;

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        loadUi();

        //validar as permissões
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        OpenCamera();
        OpenGalery();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //os dados sao sempre retornados, mas varia de acordo com a chamada
        if (resultCode == RESULT_OK) {
            Bitmap image = null;

            try {

                switch (requestCode) {
                    case SELECAO_CAMERA:
                        //recupero a foto tirada pela camera
                        image = (Bitmap) data.getExtras().get("data");
                        break;
                    case SELECAO_GALERIA:
                        //recupero a imagem escolhida na galeria
                        Uri localImagemSelecionada = data.getData();
                        image = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }

                if (image != null) {
                    circleImageViewFotoPerfil.setImageBitmap(image);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void OpenCamera() {
        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    //starto uma activity, mas capturo o resultado cono retorno
                    startActivityForResult(intent, SELECAO_CAMERA);
                }
            }
        });
    }

    private void OpenGalery() {
        imageButtonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    //starto uma activity, mas capturo o resultado cono retorno
                    startActivityForResult(intent, SELECAO_GALERIA);
                }
            }
        });
    }


    private void loadUi() {
        imageButtonCamera = findViewById(R.id.imageButtonCamera);
        imageButtonGaleria = findViewById(R.id.imageButtonGaleria);
        circleImageViewFotoPerfil = findViewById(R.id.circleImageViewFotoPerfil);
    }

}
