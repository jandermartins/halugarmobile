package br.ufc.crateus.halugar.ListagemAnuncios;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import br.ufc.crateus.halugar.R;

public class PesquisaHolder extends RecyclerView.ViewHolder {

    public TextView tvBairroAnuncioPesquisar;
    public TextView tvPrecoAnuncioPesquisar;
    public ImageView ivAnuncioPesquisar;
    public TextView tvCidadeEstadoAnuncioPesquisar;
    public CheckBox cbFavorito;

    public PesquisaHolder(View itemView){

        super(itemView);

        tvBairroAnuncioPesquisar = (TextView)itemView.findViewById(R.id.tvBairroAnuncioRV);
        tvPrecoAnuncioPesquisar = (TextView)itemView.findViewById(R.id.tvPrecoAnuncioRV);
        ivAnuncioPesquisar = (ImageView) itemView.findViewById(R.id.ivAnuncioRV);
        tvCidadeEstadoAnuncioPesquisar = (TextView)itemView.findViewById(R.id.tvCidadeEstadoAnuncioRV);
        cbFavorito = (CheckBox) itemView.findViewById(R.id.cbFavoritoG);
    }
}