package br.ufc.crateus.halugar.ListagemAnuncios;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.ufc.crateus.halugar.Activities.Anuncio.AnuncioActivity;
import br.ufc.crateus.halugar.Activities.Sessao.EntrarActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.Model.Anuncio;
import br.ufc.crateus.halugar.Model.Usuario;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;

import static android.view.View.GONE;

public class AnuncioAdapter extends RecyclerView.Adapter<AnuncioHolder> {

    private final List<Anuncio> listaAnuncios;
    private Context context;
    String idAnunciante, userKey;
    String nome, telefone, email;

    String idAnuncio;
    String idAnuncioPos;
    boolean encontrou = false;

    Banco banco;
    Sessao sessao;

    public AnuncioAdapter(ArrayList anuncios, Context context) {
        listaAnuncios = anuncios;
        this.context = context;
    }

    @Override
    public AnuncioHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AnuncioHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.anuncio_recycler, parent, false));
    }

    @Override
    public void onBindViewHolder(final AnuncioHolder holder, final int position) {

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        configurarUI(holder, position);

        // Ao clicar na imagem, enviar informações para exibição na AnuncioActivity:
        holder.ivAnuncioPrincipal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                abrirAnuncio(view, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaAnuncios != null ? listaAnuncios.size() : 0;
    }

    public void insertItem(Anuncio anuncio) {
        listaAnuncios.add(anuncio);
        notifyItemInserted(getItemCount());
    }

    public void clearList() {
        int size = listaAnuncios.size();
        listaAnuncios.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void abrirAnuncio(View view, int position){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(context.getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                if (sessao.getUsuario()!=null) {

                    // Temos que enviar o idUsuario para AnuncioActivity:

                    final Intent i = new Intent(view.getContext(), AnuncioActivity.class);

                    i.putExtra("idAnuncio", listaAnuncios.get(position).getaId()); // utilizado para verificar favorito

                    i.putExtra("Endereco", listaAnuncios.get(position).getEndereco());
                    i.putExtra("Numero", listaAnuncios.get(position).getNumero());
                    i.putExtra("Complemento", listaAnuncios.get(position).getComplemento());
                    i.putExtra("CEP", listaAnuncios.get(position).getCep());
                    i.putExtra("Bairro", listaAnuncios.get(position).getBairro());
                    i.putExtra("Cidade", listaAnuncios.get(position).getCidade());
                    i.putExtra("Estado", listaAnuncios.get(position).getEstado());
                    i.putExtra("Preco", listaAnuncios.get(position).getPrecoAluguel());
                    i.putExtra("Vagas", listaAnuncios.get(position).getQtdVagas());
                    i.putExtra("Informacoes", listaAnuncios.get(position).getInformacoesAdicionais());

                    i.putExtra("LatitudeAnuncio", listaAnuncios.get(position).getLatitude());
                    i.putExtra("LongitudeAnuncio", listaAnuncios.get(position).getLongitude());

                    i.putExtra("urlImagemPrincipal", listaAnuncios.get(position).getUrlImagemPrincipal());
                    i.putExtra("urlImagemDois", listaAnuncios.get(position).getUrlImagemDois());
                    i.putExtra("urlImagemTres", listaAnuncios.get(position).getUrlImagemTres());
                    i.putExtra("urlImagemQuatro", listaAnuncios.get(position).getUrlImagemQuatro());
                    i.putExtra("urlImagemCinco", listaAnuncios.get(position).getUrlImagemCinco());

                    //Log.i("FOTO - URL", listaAnuncios.get(position).getUrlImagemDois());

                    i.putExtra("idUsuario", listaAnuncios.get(position).getKeyUsuario());

                    // Procurando os dados do anunciante pela key:

                    banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot usuario : dataSnapshot.getChildren()){

                                idAnunciante = listaAnuncios.get(position).getKeyUsuario();

                                if (idAnunciante.equals(usuario.getValue(Usuario.class).getuKey())) {

                                    nome = usuario.child("nomeCompleto").getValue().toString();
                                    telefone = usuario.child("telefone").getValue().toString();
                                    email = usuario.child("email").getValue().toString();

                                    i.putExtra("nomeAnunciante", nome);
                                    i.putExtra("telefoneAnunciante", telefone);
                                    i.putExtra("emailAnunciante", email);

                                    view.getContext().startActivity(i);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {

                    Intent i = new Intent(view.getContext(), EntrarActivity.class);
                    view.getContext().startActivity(i);
                }
            }
        });
    }

    public void configurarUI(final AnuncioHolder holder, final int position){

        if(sessao.getUsuario()==null){

            Glide.with(context.getApplicationContext())
                    .load(listaAnuncios.get(position).getUrlImagemPrincipal())
                    .into(holder.ivAnuncioPrincipal);

            String cidade = listaAnuncios.get(position).getCidade();
            String estado = listaAnuncios.get(position).getEstado();
            String siglaEstado = estado.substring(estado.length()-3, estado.length()-1);

            holder.tvCidadeEstadoAnuncioPrincipal.setText(String.format(Locale.getDefault(), "%s (%s)", cidade.toUpperCase(), siglaEstado));
            holder.tvBairroAnuncioPrincipal.setText(String.format(Locale.getDefault(), "%s", listaAnuncios.get(position).getBairro()));
            holder.tvPrecoAnuncioPrincipal.setText(String.format(Locale.getDefault(), "R$ %.2f", listaAnuncios.get(position).getPrecoAluguel()));

            holder.cbFavorito.setVisibility(GONE);

            if(position==0){
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.ivAnuncioPrincipal.getLayoutParams();
                params.setMargins(0, 60, 0, 0);
                holder.ivAnuncioPrincipal.setLayoutParams(params);
            }
            else{
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.ivAnuncioPrincipal.getLayoutParams();
                params.setMargins(0, 50, 0, 0);
                holder.ivAnuncioPrincipal.setLayoutParams(params);
            }

            if(position==listaAnuncios.size()-1){
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.tvPrecoAnuncioPrincipal.getLayoutParams();
                params.setMargins(0, 0, 0, 50);
                holder.tvPrecoAnuncioPrincipal.setLayoutParams(params);
            }
        }
        else{

            Glide.with(context.getApplicationContext())
                    .load(listaAnuncios.get(position).getUrlImagemPrincipal())
                    .into(holder.ivAnuncioPrincipal);

            String cidade = listaAnuncios.get(position).getCidade();
            String estado = listaAnuncios.get(position).getEstado();
            String siglaEstado = estado.substring(estado.length()-3, estado.length()-1);

            holder.tvCidadeEstadoAnuncioPrincipal.setText(String.format(Locale.getDefault(), "%s (%s)", cidade.toUpperCase(), siglaEstado));
            holder.tvBairroAnuncioPrincipal.setText(String.format(Locale.getDefault(), "%s", listaAnuncios.get(position).getBairro()));
            holder.tvPrecoAnuncioPrincipal.setText(String.format(Locale.getDefault(), "R$ %.2f", listaAnuncios.get(position).getPrecoAluguel()));

            if(position==0){
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.ivAnuncioPrincipal.getLayoutParams();
                params.setMargins(0, 60, 0, 0);
                holder.ivAnuncioPrincipal.setLayoutParams(params);
            }
            else{

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.ivAnuncioPrincipal.getLayoutParams();
                params.setMargins(0, 70, 0, 0);
                holder.ivAnuncioPrincipal.setLayoutParams(params);
            }

            if(position==listaAnuncios.size()-1){
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.tvPrecoAnuncioPrincipal.getLayoutParams();
                params.setMargins(0, 0, 0, 40);
                holder.tvPrecoAnuncioPrincipal.setLayoutParams(params);
            }

            idAnuncio = listaAnuncios.get(position).getaId();
            encontrou = false;

            verificarFavorito(holder, position);

            atualizarFavorito(holder, position);
        }
    }

    public void verificarFavorito(final AnuncioHolder holder, final int position){
        // Verificar se o anúncio é favorito ou não e atualizar check box:

        banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot usuario : dataSnapshot.getChildren()){

                    //Log.i("AQUI - TESTE", idAnuncio);

                    if (sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())) {

                        userKey = usuario.getKey();

                        // Percorrendo tabela de favoritos do usuário atual:

                        banco.getTabelaFavoritos(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot favorito : dataSnapshot.getChildren()){

                                    idAnuncio = listaAnuncios.get(position).getaId();
                                    encontrou = false;

                                    // comparar id do anuncio atual com ids da lista de favoritos...

                                    if(idAnuncio.equals(favorito.getValue())){

                                        encontrou=true;
                                        holder.cbFavorito.setChecked(true);
                                        holder.cbFavorito.setVisibility(View.VISIBLE);
                                    }
                                }

                                holder.cbFavorito.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void atualizarFavorito(final AnuncioHolder holder, final int position){
        // Realizar atualização do favorito ao clicar no check box:
        // Se é favorito -> Remover | Se não é favorito -> Adicionar:

        holder.cbFavorito.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new InternetCheck(internet -> {
                    if(!internet){
                        CustomToast.mostrarMensagem(context.getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
                    }
                    else{
                        // REcuperar item pela posicao na lista:

                        for( int i=0; i<listaAnuncios.size(); i++){

                            if(position==i){

                                idAnuncioPos = listaAnuncios.get(i).getaId();

                                //is chkIos checked?
                                if (!((CheckBox) v).isChecked()) {
                                    banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            for(DataSnapshot usuario : dataSnapshot.getChildren()){

                                                if (sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())) {

                                                    userKey = usuario.getKey();

                                                    // Percorrendo tabela de favoritos do usuário atual:

                                                    banco.getTabelaFavoritos(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                            for(DataSnapshot favorito : dataSnapshot.getChildren()){

                                                                // comparar id do anuncio atual com ids da lista de favoritos...

                                                                //Log.i("AQUI - REM", position + " = atual: " + idAnuncio + " | meu: " + fav.getValue());

                                                                if(favorito.getValue().equals(idAnuncioPos)){

                                                                    String favoritoKey = favorito.getKey();
                                                                    encontrou=true;
                                                                    banco.getTabelaFavoritos(userKey).child(favoritoKey).removeValue();
                                                                    holder.cbFavorito.setChecked(false);

                                                                    CustomToast.mostrarMensagem(context.getApplicationContext(), "Removido dos favoritos", Toast.LENGTH_SHORT);
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });

                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                                else{

                                    // Adicionar aos favoritos:

                                    banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                                                if (sessao.getIdUsuario().equals(snapshot.getValue(Usuario.class).getuId())) {

                                                    userKey = snapshot.getKey();

                                                    //Toast.makeText(context, "ADD" + position, Toast.LENGTH_LONG).show();

                                                    // Percorrendo tabela de favoritos do usuário atual:

                                                    //Log.i("AQUI - ADD", position + " = atual: " + idAnuncioPos);

                                                    CustomToast.mostrarMensagem(context.getApplicationContext(), "Adicionado aos favoritos", Toast.LENGTH_SHORT);

                                                    banco.getTabelaFavoritos(userKey).push().setValue(idAnuncioPos);
                                                    holder.cbFavorito.setChecked(true);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }
        });
    }
}