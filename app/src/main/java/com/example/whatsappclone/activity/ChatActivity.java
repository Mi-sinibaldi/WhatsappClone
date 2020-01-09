package com.example.whatsappclone.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.adapter.MensagensAdapter;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.helper.Base64Custom;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Mensagem;
import com.example.whatsappclone.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private CircleImageView circleImageFotoChat;
    private TextView textViewNomeChat;
    private FloatingActionButton fabEnviarChat;
    private EditText editTextMensagemChat;

    private RecyclerView recyclerMensagensChat;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();

    private Usuario usuarioDestinatario;
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;

    private DatabaseReference databaseReference;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;

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

        configAdapter();
        configRecyclerView();

        databaseReference = ConfiguracaoFirebase.getFirebaseDatabase();

        mensagensRef = databaseReference.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

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

    private void configRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagensChat.setLayoutManager(layoutManager);
        recyclerMensagensChat.setHasFixedSize(true);
        recyclerMensagensChat.setAdapter(adapter);
    }

    private void configAdapter() {

        adapter = new MensagensAdapter(mensagens, getBaseContext());

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagem();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    private void recuperarMensagem() {
        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUi() {
        textViewNomeChat = findViewById(R.id.textViewNomeChat);
        circleImageFotoChat = findViewById(R.id.circleImageFotoChat);
        editTextMensagemChat = findViewById(R.id.editTextMensagemChat);
        fabEnviarChat = findViewById(R.id.fabEnviarChat);
        recyclerMensagensChat = findViewById(R.id.recyclerMensagensChat);

    }

}
