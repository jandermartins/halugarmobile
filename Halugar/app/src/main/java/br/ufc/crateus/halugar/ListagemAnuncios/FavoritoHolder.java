package br.ufc.crateus.halugar.ListagemAnuncios;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import br.ufc.crateus.halugar.R;

public class FavoritoHolder extends RecyclerView.ViewHolder {

    public TextView tvBairroAnuncioFavorito;
    public TextView tvPrecoAnuncioFavorito;
    public ImageView ivAnuncioPrincipal;
    public TextView tvCidadeEstadoAnuncioPrincipal;
    public CheckBox cbFavorito;

    public FavoritoHolder(View itemView){

        super(itemView);

        tvBairroAnuncioFavorito = (TextView)itemView.findViewById(R.id.tvBairroAnuncioRV);
        tvPrecoAnuncioFavorito = (TextView)itemView.findViewById(R.id.tvPrecoAnuncioRV);
        ivAnuncioPrincipal = (ImageView) itemView.findViewById(R.id.ivAnuncioRV);
        tvCidadeEstadoAnuncioPrincipal = (TextView)itemView.findViewById(R.id.tvCidadeEstadoAnuncioRV);
        cbFavorito = (CheckBox) itemView.findViewById(R.id.cbFavoritoG);
    }
}
