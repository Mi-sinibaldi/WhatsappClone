package com.example.whatsappclone.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.helper.Base64Custom;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Mensagem;
import com.example.whatsappclone.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private CircleImageView circleImageFotoChat;
    private TextView textViewNomeChat;
    private FloatingActionButton fabEnviarChat;
    private EditText editTextMensagemChat;

    private Usuario usuarioDestinatario;
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadUi();

        recupedaDadosRemetente();

        //recupero as informações do usuario selecionado
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
            textViewNomeChat.setText(usuarioDestinatario.getNome());

            String foto = usuarioDestinatario.getFoto();
            if (foto != null) {
                Uri url = Uri.parse(usuarioDestinatario.getFoto());
                Glide.with(ChatActivity.this)
                        .load(url)
                        .into(circleImageFotoChat);
            } else {
                circleImageFotoChat.setImageResource(R.drawable.padrao);
            }

            fabEnviarChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enviarMensagem();
                }
            });

            recupedaDadosDestinatario();
        }
    }

    private void enviarMensagem() {
        String textoMensagem = editTextMensagemChat.getText().toString();
        if (!textoMensagem.isEmpty()) {
            //salvar a mensagem enviada
            Mensagem mensagem = new Mensagem();
            mensagem.setIdUsuario(idUsuarioRemetente);
            mensagem.setMensagem(textoMensagem);

            //salvar mensagem para o remetente
            salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

        } else {
            Toast.makeText(ChatActivity.this,
                    "Digite uma mensagem para enviar!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg) {
        DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = databaseReference.child("mensagens");

        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        //limpar tesxto
        editTextMensagemChat.setText("");
    }

    private void recupedaDadosRemetente() {
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
    }

    private void recupedaDadosDestinatario() {
        idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
    }

    private void loadUi() {
        textViewNomeChat = findViewById(R.id.textViewNomeChat);
        circleImageFotoChat = findViewById(R.id.circleImageFotoChat);
        editTextMensagemChat = findViewById(R.id.editTextMensagemChat);
        fabEnviarChat = findViewById(R.id.fabEnviarChat);

    }

}
