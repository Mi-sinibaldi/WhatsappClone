package com.example.whatsappclone.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Mensagem;

import java.util.List;

public class MensagensAdapter extends RecyclerView.Adapter<MensagensAdapter.MyViewHolder> {

    private List<Mensagem> mensagens;
    private Context context;

    private static final int TIPO_REMETENTE = 0;
    private static final int TIPO_DESTINATARIO = 1;

    public MensagensAdapter(List<Mensagem> lista, Context c) {
        mensagens = lista;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = null;
        if (viewType == TIPO_REMETENTE) {
            item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_mensagem_remetente, parent, false);
        } else if (viewType == TIPO_DESTINATARIO) {
            item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_mensagem_destinatario, parent, false);
        }

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Mensagem mensagem = mensagens.get(position);
        String msg = mensagem.getMensagem();
        String imagem = mensagem.getImagem();

        if (imagem != null) {
            //se o ususario mandar uma imagem, ele carrega e exibe a imagem
            Uri url = Uri.parse(imagem);
            Glide.with(context).load(url).into(holder.imagem);

            //escondo o texto quando tenho uam imagem
            holder.mensagem.setVisibility(View.GONE);
        } else {
            //senao ele exibe o texto da mensagem
            holder.mensagem.setText(msg);

            //escondo a imagem quando eu tenho um texto
            holder.imagem.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    //retorna o tipo da visualizacao
    @Override
    public int getItemViewType(int position) {
        Mensagem mensagem = mensagens.get(position);

        String idUuario = UsuarioFirebase.getIdentificadorUsuario();
        if (idUuario.equals(mensagem.getIdUsuario())) {
            return TIPO_REMETENTE;
        }

        return TIPO_DESTINATARIO;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mensagem;
        ImageView imagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mensagem = itemView.findViewById(R.id.textViewMensagemTexto);
            imagem = itemView.findViewById(R.id.imageViewMensagemFoto);
        }
    }
}
