package br.ufc.crateus.halugar.Activities.Anunciar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.MeusAnuncios.MeusAnunciosActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.Model.Anuncio;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;
import id.zelory.compressor.Compressor;

import static android.view.View.VISIBLE;

public class AnunciarFotosImovelActivity extends AppCompatActivity {

    final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 123;

    String endereco, complemento, cep, bairro, cidade, estado, informacoes;
    int numero, vagas;
    double preco;

    ImageButton ibImagemPrincipal, ibSelecionarImagemPrincipal, ibImagemDois, ibSelecionarImagemDois, ibImagemTres,
            ibSelecionarImagemTres, ibImagemQuatro, ibSelecionarImagemQuatro, ibImagemCinco, ibSelecionarImagemCinco, btnMenuAnunciar,
            ibRemoverImagemTres, ibRemoverImagemDois, ibRemoverImagemQuatro, ibRemoverImagemCinco;
    ImageButton ibImagemPrincipalBackground;
    TextView labelImagemPrincipal;

    Button btnAnunciar;

    private Uri filePathImagemPrincipal, filePathImagemDois, filePathImagemTres, filePathImagemQuatro, filePathImagemCinco;
    private String idImagemPrincipal, idImagemDois, idImagemTres, idImagemQuatro, idImagemCinco;
    private String urlImagemPrincipal="", urlImagemDois="", urlImagemTres="", urlImagemQuatro="", urlImagemCinco="";
    private String urlImagemPrincipalNovo, urlImagemDoisNovo, urlImagemTresNovo, urlImagemQuatroNovo, urlImagemCincoNovo;

    final Context context = this;

    int idPicImage;
    String arquivoImagem;

    boolean camera, galeria;

    boolean imagemPrincipal, imagemDois, imagemTres, imagemQuatro, imagemCinco;

    boolean localizacaoEncontrada = false;
    String latitudeAnuncio, longitudeAnuncio;
    double latitude=0, longitude=0;
    String enderecoPesquisa;
    String estadoNome;

    InputStream is;
    String input;

    ConstraintLayout pbAnunciarFotosLayout;
    ScrollView scrollView;

    long ultimoClique = 0;

    Banco banco;
    Sessao sessao;
    StorageReference imagem;

    Bundle extras;
    String callerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anunciar_fotos_imovel);

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        btnMenuAnunciar = (ImageButton)findViewById(R.id.btnMenuAnunciarFotos);
        pbAnunciarFotosLayout = (ConstraintLayout)findViewById(R.id.pbAnunciarFotosLayout);

        scrollView = (ScrollView)findViewById(R.id.scrollView3);

        ibImagemPrincipal = (ImageButton)findViewById(R.id.ibImagemPrincipal);
        ibSelecionarImagemPrincipal = (ImageButton)findViewById(R.id.ibSelecionarImagemPrincipal);
        ibImagemDois = (ImageButton)findViewById(R.id.ibImagemDois);
        ibSelecionarImagemDois = (ImageButton)findViewById(R.id.ibSelecionarImagemDois);
        ibImagemTres = (ImageButton)findViewById(R.id.ibImagemTres);
        ibSelecionarImagemTres = (ImageButton)findViewById(R.id.ibSelecionarImagemTres);
        ibImagemQuatro = (ImageButton)findViewById(R.id.ibImagemQuatro);
        ibSelecionarImagemQuatro = (ImageButton)findViewById(R.id.ibSelecionarImagemQuatro);
        ibImagemCinco = (ImageButton)findViewById(R.id.ibImagemCinco);
        ibSelecionarImagemCinco = (ImageButton)findViewById(R.id.ibSelecionarImagemCinco);

        ibImagemPrincipalBackground = (ImageButton)findViewById(R.id.ibImagemPrincipalBackground);

        ibRemoverImagemDois = (ImageButton)findViewById(R.id.ibRemoverImagemDois);
        ibRemoverImagemTres = (ImageButton)findViewById(R.id.ibRemoverImagemTres);
        ibRemoverImagemQuatro = (ImageButton)findViewById(R.id.ibRemoverImagemQuatro);
        ibRemoverImagemCinco = (ImageButton)findViewById(R.id.ibRemoverImagemCinco);

        ibRemoverImagemDois.setVisibility(View.INVISIBLE);
        ibRemoverImagemTres.setVisibility(View.INVISIBLE);
        ibRemoverImagemQuatro.setVisibility(View.INVISIBLE);
        ibRemoverImagemCinco.setVisibility(View.INVISIBLE);

        btnAnunciar = (Button) findViewById(R.id.btnAnunciar);

        labelImagemPrincipal = (TextView)findViewById(R.id.tvFotoPrincipal);

        urlImagemPrincipal = urlImagemDois = urlImagemTres = urlImagemQuatro = urlImagemCinco = "";
        urlImagemPrincipalNovo = urlImagemDoisNovo = urlImagemTresNovo = urlImagemQuatroNovo = urlImagemCincoNovo = "";

        imagemPrincipal = imagemDois = imagemTres = imagemQuatro = imagemCinco = false;

        latitude = longitude = 0;

        carregarExtras();

        enderecoPesquisa = endereco + ", " + numero + ", " + bairro + ", " + cidade + ", " + estadoNome;

        habilitarSwipeMenu();
        pesquisarLocalizacao();

        ibSelecionarImagemPrincipal.setOnClickListener(v -> selecionarImagemPrincipal());

        ibSelecionarImagemDois.setOnClickListener(view -> selecionarImagemDois());

        ibSelecionarImagemTres.setOnClickListener(view -> selecionarImagemTres());

        ibSelecionarImagemQuatro.setOnClickListener(view -> selecionarImagemQuatro());

        ibSelecionarImagemCinco.setOnClickListener(view -> selecionarImagemCinco());

        ibRemoverImagemDois.setOnClickListener(view -> removerImagemDois());

        ibRemoverImagemTres.setOnClickListener(view -> removerImagemTres());

        ibRemoverImagemQuatro.setOnClickListener(view -> removerImagemQuatro());

        ibRemoverImagemCinco.setOnClickListener(view -> removerImagemCinco());

        btnAnunciar.setOnClickListener(view -> criarAnuncio());

        btnMenuAnunciar.setOnClickListener(view -> {

            Intent i = new Intent(AnunciarFotosImovelActivity.this, MenuActivity.class);

            i.putExtra("callerIntent", "AnunciarFotosImovelActivity");

            i.putExtra("Endereco", endereco);
            i.putExtra("Numero", numero);
            i.putExtra("Complemento", complemento);
            i.putExtra("CEP", cep);
            i.putExtra("Bairro", bairro);
            i.putExtra("Cidade", cidade);
            i.putExtra("Estado", estado);
            i.putExtra("Preco", preco);
            i.putExtra("Vagas", vagas);
            i.putExtra("Informacoes", informacoes);
            i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
            i.putExtra("urlImagemDois", urlImagemDois);
            i.putExtra("urlImagemTres", urlImagemTres);
            i.putExtra("urlImagemQuatro", urlImagemQuatro);
            i.putExtra("urlImagemCinco", urlImagemCinco);

            startActivity(i);
            Transition.enterTransition(AnunciarFotosImovelActivity.this);
            finish();
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
    protected void onDestroy() {
        super.onDestroy();
        //removerFotos();
    }

    public class Async extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL data = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) data.openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                input = convertInputStreamToString(is);

                if(input.equals("[]")){
                    localizacaoEncontrada = false;
                }

                Log.i("LOCALIZACAO (async)", "Lat: " + latitudeAnuncio + " & Lon: " + longitudeAnuncio);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return input;
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);

            JSONArray jsonArray = null;

            try {
                jsonArray = new JSONArray(string);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                String str = jsonArray.getJSONObject(0).toString();
                latitudeAnuncio = jsonArray.getJSONObject(0).getString("lat");
                longitudeAnuncio = jsonArray.getJSONObject(0).getString("lon");

                // FAZER VERIFICAÇÂO DO OBJETO JSON (nulo)
                Log.i("LOCALIZACAO (retorno)", "Lat: " + latitudeAnuncio + " & Lon: " + longitudeAnuncio);
                Log.i("LOCALIZACAO (json)", str);

                if(latitudeAnuncio==null || longitudeAnuncio==null){
                    localizacaoEncontrada = false;
                } else {
                    if (latitudeAnuncio!=null && longitudeAnuncio!=null) {

                        latitude = Double.parseDouble(latitudeAnuncio);
                        longitude = Double.parseDouble(longitudeAnuncio);
                        localizacaoEncontrada = true;

                        Log.i("AQUI - NO Async", "LAT: " + latitude + " | LON: " + longitude);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader;
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        inputStream.close();

        if(result.equals("[]")){
            localizacaoEncontrada = false;
        }

        return result;
    }

    public void uploadFile() {

        if(filePathImagemPrincipal!=null && imagemPrincipal==true) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Aguarde...");
            progressDialog.show();

            idImagemPrincipal = UUID.randomUUID().toString();

            //final StorageReference ref = storageReference.child("images/"+ idImagemPrincipal);

            imagem = banco.getImagem(idImagemPrincipal);

            imagem.putFile(filePathImagemPrincipal)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            imagem.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    urlImagemPrincipalNovo = String.valueOf(uri);

                                    // Colocando a imagem novamente:

                                    Glide.with(AnunciarFotosImovelActivity.this)
                                            .load(urlImagemPrincipalNovo)
                                            .into(ibImagemPrincipal);

                                    progressDialog.dismiss();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem carregada com sucesso", Toast.LENGTH_SHORT);

                                    configurarCamposInvalidosUI();

                                    // Removendo imagem anterior do storage (se existir):
                                    if(!urlImagemPrincipal.equals("")){
                                        banco.removerImagem(urlImagemPrincipal);
                                        urlImagemPrincipal = urlImagemPrincipalNovo;
                                    }
                                    else{
                                        urlImagemPrincipal = urlImagemPrincipalNovo;
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            //Toast.makeText(AnunciarActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao carregar imagem", Toast.LENGTH_SHORT);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Carregando imagem "+(int)progress+"%");
                        }
                    });
        }

        if(filePathImagemDois!=null && imagemDois==true) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Aguarde...");
            progressDialog.show();

            idImagemDois = UUID.randomUUID().toString();

            //final StorageReference ref = storageReference.child("images/"+ idImagemDois);

            imagem = banco.getImagem(idImagemDois);

            imagem.putFile(filePathImagemDois)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            imagem.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    urlImagemDoisNovo = String.valueOf(uri);

                                    // Colocando a imagem novamente:

                                    Glide.with(AnunciarFotosImovelActivity.this)
                                            .load(urlImagemDoisNovo)
                                            .into(ibImagemDois);

                                    progressDialog.dismiss();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem carregada com sucesso", Toast.LENGTH_SHORT);
                                    ibRemoverImagemDois.setVisibility(VISIBLE);

                                    // Removendo imagem anterior do storage (se existir):
                                    if(!urlImagemDois.equals("")){
                                        banco.removerImagem(urlImagemDois);
                                        urlImagemDois = urlImagemDoisNovo;
                                    }
                                    else{
                                        urlImagemDois = urlImagemDoisNovo;
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            //Toast.makeText(AnunciarActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao carregar imagem", Toast.LENGTH_SHORT);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Carregando imagem "+(int)progress+"%");
                        }
                    });
        }

        if(filePathImagemTres!=null && imagemTres==true) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Aguarde...");
            progressDialog.show();

            idImagemTres = UUID.randomUUID().toString();

            //final StorageReference ref = storageReference.child("images/"+ idImagemTres);

            imagem = banco.getImagem(idImagemTres);

            imagem.putFile(filePathImagemTres)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            imagem.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    urlImagemTresNovo = String.valueOf(uri);

                                    // Colocando a imagem novamente:

                                    Glide.with(AnunciarFotosImovelActivity.this)
                                            .load(urlImagemTresNovo)
                                            .into(ibImagemTres);

                                    progressDialog.dismiss();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem carregada com sucesso", Toast.LENGTH_SHORT);
                                    ibRemoverImagemTres.setVisibility(VISIBLE);

                                    // Removendo imagem anterior do storage (se existir):
                                    if(!urlImagemTres.equals("")){
                                        banco.removerImagem(urlImagemTres);
                                        urlImagemTres = urlImagemTresNovo;
                                    }
                                    else{
                                        urlImagemTres = urlImagemTresNovo;
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            //Toast.makeText(AnunciarActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao carregar imagem", Toast.LENGTH_SHORT);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Carregando imagem "+(int)progress+"%");
                        }
                    });
        }

        if(filePathImagemQuatro!=null && imagemQuatro==true) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Aguarde...");
            progressDialog.show();

            idImagemQuatro = UUID.randomUUID().toString();

            //final StorageReference ref = storageReference.child("images/"+ idImagemQuatro);

            imagem = banco.getImagem(idImagemQuatro);

            imagem.putFile(filePathImagemQuatro)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            imagem.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    urlImagemQuatroNovo = String.valueOf(uri);

                                    // Colocando a imagem novamente:

                                    Glide.with(AnunciarFotosImovelActivity.this)
                                            .load(urlImagemQuatroNovo)
                                            .into(ibImagemQuatro);

                                    progressDialog.dismiss();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem carregada com sucesso", Toast.LENGTH_SHORT);
                                    ibRemoverImagemQuatro.setVisibility(VISIBLE);

                                    // Removendo imagem anterior do storage (se existir):
                                    if(!urlImagemQuatro.equals("")){
                                        banco.removerImagem(urlImagemQuatro);
                                        urlImagemQuatro = urlImagemQuatroNovo;
                                    }
                                    else{
                                        urlImagemQuatro = urlImagemQuatroNovo;
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            //Toast.makeText(AnunciarActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao carregar imagem", Toast.LENGTH_SHORT);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Carregando imagem "+(int)progress+"%");
                        }
                    });
        }

        if(filePathImagemCinco!=null && imagemCinco==true) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Aguarde...");
            progressDialog.show();

            idImagemCinco = UUID.randomUUID().toString();

            //final StorageReference ref = storageReference.child("images/"+ idImagemCinco);

            imagem = banco.getImagem(idImagemCinco);

            imagem.putFile(filePathImagemCinco)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            imagem.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    urlImagemCincoNovo = String.valueOf(uri);

                                    // Colocando a imagem novamente:

                                    Glide.with(AnunciarFotosImovelActivity.this)
                                            .load(urlImagemCincoNovo)
                                            .into(ibImagemCinco);

                                    progressDialog.dismiss();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem carregada com sucesso", Toast.LENGTH_SHORT);
                                    ibRemoverImagemCinco.setVisibility(VISIBLE);

                                    // Removendo imagem anterior do storage (se existir):
                                    if(!urlImagemCinco.equals("")){
                                        banco.removerImagem(urlImagemCinco);
                                        urlImagemCinco = urlImagemCincoNovo;
                                    }
                                    else{
                                        urlImagemCinco = urlImagemCincoNovo;
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            //Toast.makeText(AnunciarActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao carregar imagem", Toast.LENGTH_SHORT);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Carregando imagem "+(int)progress+"%");
                        }
                    });
        }
    }

    private void acessarGaleria() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // Thread para aguardar o seletor de imagem abrir:

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Aguarde...");
        progressDialog.setMessage("Acessando galeria");
        progressDialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Log.i("MAIN", "acessando galeria...");
                        runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    progressDialog.dismiss();
                                    startActivityForResult(Intent.createChooser(intent, "Selecione a imagem"), idPicImage);
                                } catch (Exception e) {
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

    @Override
    public void onBackPressed() {

        Intent i = new Intent(AnunciarFotosImovelActivity.this, AnunciarDadosImovelActivity.class);

        i.putExtra("callerIntent", "AnunciarFotosImovelActivity");
        i.putExtra("Endereco", endereco);
        i.putExtra("Numero", numero);
        i.putExtra("Complemento", complemento);
        i.putExtra("CEP", cep);
        i.putExtra("Bairro", bairro);
        i.putExtra("Cidade", cidade);
        i.putExtra("Estado", estado);
        i.putExtra("Preco", preco);
        i.putExtra("Vagas", vagas);
        i.putExtra("Informacoes", informacoes);
        i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
        i.putExtra("urlImagemDois", urlImagemDois);
        i.putExtra("urlImagemTres", urlImagemTres);
        i.putExtra("urlImagemQuatro", urlImagemQuatro);
        i.putExtra("urlImagemCinco", urlImagemCinco);

        startActivity(i);
        Transition.backTransition(AnunciarFotosImovelActivity.this);
        finish();
    }

    public void habilitarSwipeMenu(){

        scrollView.setOnTouchListener(new OnSwipeTouchListener(AnunciarFotosImovelActivity.this) {
            public void onSwipeRight() {

                Intent i = new Intent(AnunciarFotosImovelActivity.this, MenuActivity.class);

                i.putExtra("callerIntent", "AnunciarFotosImovelActivity");

                i.putExtra("Endereco", endereco);
                i.putExtra("Numero", numero);
                i.putExtra("Complemento", complemento);
                i.putExtra("CEP", cep);
                i.putExtra("Bairro", bairro);
                i.putExtra("Cidade", cidade);
                i.putExtra("Estado", estado);
                i.putExtra("Preco", preco);
                i.putExtra("Vagas", vagas);
                i.putExtra("Informacoes", informacoes);
                i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                i.putExtra("urlImagemDois", urlImagemDois);
                i.putExtra("urlImagemTres", urlImagemTres);
                i.putExtra("urlImagemQuatro", urlImagemQuatro);
                i.putExtra("urlImagemCinco", urlImagemCinco);

                startActivity(i);
                Transition.enterTransition(AnunciarFotosImovelActivity.this);
                finish();
            }
        });
    }

    public void pesquisarLocalizacao(){

        // Pesquisando latitute e longitude:
        new InternetCheck(internet -> {

            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                try {
                    new Async().execute("https://nominatim.openstreetmap.org/search?q="+ URLEncoder.encode(enderecoPesquisa, "UTF-8")+"&format=json");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        Log.i("AQUI - APÓS Async", "LAT: " + latitude + " | LON: " + longitude);
    }

    public void selecionarImagemPrincipal(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                // Prevenindo clique duplo por um limite de 3 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                imagemPrincipal = true;
                imagemDois = imagemTres = imagemQuatro = imagemCinco = false;

                verificarPermissao();

                selecionarImagem(AnunciarFotosImovelActivity.this);
            }
        });
    }

    public void selecionarImagemDois(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                // Prevenindo clique duplo por um limite de 3 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                imagemDois = true;
                imagemPrincipal = imagemTres = imagemQuatro = imagemCinco = false;

                verificarPermissao();

                selecionarImagem(AnunciarFotosImovelActivity.this);
            }
        });
    }

    public void selecionarImagemTres(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                // Prevenindo clique duplo por um limite de 3 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                imagemTres = true;
                imagemPrincipal = imagemDois = imagemQuatro = imagemCinco = false;

                verificarPermissao();

                selecionarImagem(AnunciarFotosImovelActivity.this);
            }
        });
    }

    public void selecionarImagemQuatro(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                // Prevenindo clique duplo por um limite de 3 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                imagemQuatro = true;
                imagemPrincipal = imagemDois = imagemTres = imagemCinco = false;

                verificarPermissao();

                selecionarImagem(AnunciarFotosImovelActivity.this);
            }
        });
    }

    public void selecionarImagemCinco(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                // Prevenindo clique duplo por um limite de 3 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                imagemCinco = true;
                imagemPrincipal = imagemDois = imagemTres = imagemQuatro = false;

                verificarPermissao();

                selecionarImagem(AnunciarFotosImovelActivity.this);
            }
        });
    }

    private void selecionarImagem(Context context) {

        final CharSequence[] origem = {"Câmera", "Galeria"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Selecione a origem");

        builder.setItems(origem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (origem[item].equals("Câmera")) {
                    camera = true;
                    galeria = false;
                    tirarFoto();
                } else if (origem[item].equals("Galeria")) {
                    galeria = true;
                    camera = false;
                    acessarGaleria();
                }
            }
        });
        builder.show();
    }

    public void removerImagemDois(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                // Exibir tela para confirmar exclusão:

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View excluirImagem = li.inflate(R.layout.excluir_foto, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(excluirImagem);
                alertDialogBuilder.setTitle("Confirmação");

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Sim",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                        ibRemoverImagemDois.setVisibility(View.INVISIBLE);

                                        // Removendo imagem do storage:
                                        if(!urlImagemDois.equals("")){

                                            banco.removerImagem(urlImagemDois);
                                            urlImagemDois = "";
                                        }

                                        final ProgressDialog progressDialog = new ProgressDialog(context);
                                        progressDialog.setTitle("Aguarde...");
                                        progressDialog.setMessage("Removendo imagem");
                                        progressDialog.show();

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                Thread thread = new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Log.i("MAIN", "carregando anúncios...");
                                                        runOnUiThread( new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    progressDialog.dismiss();
                                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem removida com sucesso", Toast.LENGTH_SHORT);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    }
                                                };

                                                thread.start();
                                            }
                                        }, 2000);

                                        // Removendo prévia:

                                        Glide.with(AnunciarFotosImovelActivity.this)
                                                .load(urlImagemDois)
                                                .into(ibImagemDois);
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

    public void removerImagemTres(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                // Exibir tela para confirmar exclusão:

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View excluirImagem = li.inflate(R.layout.excluir_foto, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(excluirImagem);
                alertDialogBuilder.setTitle("Confirmação");

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Sim",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                        ibRemoverImagemTres.setVisibility(View.INVISIBLE);

                                        // Removendo imagem do storage:
                                        if(!urlImagemTres.equals("")){

                                            banco.removerImagem(urlImagemTres);
                                            urlImagemTres = "";
                                        }

                                        // Thread para aguardar as operações do banco serem realizadas:

                                        final ProgressDialog progressDialog = new ProgressDialog(context);
                                        progressDialog.setTitle("Aguarde...");
                                        progressDialog.setMessage("Removendo imagem");
                                        progressDialog.show();

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                Thread thread = new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Log.i("MAIN", "carregando anúncios...");
                                                        runOnUiThread( new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    progressDialog.dismiss();
                                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem removida com sucesso", Toast.LENGTH_SHORT);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    }
                                                };

                                                thread.start();
                                            }
                                        }, 2000);

                                        // Removendo prévia:

                                        Glide.with(AnunciarFotosImovelActivity.this)
                                                .load(urlImagemTres)
                                                .into(ibImagemTres);

                                        CustomToast.mostrarMensagem(getApplicationContext(), "Imagem removida com sucesso", Toast.LENGTH_SHORT);
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

    public void removerImagemQuatro(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                // Exibir tela para confirmar exclusão:

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View excluirImagem = li.inflate(R.layout.excluir_foto, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(excluirImagem);
                alertDialogBuilder.setTitle("Confirmação");

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Sim",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                        ibRemoverImagemQuatro.setVisibility(View.INVISIBLE);

                                        // Removendo imagem do storage:

                                        if(!urlImagemQuatro.equals("")){

                                            banco.removerImagem(urlImagemQuatro);
                                            urlImagemQuatro = "";
                                        }

                                        // Thread para aguardar as operações do banco serem realizadas:

                                        final ProgressDialog progressDialog = new ProgressDialog(context);
                                        progressDialog.setTitle("Aguarde...");
                                        progressDialog.setMessage("Removendo imagem");
                                        progressDialog.show();

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                Thread thread = new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Log.i("MAIN", "carregando anúncios...");
                                                        runOnUiThread( new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    progressDialog.dismiss();
                                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem removida com sucesso", Toast.LENGTH_SHORT);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    }
                                                };

                                                thread.start();
                                            }
                                        }, 2000);

                                        // Removendo prévia:

                                        Glide.with(AnunciarFotosImovelActivity.this)
                                                .load(urlImagemQuatro)
                                                .into(ibImagemQuatro);

                                        CustomToast.mostrarMensagem(getApplicationContext(), "Imagem removida com sucesso", Toast.LENGTH_SHORT);
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

    public void removerImagemCinco(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                // Exibir tela para confirmar exclusão:

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View excluirImagem = li.inflate(R.layout.excluir_foto, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(excluirImagem);
                alertDialogBuilder.setTitle("Confirmação");

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Sim",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                        ibRemoverImagemCinco.setVisibility(View.INVISIBLE);

                                        // Removendo imagem do storage:

                                        if(!urlImagemCinco.equals("")){

                                            banco.removerImagem(urlImagemCinco);
                                            urlImagemCinco = "";
                                        }

                                        // Thread para aguardar as operações do banco serem realizadas:

                                        final ProgressDialog progressDialog = new ProgressDialog(context);
                                        progressDialog.setTitle("Aguarde...");
                                        progressDialog.setMessage("Removendo imagem");
                                        progressDialog.show();

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                Thread thread = new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Log.i("MAIN", "carregando anúncios...");
                                                        runOnUiThread( new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    progressDialog.dismiss();
                                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem removida com sucesso", Toast.LENGTH_SHORT);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    }
                                                };

                                                thread.start();
                                            }
                                        }, 2000);

                                        // Removendo prévia:

                                        Glide.with(AnunciarFotosImovelActivity.this)
                                                .load(urlImagemCinco)
                                                .into(ibImagemCinco);

                                        //Toast.makeText(MeuAnuncioActivity.this, "Anúncio Excluído com Sucesso!", Toast.LENGTH_SHORT).show();
                                        CustomToast.mostrarMensagem(getApplicationContext(), "Imagem removida com sucesso", Toast.LENGTH_SHORT);
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

    public void criarAnuncio(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
                pbAnunciarFotosLayout.setVisibility(View.INVISIBLE);
            }
            else{
                // Validando input do usuário antes de criar o anúncio:
                if(urlImagemPrincipal.equals("") && urlImagemPrincipalNovo.equals("")){

                    configurarCamposInvalidosUI();
                    CustomToast.mostrarMensagem(getApplicationContext(), "Adicione a imagem principal", Toast.LENGTH_SHORT);
                }
                else{

                    pbAnunciarFotosLayout.setVisibility(VISIBLE);
                    configurarCamposInvalidosUI();
                    desabilitarUI();

                    // Prevenindo clique duplo por um limite de 3 segundos
                    if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                    ultimoClique = SystemClock.elapsedRealtime();

                    Anuncio anuncio = new Anuncio();

                    anuncio.setEndereco(endereco);
                    anuncio.setNumero(numero);
                    anuncio.setComplemento(complemento);
                    anuncio.setCep(cep);
                    anuncio.setBairro(bairro);
                    anuncio.setCidade(cidade);
                    anuncio.setEstado(estado);
                    anuncio.setPrecoAluguel(preco);
                    anuncio.setQtdVagas(vagas);
                    anuncio.setInformacoesAdicionais(informacoes);
                    anuncio.setKeyUsuario("");
                    //Imagens:
                    anuncio.setUrlImagemPrincipal(urlImagemPrincipal);
                    anuncio.setUrlImagemDois(urlImagemDois);
                    anuncio.setUrlImagemTres(urlImagemTres);
                    anuncio.setUrlImagemQuatro(urlImagemQuatro);
                    anuncio.setUrlImagemCinco(urlImagemCinco);
                    //Localização:
                    anuncio.setLatitude(latitude);
                    anuncio.setLongitude(longitude);

                    banco.adicionarAnuncio(sessao.getIdUsuario(), anuncio);

                    // Thread para aguardar o salvamento dos dados do anúncio no banco:
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    Log.i("MAIN", "criando anúncio...");
                                    runOnUiThread( new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                CustomToast.mostrarMensagem(getApplicationContext(), "Anúncio criado com sucesso", Toast.LENGTH_SHORT);
                                                startActivity(new Intent(AnunciarFotosImovelActivity.this, MeusAnunciosActivity.class));
                                                Transition.enterTransition(AnunciarFotosImovelActivity.this);
                                            } catch (Exception e) {
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
            }
        });
    }

    public void habilitarUI(){
        ibSelecionarImagemPrincipal.setEnabled(true);
        ibSelecionarImagemDois.setEnabled(true);
        ibSelecionarImagemTres.setEnabled(true);
        ibSelecionarImagemQuatro.setEnabled(true);
        ibSelecionarImagemCinco.setEnabled(true);

        ibRemoverImagemDois.setEnabled(true);
        ibRemoverImagemTres.setEnabled(true);
        ibRemoverImagemQuatro.setEnabled(true);
        ibRemoverImagemCinco.setEnabled(true);

        btnAnunciar.setEnabled(true);
    }

    // Durante a execução da Progress Bar:
    public void desabilitarUI(){
        ibSelecionarImagemPrincipal.setEnabled(false);
        ibSelecionarImagemDois.setEnabled(false);
        ibSelecionarImagemTres.setEnabled(false);
        ibSelecionarImagemQuatro.setEnabled(false);
        ibSelecionarImagemCinco.setEnabled(false);

        ibRemoverImagemDois.setEnabled(false);
        ibRemoverImagemTres.setEnabled(false);
        ibRemoverImagemQuatro.setEnabled(false);
        ibRemoverImagemCinco.setEnabled(false);

        btnAnunciar.setEnabled(false);
    }

    private void tirarFoto() {

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File filepath = Environment.getExternalStorageDirectory();
        File dir = new File(filepath.getAbsolutePath() + "/DCIM/");
        dir.mkdir();
        File file = new File(dir, "halugar_" + System.currentTimeMillis() + ".jpg");

        arquivoImagem = file.toString();

        Uri outuri = Uri.fromFile(file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);

        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // Origem == Câmera
        if(camera){
            if(imagemPrincipal){
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == 2) {
                        if (data != null) {

                            filePathImagemPrincipal = data.getData();
                            ibImagemPrincipal.setImageURI(filePathImagemPrincipal);

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(filePathImagemPrincipal.getPath());
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemPrincipal = Uri.fromFile(imagemComprimida);

                            uploadFile();
                        }

                        if (filePathImagemPrincipal == null && arquivoImagem != null) {

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(arquivoImagem);
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemPrincipal = Uri.fromFile(imagemComprimida);
                            ibImagemPrincipal.setImageURI(filePathImagemPrincipal);

                            uploadFile();
                        }
                    }
                }
            }

            if(imagemDois){
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == 2) {
                        if (data != null) {

                            filePathImagemDois = data.getData();
                            ibImagemDois.setImageURI(filePathImagemDois);

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(filePathImagemDois.getPath());
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemDois = Uri.fromFile(imagemComprimida);

                            uploadFile();
                        }

                        if (filePathImagemDois == null && arquivoImagem != null) {

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(arquivoImagem);
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemDois = Uri.fromFile(imagemComprimida);
                            ibImagemDois.setImageURI(filePathImagemDois);

                            uploadFile();
                        }
                    }
                }
            }

            if(imagemTres){
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == 2) {
                        if (data != null) {

                            filePathImagemTres = data.getData();
                            ibImagemTres.setImageURI(filePathImagemTres);

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(filePathImagemTres.getPath());
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemTres = Uri.fromFile(imagemComprimida);

                            uploadFile();
                        }

                        if (filePathImagemTres == null && arquivoImagem != null) {

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(arquivoImagem);
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemTres = Uri.fromFile(imagemComprimida);
                            ibImagemTres.setImageURI(filePathImagemTres);

                            uploadFile();
                        }
                    }
                }
            }

            if(imagemQuatro){
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == 2) {
                        if (data != null) {

                            filePathImagemQuatro = data.getData();
                            ibImagemQuatro.setImageURI(filePathImagemQuatro);

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(filePathImagemQuatro.getPath());
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemQuatro = Uri.fromFile(imagemComprimida);

                            uploadFile();
                        }

                        if (filePathImagemQuatro == null && arquivoImagem != null) {

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(arquivoImagem);
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemQuatro = Uri.fromFile(imagemComprimida);
                            ibImagemQuatro.setImageURI(filePathImagemQuatro);

                            uploadFile();
                        }
                    }
                }
            }

            if(imagemCinco){
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == 2) {
                        if (data != null) {

                            filePathImagemCinco = data.getData();
                            ibImagemCinco.setImageURI(filePathImagemCinco);

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(filePathImagemCinco.getPath());
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemCinco = Uri.fromFile(imagemComprimida);

                            uploadFile();
                        }
                        if (filePathImagemCinco == null && arquivoImagem != null) {

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(arquivoImagem);
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemCinco = Uri.fromFile(imagemComprimida);
                            ibImagemCinco.setImageURI(filePathImagemCinco);

                            uploadFile();
                        }
                    }
                }
            }
        }

        // Origem == Galeria
        if(galeria){

            if(requestCode==idPicImage && resultCode==RESULT_OK && data!=null && data.getData()!=null && imagemPrincipal) {
                filePathImagemPrincipal = data.getData();
                uploadFile();
            }

            if(requestCode==idPicImage && resultCode==RESULT_OK && data!=null && data.getData()!=null && imagemDois) {
                filePathImagemDois = data.getData();
                uploadFile();
            }

            if(requestCode==idPicImage && resultCode==RESULT_OK && data!=null && data.getData()!=null && imagemTres) {
                filePathImagemTres = data.getData();
                uploadFile();
            }

            if(requestCode==idPicImage && resultCode==RESULT_OK && data!=null && data.getData()!=null && imagemQuatro) {
                filePathImagemQuatro = data.getData();
                uploadFile();
            }

            if(requestCode==idPicImage && resultCode==RESULT_OK && data!=null && data.getData()!=null && imagemCinco) {
                filePathImagemCinco = data.getData();
                uploadFile();
            }
        }
    }

    public void verificarPermissao(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(AnunciarFotosImovelActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted... Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(AnunciarFotosImovelActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.i("PERMISSAO", "permissão não concedida");
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(AnunciarFotosImovelActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_STORAGE);

                Log.i("PERMISSAO", "permitir acesso?");
            }
        } else {
            Log.i("PERMISSAO", "já possui acesso");
        }
    }

    public void carregarExtras(){

        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent");

            if(callerIntent.equals("AnunciarDadosImovelActivity") || callerIntent.equals("MenuActivity")){

                endereco = extras.getString("Endereco");
                numero = extras.getInt("Numero");
                complemento = extras.getString("Complemento");
                cep = extras.getString("CEP");
                bairro = extras.getString("Bairro");
                cidade = extras.getString("Cidade");
                estado = extras.getString("Estado");
                preco = extras.getDouble("Preco");
                vagas = extras.getInt("Vagas");
                informacoes = extras.getString("Informacoes");
                urlImagemPrincipal = extras.getString("urlImagemPrincipal");
                urlImagemDois = extras.getString("urlImagemDois");
                urlImagemTres = extras.getString("urlImagemTres");
                urlImagemQuatro = extras.getString("urlImagemQuatro");
                urlImagemCinco = extras.getString("urlImagemCinco");

                // Removendo a sigla para criar endereco de pesquisa:
                estadoNome = estado.substring(0, estado.length()-3);

                // Configurando UI:

                Glide.with(AnunciarFotosImovelActivity.this)
                        .load(urlImagemPrincipal)
                        .into(ibImagemPrincipal);

                Glide.with(AnunciarFotosImovelActivity.this)
                        .load(urlImagemDois)
                        .into(ibImagemDois);

                Glide.with(AnunciarFotosImovelActivity.this)
                        .load(urlImagemTres)
                        .into(ibImagemTres);

                Glide.with(AnunciarFotosImovelActivity.this)
                        .load(urlImagemQuatro)
                        .into(ibImagemQuatro);

                Glide.with(AnunciarFotosImovelActivity.this)
                        .load(urlImagemCinco)
                        .into(ibImagemCinco);

                if(!urlImagemDois.equals("")){
                    ibRemoverImagemDois.setVisibility(VISIBLE);
                }

                if(!urlImagemTres.equals("")){
                    ibRemoverImagemTres.setVisibility(VISIBLE);
                }

                if(!urlImagemQuatro.equals("")){
                    ibRemoverImagemQuatro.setVisibility(VISIBLE);
                }

                if(!urlImagemCinco.equals("")){
                    ibRemoverImagemCinco.setVisibility(VISIBLE);
                }
            }
        }
    }

    public void configurarCamposInvalidosUI(){

        if(urlImagemPrincipal.equals("") && urlImagemPrincipalNovo.equals("")){
            labelImagemPrincipal.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            ibSelecionarImagemPrincipal.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_corner_invalid));
            ibImagemPrincipalBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_launcher_background_invalid));
        }
        else{
            labelImagemPrincipal.setTextColor(getResources().getColor(R.color.colorAccent));
            ibSelecionarImagemPrincipal.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_corner));
            ibImagemPrincipalBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_launcher_background));
        }
    }

    public void removerFotos(){

        if(!urlImagemPrincipal.equals("")){
            banco.removerImagem(urlImagemPrincipal);
        }

        if(!urlImagemDois.equals("")){
            banco.removerImagem(urlImagemDois);
        }

        if(!urlImagemTres.equals("")){
            banco.removerImagem(urlImagemTres);
        }

        if(!urlImagemQuatro.equals("")){
            banco.removerImagem(urlImagemQuatro);
        }

        if(!urlImagemCinco.equals("")){
            banco.removerImagem(urlImagemCinco);
        }
    }
}
