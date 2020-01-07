package com.example.whatsappclone.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.helper.Permissao;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Usuario;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;


public class ConfiguracoesActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private ImageButton imageButtonCamera, imageButtonGaleria;
    private ImageView imageAtualizarNome;
    private CircleImageView circleImageViewFotoPerfil;
    private EditText editTextNameUserPerf;

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private StorageReference storageReference;
    private String identificadorUsuario;

    private Usuario usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        loadUi();

        //Configurações iniciais
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //validar as permissões
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recupera os dados do usuario
        FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
        Uri url = usuario.getPhotoUrl();

        if (url != null) {
            Glide.with(ConfiguracoesActivity.this)
                    .load(url)
                    .into(circleImageViewFotoPerfil);
        } else {
            circleImageViewFotoPerfil.setImageResource(R.drawable.padrao);
        }

        editTextNameUserPerf.setText(usuario.getDisplayName());

        openCamera();
        openGalery();
        atualizarNome();

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

                    //recuperar os dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImage = baos.toByteArray();

                    //salvar a imagem escolhida no firebase
                    final StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("perfil")
                            //.child(identificadorUsuario)
                            .child(identificadorUsuario + ".jpeg");


                    imagemRef.putBytes(dadosImage).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return imagemRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                atualizarFotoUsuario(downloadUri);
                            } else {
                                Toast.makeText(ConfiguracoesActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void atualizarFotoUsuario(Uri url) {
        boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);
        if (retorno) {
            usuarioLogado.setFoto(url.toString());
            usuarioLogado.atualizar();

            Toast.makeText(ConfiguracoesActivity.this,
                    "Sua foto foi alterada!", Toast.LENGTH_SHORT).show();
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

    private void openCamera() {
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

    private void openGalery() {
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

    private void atualizarNome() {
        imageAtualizarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nome = editTextNameUserPerf.getText().toString();
                boolean retorno = UsuarioFirebase.atualizarNomeUsuario(nome);
                if (retorno) {

                    usuarioLogado.setNome(nome);
                    usuarioLogado.atualizar();

                    Toast.makeText(ConfiguracoesActivity.this,
                            "Nome alterado com sucesso!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadUi() {
        imageButtonCamera = findViewById(R.id.imageButtonCamera);
        imageButtonGaleria = findViewById(R.id.imageButtonGaleria);
        circleImageViewFotoPerfil = findViewById(R.id.circleImageViewFotoPerfil);
        editTextNameUserPerf = findViewById(R.id.editTextNameUserPerfil);
        imageAtualizarNome = findViewById(R.id.imageAtualizarNome);
    }
}
