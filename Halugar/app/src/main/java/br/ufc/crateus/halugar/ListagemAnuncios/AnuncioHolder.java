package br.ufc.crateus.halugar.ListagemAnuncios;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import br.ufc.crateus.halugar.R;

public class AnuncioHolder extends RecyclerView.ViewHolder {

    public TextView tvBairroAnuncioPrincipal;
    public TextView tvPrecoAnuncioPrincipal;
    public ImageView ivAnuncioPrincipal;
    public TextView tvCidadeEstadoAnuncioPrincipal;
    public CheckBox cbFavorito;

    public AnuncioHolder(View itemView){

        super(itemView);

        tvBairroAnuncioPrincipal = (TextView)itemView.findViewById(R.id.tvBairroAnuncioRV);
        tvPrecoAnuncioPrincipal = (TextView)itemView.findViewById(R.id.tvPrecoAnuncioRV);
        ivAnuncioPrincipal = (ImageView) itemView.findViewById(R.id.ivAnuncioRV);
        tvCidadeEstadoAnuncioPrincipal = (TextView)itemView.findViewById(R.id.tvCidadeEstadoAnuncioRV);
        cbFavorito = (CheckBox) itemView.findViewById(R.id.cbFavoritoG);
    }
}
