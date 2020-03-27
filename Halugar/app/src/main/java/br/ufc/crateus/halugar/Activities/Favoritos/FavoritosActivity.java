package br.ufc.crateus.halugar.Activities.Favoritos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.ufc.crateus.halugar.Activities.main.MainActivity;
import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.Model.Anuncio;
import br.ufc.crateus.halugar.Model.Usuario;
import br.ufc.crateus.halugar.ListagemAnuncios.FavoritoAdapter;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;

public class FavoritosActivity extends AppCompatActivity {

    private String anuncioKey, idMeuAnuncioFavorito;

    RecyclerView rvAnunciosFavoritos;
    FavoritoAdapter mAdapter;

    ImageButton btnMenuFavoritos;
    TextView labelQtdFavoritos, labelEstrela;
    ImageView ivNenhumAnuncioFavorito;

    int qtdFavoritos=0;
    boolean atualizarFlag=true;
    boolean encontrou=false;

    CheckBox cbOrdenar;

    Banco banco;
    Sessao sessao;

    ConstraintLayout pbFavoritosLayout;

    Toolbar.LayoutParams params;

    Bundle extras;
    String callerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        btnMenuFavoritos = (ImageButton)findViewById(R.id.btnMenuFavoritos);
        labelQtdFavoritos = (TextView)findViewById(R.id.labelQtdFavoritos);
        labelEstrela = (TextView)findViewById(R.id.labelEstrela);
        cbOrdenar = (CheckBox)findViewById(R.id.cbOrdenar);
        ivNenhumAnuncioFavorito = (ImageView)findViewById(R.id.ivNenhumAnuncioFavorito);

        pbFavoritosLayout = (ConstraintLayout)findViewById(R.id.pbFavoritosLayout);

        rvAnunciosFavoritos = (RecyclerView) findViewById(R.id.anunciosFavoritosRV);

        params = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
        );

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        carregarExtras();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvAnunciosFavoritos.setLayoutManager(layoutManager);
        mAdapter = new FavoritoAdapter(new ArrayList<>(0), getApplicationContext());
        rvAnunciosFavoritos.setAdapter(mAdapter);

        habilitarSwipeMenu();
        carregarFavoritos();

        btnMenuFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizarFlag = false; // Parar thread de atualizarFavoritos()

                Intent i = new Intent(FavoritosActivity.this, MenuActivity.class);

                i.putExtra("callerIntent", "FavoritosActivity");

                startActivity(i);
                Transition.enterTransition(FavoritosActivity.this);
                finish();
            }
        });

        cbOrdenar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //is chkIos checked?
                if (!((CheckBox) view).isChecked()) {
                    sortListDescending();
                }
                else{
                    sortListAscending();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public void onBackPressed() {

        atualizarFlag = false;

        Intent i = new Intent(FavoritosActivity.this, MainActivity.class);

        startActivity(i);
        Transition.backTransition(FavoritosActivity.this);
        finish();
    }

    public void insertItem(Anuncio anuncio) {
        mAdapter.insertItem(anuncio);
        sortListAscending();
    }

    public void sortListAscending(){
        mAdapter.sortListAscending();
    }

    public void sortListDescending(){
        mAdapter.sortListDescending();
    }

    public void habilitarSwipeMenu(){

        rvAnunciosFavoritos.setOnTouchListener(new OnSwipeTouchListener(FavoritosActivity.this) {
            public void onSwipeRight() {
                atualizarFlag = false; // Parar thread de atualizarFavoritos()

                Intent i = new Intent(FavoritosActivity.this, MenuActivity.class);

                startActivity(i);
                Transition.enterTransition(FavoritosActivity.this);
                finish();
            }
        });
    }

    public void atualizarQuantidadeFavoritos(){
        // Thread para atualizar a quantidade de anúncios favoritos continuamente:
        Thread thread = new Thread() {
            @Override
            public void run() {
                while(atualizarFlag) {
                    runOnUiThread( new Runnable() {
                        @Override
                        public void run() {

                            qtdFavoritos = mAdapter.getItemCount();

                            if(qtdFavoritos==0){
                                labelEstrela.setVisibility(View.GONE);
                                //pbFavoritosLayout.setVisibility(View.INVISIBLE);
                                labelQtdFavoritos.setText("NENHUM ANÚNCIO FAVORITO");
                                //labelQtdFavoritos.setVisibility(View.VISIBLE);
                                cbOrdenar.setVisibility(View.INVISIBLE);
                                ivNenhumAnuncioFavorito.setVisibility(View.VISIBLE);
                            }
                            else
                            if(qtdFavoritos==1){
                                //pbFavoritosLayout.setVisibility(View.INVISIBLE);
                                labelQtdFavoritos.setText(qtdFavoritos + " ANÚNCIO FAVORITO");
                                params.setMargins(145, 0, 0, 0);
                                labelEstrela.setLayoutParams(params);
                                //labelQtdFavoritos.setVisibility(View.VISIBLE);
                                //labelEstrela.setVisibility(View.VISIBLE);
                                cbOrdenar.setVisibility(View.INVISIBLE);
                                ivNenhumAnuncioFavorito.setVisibility(View.INVISIBLE);
                            }
                            else
                            if(qtdFavoritos>1 && qtdFavoritos<10){
                                //pbFavoritosLayout.setVisibility(View.INVISIBLE);
                                labelQtdFavoritos.setText(qtdFavoritos + " ANÚNCIOS FAVORITOS");
                                params.setMargins(125, 0, 0, 0);
                                labelEstrela.setLayoutParams(params);
                                //labelQtdFavoritos.setVisibility(View.VISIBLE);
                                //labelEstrela.setVisibility(View.VISIBLE);
                                cbOrdenar.setVisibility(View.VISIBLE);
                                ivNenhumAnuncioFavorito.setVisibility(View.INVISIBLE);
                            }
                            else
                            if(qtdFavoritos>=10 && qtdFavoritos<=99){
                                //pbFavoritosLayout.setVisibility(View.INVISIBLE);
                                labelQtdFavoritos.setText(qtdFavoritos + " ANÚNCIOS FAVORITOS");
                                params.setMargins(120, 0, 0, 0);
                                labelEstrela.setLayoutParams(params);
                                //labelQtdFavoritos.setVisibility(View.VISIBLE);
                                //labelEstrela.setVisibility(View.VISIBLE);
                                cbOrdenar.setVisibility(View.VISIBLE);
                                ivNenhumAnuncioFavorito.setVisibility(View.INVISIBLE);
                            }
                            else{
                                //pbFavoritosLayout.setVisibility(View.INVISIBLE);
                                labelQtdFavoritos.setText(qtdFavoritos + " ANÚNCIOS FAVORITOS");
                                params.setMargins(115, 0, 0, 0);
                                labelEstrela.setLayoutParams(params);
                                //labelQtdFavoritos.setVisibility(View.VISIBLE);
                                //labelEstrela.setVisibility(View.VISIBLE);
                                cbOrdenar.setVisibility(View.VISIBLE);
                                ivNenhumAnuncioFavorito.setVisibility(View.INVISIBLE);
                            }
                        }
                    });

                    try {
                        Thread. sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
    }

    public void carregarFavoritos(){

        // Delay para aguardar carregamento dos anúncios:
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                pbFavoritosLayout.setVisibility(View.INVISIBLE);
                //labelQtdFavoritos.setVisibility(View.VISIBLE);
                //labelEstrela.setVisibility(View.VISIBLE);

                banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot usuario : dataSnapshot.getChildren()){

                            if (sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())) {

                                String userKey = usuario.getKey();

                                // Percorrendo tabela de favoritos do usuário:
                                banco.getTabelaFavoritos(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.getValue()==null){

                                            Log.i("FAVORITOS", "dataSnapshot==null");

                                            labelEstrela.setVisibility(View.GONE);
                                            //pbFavoritosLayout.setVisibility(View.INVISIBLE);
                                            labelQtdFavoritos.setText("NENHUM ANÚNCIO FAVORITO");
                                            //labelQtdFavoritos.setVisibility(View.VISIBLE);
                                            ivNenhumAnuncioFavorito.setVisibility(View.VISIBLE);
                                        }

                                        for(DataSnapshot favorito : dataSnapshot.getChildren()){
                                            // Adicionar anuncios no recycler view:
                                            idMeuAnuncioFavorito = favorito.getValue().toString();

                                            // Procurar o anuncio favorito na tabela de anúncios:
                                            banco.getTabelaAnuncio().addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot anuncio : dataSnapshot.getChildren()) {

                                                        idMeuAnuncioFavorito = favorito.getValue().toString();
                                                        anuncioKey = anuncio.getKey();

                                                        if(idMeuAnuncioFavorito.equals(anuncioKey)){

                                                            atualizarQuantidadeFavoritos();

                                                            // Inserir anúncio
                                                            final Anuncio anuncioFavorito = new Anuncio();
                                                            anuncioFavorito.setaId(anuncio.getKey());
                                                            anuncioFavorito.setEndereco(anuncio.getValue(Anuncio.class).getEndereco());
                                                            anuncioFavorito.setNumero(anuncio.getValue(Anuncio.class).getNumero());
                                                            anuncioFavorito.setBairro(anuncio.getValue(Anuncio.class).getBairro());
                                                            anuncioFavorito.setCep(anuncio.getValue(Anuncio.class).getCep());
                                                            anuncioFavorito.setCidade(anuncio.getValue(Anuncio.class).getCidade());
                                                            anuncioFavorito.setEstado(anuncio.getValue(Anuncio.class).getEstado());
                                                            anuncioFavorito.setComplemento(anuncio.getValue(Anuncio.class).getComplemento());
                                                            anuncioFavorito.setInformacoesAdicionais(anuncio.getValue(Anuncio.class).getInformacoesAdicionais());
                                                            anuncioFavorito.setPrecoAluguel(anuncio.getValue(Anuncio.class).getPrecoAluguel());
                                                            anuncioFavorito.setQtdVagas(anuncio.getValue(Anuncio.class).getQtdVagas());
                                                            anuncioFavorito.setUrlImagemPrincipal(anuncio.getValue(Anuncio.class).getUrlImagemPrincipal());
                                                            anuncioFavorito.setUrlImagemDois(anuncio.getValue(Anuncio.class).getUrlImagemDois());
                                                            anuncioFavorito.setUrlImagemTres(anuncio.getValue(Anuncio.class).getUrlImagemTres());
                                                            anuncioFavorito.setUrlImagemQuatro(anuncio.getValue(Anuncio.class).getUrlImagemQuatro());
                                                            anuncioFavorito.setUrlImagemCinco(anuncio.getValue(Anuncio.class).getUrlImagemCinco());
                                                            anuncioFavorito.setKeyUsuario(anuncio.getValue(Anuncio.class).getKeyUsuario());
                                                            anuncioFavorito.setLatitude(anuncio.getValue(Anuncio.class).getLatitude());
                                                            anuncioFavorito.setLongitude(anuncio.getValue(Anuncio.class).getLongitude());

                                                            insertItem(anuncioFavorito);

                                                            encontrou = true;
                                                            ++qtdFavoritos;

                                                            if(qtdFavoritos==1){
                                                                labelQtdFavoritos.setText(qtdFavoritos + " ANÚNCIO FAVORITO");
                                                                params.setMargins(145, 0, 0, 0);
                                                                labelEstrela.setLayoutParams(params);
                                                                //labelQtdFavoritos.setVisibility(View.VISIBLE);
                                                                //labelEstrela.setVisibility(View.VISIBLE);
                                                                ivNenhumAnuncioFavorito.setVisibility(View.INVISIBLE);
                                                            }
                                                            else
                                                            if(qtdFavoritos>1 && qtdFavoritos<10){
                                                                labelQtdFavoritos.setText(qtdFavoritos + " ANÚNCIOS FAVORITOS");
                                                                params.setMargins(125, 0, 0, 0);
                                                                labelEstrela.setLayoutParams(params);
                                                                //labelQtdFavoritos.setVisibility(View.VISIBLE);
                                                                //labelEstrela.setVisibility(View.VISIBLE);
                                                                ivNenhumAnuncioFavorito.setVisibility(View.INVISIBLE);
                                                            }
                                                            else
                                                            if(qtdFavoritos>=10 && qtdFavoritos<=99){

                                                                labelQtdFavoritos.setText(qtdFavoritos + " ANÚNCIOS FAVORITOS");
                                                                params.setMargins(120, 0, 0, 0);
                                                                labelEstrela.setLayoutParams(params);
                                                                //labelQtdFavoritos.setVisibility(View.VISIBLE);
                                                                //labelEstrela.setVisibility(View.VISIBLE);
                                                                ivNenhumAnuncioFavorito.setVisibility(View.INVISIBLE);
                                                            }
                                                            else{
                                                                labelQtdFavoritos.setText(qtdFavoritos + " ANÚNCIOS FAVORITOS");
                                                                params.setMargins(115, 0, 0, 0);
                                                                labelEstrela.setLayoutParams(params);
                                                                //labelQtdFavoritos.setVisibility(View.VISIBLE);
                                                                //labelEstrela.setVisibility(View.VISIBLE);
                                                                ivNenhumAnuncioFavorito.setVisibility(View.INVISIBLE);
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });
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
        }, 2000);
    }

    public void carregarExtras(){

        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent");
        }
    }
}
