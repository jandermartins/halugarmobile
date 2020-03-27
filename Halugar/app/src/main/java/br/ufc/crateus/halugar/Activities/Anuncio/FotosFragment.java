package br.ufc.crateus.halugar.Activities.Anuncio;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnSingleFlingListener;
import com.github.chrisbanes.photoview.PhotoView;

import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;

public class FotosFragment extends Fragment {

    private PhotoView fotoPrincipal, fotoDois, fotoTres, fotoQuatro, fotoCinco;
    private PhotoView fotoPrincipalBackground, fotoDoisBackground, fotoTresBackground, fotoQuatroBackground, fotoCincoBackground;

    Bundle arguments;
    String urlImagemPrincipal, urlImagemDois, urlImagemTres, urlImagemQuatro, urlImagemCinco;

    ScrollView scrollView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arguments =  getArguments();

        if(arguments==null){
            Log.i("TESTE - FRAGMENT", "FOTO: arguments==null");
            return;
        }

        urlImagemPrincipal = arguments.getString("UrlImagemPrincipal");
        urlImagemDois = arguments.getString("UrlImagemDois");
        urlImagemTres = arguments.getString("UrlImagemTres");
        urlImagemQuatro = arguments.getString("UrlImagemQuatro");
        urlImagemCinco = arguments.getString("UrlImagemCinco");
    }

    @Override
    public void onStart() {
        super.onStart();
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getActivity().getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FotosActivity fotosActivity;

        fotosActivity = ViewModelProviders.of(this).get(FotosActivity.class);
        View root = inflater.inflate(R.layout.fragment_fotos, container, false);

        fotoPrincipal = (PhotoView) root.findViewById(R.id.ivFotoPrincipal);
        fotoDois = (PhotoView)root.findViewById(R.id.ivFotoDois);
        fotoTres = (PhotoView)root.findViewById(R.id.ivFotoTres);
        fotoQuatro = (PhotoView)root.findViewById(R.id.ivFotoQuatro);
        fotoCinco = (PhotoView) root.findViewById(R.id.ivFotoCinco);

        fotoPrincipalBackground = (PhotoView) root.findViewById(R.id.ivFotoPrincipalBackground);
        fotoDoisBackground = (PhotoView)root.findViewById(R.id.ivFotoDoisBackground);
        fotoTresBackground = (PhotoView)root.findViewById(R.id.ivFotoTresBackground);
        fotoQuatroBackground = (PhotoView)root.findViewById(R.id.ivFotoQuatroBackground);
        fotoCincoBackground = (PhotoView) root.findViewById(R.id.ivFotoCincoBackground);

        scrollView = (ScrollView)root.findViewById(R.id.scrollView2);

        habilitarSwipeMenu();

        fotosActivity.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                carregarImagens();
            }
        });

        habilitarZoom();

        return root;
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
        scrollView.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
            public void onSwipeRight() {
                //Toast.makeText(getApplicationContext(), "RIGHT", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(getActivity(), MenuActivity.class));
                Intent i = new Intent(getActivity(), MenuActivity.class);
                i.putExtra("callerIntent", "FotosFragment");
                startActivity(i);
            }
        });
    }

    public void carregarImagens(){
        Glide.with(FotosFragment.this)
                .load(urlImagemPrincipal)
                .into(fotoPrincipal);

        if(!urlImagemDois.equals("")){

            Glide.with(FotosFragment.this)
                    .load(urlImagemDois)
                    .into(fotoDois);

            if(urlImagemCinco.equals("") && urlImagemQuatro.equals("") && urlImagemTres.equals("")){

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fotoDoisBackground.getLayoutParams();
                params.setMargins(0, 0, 0, 170);
                fotoDoisBackground.setLayoutParams(params);
            }
        }
        else{

            fotoDois.setVisibility(View.GONE);
            fotoDoisBackground.setVisibility(View.GONE);
        }

        if(!urlImagemTres.equals("")){

            Glide.with(FotosFragment.this)
                    .load(urlImagemTres)
                    .into(fotoTres);

            if(urlImagemCinco.equals("") && urlImagemQuatro.equals("")){

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fotoTresBackground.getLayoutParams();
                params.setMargins(0, 0, 0, 170);
                fotoTresBackground.setLayoutParams(params);
            }
        }
        else{

            fotoTres.setVisibility(View.GONE);
            fotoTresBackground.setVisibility(View.GONE);
        }

        if(!urlImagemQuatro.equals("")){

            Glide.with(FotosFragment.this)
                    .load(urlImagemQuatro)
                    .into(fotoQuatro);

            if(urlImagemCinco.equals("")){

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fotoQuatroBackground.getLayoutParams();
                params.setMargins(0, 0, 0, 170);
                fotoQuatroBackground.setLayoutParams(params);
            }
        }
        else{

            fotoQuatro.setVisibility(View.GONE);
            fotoQuatroBackground.setVisibility(View.GONE);
        }

        if(!urlImagemCinco.equals("")){

            Glide.with(FotosFragment.this)
                    .load(urlImagemCinco)
                    .into(fotoCinco);
        }
        else {

            fotoCinco.setVisibility(View.GONE);
            fotoCincoBackground.setVisibility(View.GONE);
        }
    }

    public void habilitarZoom(){
        fotoPrincipal.setOnMatrixChangeListener(new MatrixChangeListener());
        fotoPrincipal.setOnPhotoTapListener(new PhotoTapListener());
        fotoPrincipal.setOnSingleFlingListener(new SingleFlingListener());

        fotoDois.setOnMatrixChangeListener(new MatrixChangeListener());
        fotoDois.setOnPhotoTapListener(new PhotoTapListener());
        fotoDois.setOnSingleFlingListener(new SingleFlingListener());

        fotoTres.setOnMatrixChangeListener(new MatrixChangeListener());
        fotoTres.setOnPhotoTapListener(new PhotoTapListener());
        fotoTres.setOnSingleFlingListener(new SingleFlingListener());

        fotoQuatro.setOnMatrixChangeListener(new MatrixChangeListener());
        fotoQuatro.setOnPhotoTapListener(new PhotoTapListener());
        fotoQuatro.setOnSingleFlingListener(new SingleFlingListener());

        fotoCinco.setOnMatrixChangeListener(new MatrixChangeListener());
        fotoCinco.setOnPhotoTapListener(new PhotoTapListener());
        fotoCinco.setOnSingleFlingListener(new SingleFlingListener());
    }
}
