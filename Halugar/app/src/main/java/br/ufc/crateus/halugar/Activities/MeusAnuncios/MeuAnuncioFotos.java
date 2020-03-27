package br.ufc.crateus.halugar.Activities.MeusAnuncios;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnSingleFlingListener;
import com.github.chrisbanes.photoview.PhotoView;

import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;

public class MeuAnuncioFotos extends AppCompatActivity {

    ImageButton btnMenuMeuAnuncioFotos, btnExcluirAnuncio;
    Button btnAtualizarFotosAnuncio;
    PhotoView fotoPrincipal, fotoDois, fotoTres, fotoQuatro, fotoCinco;
    PhotoView fotoPrincipalBackground, fotoDoisBackground, fotoTresBackground, fotoQuatroBackground, fotoCincoBackground;

    private String anuncioId;

    Banco banco;
    Sessao sessao;

    final Context context = this;

    ScrollView scrollView;
    ConstraintLayout pbMeuAnuncioFotosLayoutRemover;

    String urlImagemPrincipal="", urlImagemDois="", urlImagemTres="", urlImagemQuatro="", urlImagemCinco="";

    Bundle extras;
    String callerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meu_anuncio_fotos);

        Transition.enterTransition(MeuAnuncioFotos.this); // Vem do adapter

        scrollView = (ScrollView)findViewById(R.id.scrollView2);

        fotoPrincipalBackground = (PhotoView)findViewById(R.id.ivFotoPrincipalBackgroundMeuAnuncio);
        fotoDoisBackground = (PhotoView)findViewById(R.id.ivFotoDoisBackgroundMeuAnuncio);
        fotoTresBackground = (PhotoView)findViewById(R.id.ivFotoTresBackgroundMeuAnuncio);
        fotoQuatroBackground = (PhotoView)findViewById(R.id.ivFotoQuatroBackgroundMeuAnuncio);
        fotoCincoBackground = (PhotoView)findViewById(R.id.ivFotoCincoBackgroundMeuAnuncio);

        fotoPrincipal = (PhotoView)findViewById(R.id.ivFotoPrincipal);
        fotoDois = (PhotoView) findViewById(R.id.ivFotoDois);
        fotoTres = (PhotoView) findViewById(R.id.ivFotoTres);
        fotoQuatro = (PhotoView) findViewById(R.id.ivFotoQuatro);
        fotoCinco = (PhotoView) findViewById(R.id.ivFotoCinco);

        btnMenuMeuAnuncioFotos = (ImageButton) findViewById(R.id.btnMenuMeuAnuncioFotos);
        btnAtualizarFotosAnuncio = (Button) findViewById(R.id.btnAtualizarFotosAnuncio);
        btnExcluirAnuncio = (ImageButton)findViewById(R.id.btnExcluirAnuncio);

        pbMeuAnuncioFotosLayoutRemover = (ConstraintLayout)findViewById(R.id.pbMeuAnuncioFotosLayoutRemover);

        urlImagemPrincipal = urlImagemDois = urlImagemTres = urlImagemQuatro = urlImagemCinco = "";

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        carregarExtras();
        carregarImagens();

        if(callerIntent.equals("MeusAnunciosActivity")){
            Transition.enterTransition(MeuAnuncioFotos.this); // Vem do adapter
        }

        habilitarSwipeMenu();
        habilitarZoom();

        btnAtualizarFotosAnuncio.setOnClickListener(view -> atualizarImagens());

        btnMenuMeuAnuncioFotos.setOnClickListener(view -> {
            Intent i = new Intent(MeuAnuncioFotos.this, MenuActivity.class);

            i.putExtra("callerIntent", "MeuAnuncioFotos");

            i.putExtra("idAnuncio", anuncioId);

            i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
            i.putExtra("urlImagemDois", urlImagemDois);
            i.putExtra("urlImagemTres", urlImagemTres);
            i.putExtra("urlImagemQuatro", urlImagemQuatro);
            i.putExtra("urlImagemCinco", urlImagemCinco);

            startActivity(i);
            Transition.enterTransition(MeuAnuncioFotos.this);
            finish();
        });

        btnExcluirAnuncio.setOnClickListener(view -> removerAnuncio());
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

        Intent i = new Intent(MeuAnuncioFotos.this, MeusAnunciosActivity.class);

        i.putExtra("callerIntent", "MeuAnuncioFotos");

        startActivity(i);
        Transition.backTransition(MeuAnuncioFotos.this);
        finish();
    }

    private class PhotoTapListener implements OnPhotoTapListener {
        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
            float xPercentage = x * 100f;
            float yPercentage = y * 100f;
        }
    }

    private class MatrixChangeListener implements OnMatrixChangedListener {
        @Override
        public void onMatrixChanged(RectF rect) {
        }
    }

    private class SingleFlingListener implements OnSingleFlingListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }
    }

    public void habilitarSwipeMenu(){

        scrollView.setOnTouchListener(new OnSwipeTouchListener(MeuAnuncioFotos.this) {
            public void onSwipeRight() {
                Intent i = new Intent(MeuAnuncioFotos.this, MenuActivity.class);

                i.putExtra("callerIntent", "MeuAnuncioFotos");

                i.putExtra("idAnuncio", anuncioId);

                i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                i.putExtra("urlImagemDois", urlImagemDois);
                i.putExtra("urlImagemTres", urlImagemTres);
                i.putExtra("urlImagemQuatro", urlImagemQuatro);
                i.putExtra("urlImagemCinco", urlImagemCinco);

                startActivity(i);
                Transition.enterTransition(MeuAnuncioFotos.this);
                finish();
            }
        });
    }

    public void carregarImagens(){

        extras = getIntent().getExtras();

        if(extras!=null){

            anuncioId = extras.getString("idAnuncio");
            urlImagemPrincipal = extras.getString("urlImagemPrincipal");
            urlImagemDois = extras.getString("urlImagemDois");
            urlImagemTres = extras.getString("urlImagemTres");
            urlImagemQuatro = extras.getString("urlImagemQuatro");
            urlImagemCinco = extras.getString("urlImagemCinco");

            // Setando as imagens e configurando UI:
            Glide.with(MeuAnuncioFotos.this)
                    .load(urlImagemPrincipal)
                    .into(fotoPrincipal);

            if(!urlImagemDois.equals("")){

                Glide.with(MeuAnuncioFotos.this)
                        .load(urlImagemDois)
                        .into(fotoDois);

                if(urlImagemCinco.equals("") && urlImagemQuatro.equals("") && urlImagemTres.equals("")){
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fotoDoisBackground.getLayoutParams();
                    params.setMargins(0, 0, 0, 50);
                    fotoDoisBackground.setLayoutParams(params);
                }
            }
            else{
                fotoDois.setVisibility(View.GONE);
                fotoDoisBackground.setVisibility(View.GONE);
            }

            if(!urlImagemTres.equals("")){

                Glide.with(MeuAnuncioFotos.this)
                        .load(urlImagemTres)
                        .into(fotoTres);

                if(urlImagemCinco.equals("") && urlImagemQuatro.equals("")){
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fotoTresBackground.getLayoutParams();
                    params.setMargins(0, 0, 0, 50);
                    fotoTresBackground.setLayoutParams(params);
                }
            }
            else{
                fotoTres.setVisibility(View.GONE);
                fotoTresBackground.setVisibility(View.GONE);
            }

            if(!urlImagemQuatro.equals("")){

                Glide.with(MeuAnuncioFotos.this)
                        .load(urlImagemQuatro)
                        .into(fotoQuatro);

                if(urlImagemCinco.equals("")){
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fotoQuatroBackground.getLayoutParams();
                    params.setMargins(0, 0, 0, 50);
                    fotoQuatroBackground.setLayoutParams(params);
                }
            }
            else{
                fotoQuatro.setVisibility(View.GONE);
                fotoQuatroBackground.setVisibility(View.GONE);
            }

            if(!urlImagemCinco.equals("")){

                Glide.with(MeuAnuncioFotos.this)
                        .load(urlImagemCinco)
                        .into(fotoCinco);
            }
            else{
                fotoCinco.setVisibility(View.GONE);
                fotoCincoBackground.setVisibility(View.GONE);
                //ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fotoCinco.getLayoutParams();
                //params.setMargins(0, 0, 0, 0);
                //fotoCinco.setLayoutParams(params);
            }
        }
    }

    public void habilitarZoom(){
        fotoPrincipal.setOnMatrixChangeListener(new MeuAnuncioFotos.MatrixChangeListener());
        fotoPrincipal.setOnPhotoTapListener(new MeuAnuncioFotos.PhotoTapListener());
        fotoPrincipal.setOnSingleFlingListener(new MeuAnuncioFotos.SingleFlingListener());

        fotoDois.setOnMatrixChangeListener(new MeuAnuncioFotos.MatrixChangeListener());
        fotoDois.setOnPhotoTapListener(new MeuAnuncioFotos.PhotoTapListener());
        fotoDois.setOnSingleFlingListener(new MeuAnuncioFotos.SingleFlingListener());

        fotoTres.setOnMatrixChangeListener(new MeuAnuncioFotos.MatrixChangeListener());
        fotoTres.setOnPhotoTapListener(new MeuAnuncioFotos.PhotoTapListener());
        fotoTres.setOnSingleFlingListener(new MeuAnuncioFotos.SingleFlingListener());

        fotoQuatro.setOnMatrixChangeListener(new MeuAnuncioFotos.MatrixChangeListener());
        fotoQuatro.setOnPhotoTapListener(new MeuAnuncioFotos.PhotoTapListener());
        fotoQuatro.setOnSingleFlingListener(new MeuAnuncioFotos.SingleFlingListener());

        fotoCinco.setOnMatrixChangeListener(new MeuAnuncioFotos.MatrixChangeListener());
        fotoCinco.setOnPhotoTapListener(new MeuAnuncioFotos.PhotoTapListener());
        fotoCinco.setOnSingleFlingListener(new MeuAnuncioFotos.SingleFlingListener());
    }

    public void atualizarImagens(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                Intent i = new Intent(MeuAnuncioFotos.this, EditarMeuAnuncioFotos.class);

                i.putExtra("callerIntent", "MeuAnuncioFotos");

                i.putExtra("idAnuncio", anuncioId);

                i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                i.putExtra("urlImagemDois", urlImagemDois);
                i.putExtra("urlImagemTres", urlImagemTres);
                i.putExtra("urlImagemQuatro", urlImagemQuatro);
                i.putExtra("urlImagemCinco", urlImagemCinco);

                startActivity(i);
                Transition.enterTransition(MeuAnuncioFotos.this);
                finish();
            }
        });
    }

    public void removerAnuncio(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else {
                // Exibir tela para confirmar exclusão:

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View excluirAnuncio = li.inflate(R.layout.excluir_anuncio, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(excluirAnuncio);
                alertDialogBuilder.setTitle("Confirmação");

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Sim",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                        pbMeuAnuncioFotosLayoutRemover.setVisibility(View.VISIBLE);
                                        desabilitarUI();

                                        banco.removerAnuncio(sessao.getIdUsuario(), anuncioId);

                                        // Thread para aguardar a remoção do anúncio do banco:
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                Thread thread = new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Log.i("MAIN", "removendo anúncio...");
                                                        runOnUiThread( new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Anúncio removido com sucesso", Toast.LENGTH_SHORT);

                                                                    startActivity(new Intent(MeuAnuncioFotos.this, MeusAnunciosActivity.class));
                                                                    Transition.enterTransition(MeuAnuncioFotos.this);
                                                                    finish();
                                                                }
                                                                catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    }
                                                };

                                                thread.start();
                                            }
                                        }, 2000);
                                    }
                                })
                        .setNegativeButton("Não",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });
    }

    public void habilitarUI(){
        btnMenuMeuAnuncioFotos.setEnabled(true);
        btnAtualizarFotosAnuncio.setEnabled(true);
        btnExcluirAnuncio.setEnabled(true);
    }

    public void desabilitarUI(){
        btnMenuMeuAnuncioFotos.setEnabled(false);
        btnAtualizarFotosAnuncio.setEnabled(false);
        btnExcluirAnuncio.setEnabled(false);
    }

    public void carregarExtras(){

        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent");
        }
    }
}
