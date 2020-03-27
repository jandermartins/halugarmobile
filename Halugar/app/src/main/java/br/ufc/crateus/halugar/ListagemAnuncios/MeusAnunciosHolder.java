package br.ufc.crateus.halugar.ListagemAnuncios;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import br.ufc.crateus.halugar.R;

public class MeusAnunciosHolder extends RecyclerView.ViewHolder {

    public TextView tvBairroMeuAnuncio;
    public TextView tvPrecoMeuAnuncio;
    public ImageView ivMeuAnuncio;
    public TextView tvCidadeEstadoMeuAnuncio;
    public ImageButton btnInformacoesMeuAnuncio;
    public ImageButton btnFotosMeuAnuncio;

    public MeusAnunciosHolder(View itemView){

        super(itemView);

        tvBairroMeuAnuncio = (TextView)itemView.findViewById(R.id.tvBairroMeuAnuncioRV);
        tvPrecoMeuAnuncio = (TextView)itemView.findViewById(R.id.tvPrecoMeuAnuncioRV);
        ivMeuAnuncio = (ImageView) itemView.findViewById(R.id.ivMeuAnuncioRV);
        tvCidadeEstadoMeuAnuncio = (TextView)itemView.findViewById(R.id.tvCidadeEstadoMeuAnuncioRV);
        btnInformacoesMeuAnuncio = (ImageButton)itemView.findViewById(R.id.btnInformacoesMeuAnuncio);
        btnFotosMeuAnuncio = (ImageButton)itemView.findViewById(R.id.btnFotosMeuAnuncio);
    }

}
