package br.ufc.crateus.halugar.ListagemAnuncios;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import br.ufc.crateus.halugar.Activities.MeusAnuncios.MeuAnuncioDados;
import br.ufc.crateus.halugar.Activities.MeusAnuncios.MeuAnuncioFotos;
import br.ufc.crateus.halugar.Activities.MeusAnuncios.MeusAnunciosActivity;
import br.ufc.crateus.halugar.Activities.Sessao.EntrarActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Model.Anuncio;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.Transition;

public class MeusAnunciosAdapter extends RecyclerView.Adapter<MeusAnunciosHolder> {

    private final List<Anuncio> listaAnuncios;
    private Context context;

    Sessao sessao;

    public MeusAnunciosAdapter(ArrayList anuncios, Context context) {
        listaAnuncios = anuncios;
        this.context = context;
    }

    @Override
    public MeusAnunciosHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MeusAnunciosHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meus_anuncios_recycler, parent, false));
    }

    @Override
    public void onBindViewHolder(final MeusAnunciosHolder holder, final int position) {

        sessao = Sessao.getInstance();

        configurarUI(holder, position);

        // Ao clicar no ícone de informações, enviar dados para MeuAnuncioDados
        holder.btnInformacoesMeuAnuncio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                acessarDescricao(view, position);
            }
        });

        // Ao clicar no ícone de fotos, enviar dados para MeuAnuncioFotos:
        holder.btnFotosMeuAnuncio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                acessarImagens(view, position);
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

    public void sortListAscending(){
        Collections.sort(listaAnuncios, new Comparator<Anuncio>() {
            @Override
            public int compare(Anuncio a1, Anuncio a2) {
                return (int)(a1.getPrecoAluguel()-a2.getPrecoAluguel());
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortListDescending(){
        Collections.sort(listaAnuncios, new Comparator<Anuncio>() {
            @Override
            public int compare(Anuncio a1, Anuncio a2) {
                return (int)(a2.getPrecoAluguel()-a1.getPrecoAluguel());
            }
        });
        this.notifyDataSetChanged();
    }

    public void configurarUI(final MeusAnunciosHolder holder, final int position){
        Glide.with(context.getApplicationContext())
                .load(listaAnuncios.get(position).getUrlImagemPrincipal())
                .into(holder.ivMeuAnuncio);

        String cidade = listaAnuncios.get(position).getCidade();
        String estado = listaAnuncios.get(position).getEstado();
        String siglaEstado = estado.substring(estado.length()-3, estado.length()-1);

        holder.tvCidadeEstadoMeuAnuncio.setText(String.format(Locale.getDefault(), "%s (%s)", cidade.toUpperCase(), siglaEstado));
        holder.tvBairroMeuAnuncio.setText(String.format(Locale.getDefault(), "%s", listaAnuncios.get(position).getBairro()));
        holder.tvPrecoMeuAnuncio.setText(String.format(Locale.getDefault(), "R$ %.2f", listaAnuncios.get(position).getPrecoAluguel()));

        if(position==0){
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.ivMeuAnuncio.getLayoutParams();
            params.setMargins(0, 30, 0, 10);
            holder.ivMeuAnuncio.setLayoutParams(params);
        }
        else{
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.ivMeuAnuncio.getLayoutParams();
            params.setMargins(0, 0, 0, 10);
            holder.ivMeuAnuncio.setLayoutParams(params);
        }
    }

    public void acessarDescricao(View view, int position){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(context.getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                Intent i = new Intent(view.getContext(), MeuAnuncioDados.class);

                i.putExtra("callerIntent", "MeusAnunciosActivity");

                i.putExtra("idAnuncio", listaAnuncios.get(position).getaId());

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

                view.getContext().startActivity(i);
            }
        });
    }

    public void acessarImagens(View view, int position){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(context.getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                if (sessao.getUsuario() != null) {

                    final Intent i = new Intent(view.getContext(), MeuAnuncioFotos.class);

                    i.putExtra("callerIntent", "MeusAnunciosActivity");

                    i.putExtra("idAnuncio", listaAnuncios.get(position).getaId());

                    i.putExtra("urlImagemPrincipal", listaAnuncios.get(position).getUrlImagemPrincipal());
                    i.putExtra("urlImagemDois", listaAnuncios.get(position).getUrlImagemDois());
                    i.putExtra("urlImagemTres", listaAnuncios.get(position).getUrlImagemTres());
                    i.putExtra("urlImagemQuatro", listaAnuncios.get(position).getUrlImagemQuatro());
                    i.putExtra("urlImagemCinco", listaAnuncios.get(position).getUrlImagemCinco());

                    view.getContext().startActivity(i);

                } else {

                    Intent i = new Intent(view.getContext(), EntrarActivity.class);
                    view.getContext().startActivity(i);
                }
            }
        });
    }
}
