package br.ufc.crateus.halugar.Activities.MeusAnuncios;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;
import id.zelory.compressor.Compressor;

public class EditarMeuAnuncioFotos extends AppCompatActivity {

    ImageButton btnMenuEditarFotos, btnExcluirAnuncio;
    ImageButton ibEditarFotoPrincipal, ibEditarFotoDois, ibEditarFotoTres, ibEditarFotoQuatro, ibEditarFotoCinco;
    ImageButton ibRemoverFotoDois, ibRemoverFotoTres, ibRemoverFotoQuatro, ibRemoverFotoCinco;
    ImageButton ivFotoPrincipal, ivFotoDois, ivFotoTres, ivFotoQuatro, ivFotoCinco;

    Button btnSalvarFotos;

    String idAnuncio;

    final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 123;

    int idPicImage;
    String arquivoImagem;

    boolean camera, galeria;

    Banco banco;
    Sessao sessao;
    StorageReference imagem;

    ScrollView scrollView;
    ConstraintLayout pbEditarAnuncioFotosLayout, pbEditarAnuncioFotosLayoutRemover;

    private Uri filePathImagemPrincipalNovo, filePathImagemDoisNovo, filePathImagemTresNovo, filePathImagemQuatroNovo, filePathImagemCincoNovo;
    private String idImagemPrincipal, idImagemDois, idImagemTres, idImagemQuatro, idImagemCinco;
    private String urlImagemPrincipal="", urlImagemDois="", urlImagemTres="", urlImagemQuatro="", urlImagemCinco="";
    private String urlImagemPrincipalNovo, urlImagemDoisNovo, urlImagemTresNovo, urlImagemQuatroNovo, urlImagemCincoNovo;

    boolean imagemPrincipal, imagemDois, imagemTres, imagemQuatro, imagemCinco;
    boolean atualizadoPrincipal, atualizadoDois, atualizadoTres, atualizadoQuatro, atualizadoCinco;

    final Context context = this;

    long ultimoClique = 0;

    Bundle extras;
    String callerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_meu_anuncio_fotos);

        scrollView = (ScrollView)findViewById(R.id.scrollViewEditarFotos);

        ivFotoPrincipal = (ImageButton)findViewById(R.id.ivFotoPrincipalEditar);
        ivFotoDois = (ImageButton) findViewById(R.id.ivFotoDoisEditar);
        ivFotoTres = (ImageButton) findViewById(R.id.ivFotoTresEditar);
        ivFotoQuatro = (ImageButton) findViewById(R.id.ivFotoQuatroEditar);
        ivFotoCinco = (ImageButton) findViewById(R.id.ivFotoCincoEditar);

        ibEditarFotoPrincipal = (ImageButton)findViewById(R.id.ibEditarFotoPrincipal);
        ibEditarFotoDois = (ImageButton)findViewById(R.id.ibEditarFotoDois);
        ibEditarFotoTres = (ImageButton)findViewById(R.id.ibEditarFotoTres);
        ibEditarFotoQuatro = (ImageButton)findViewById(R.id.ibEditarFotoQuatro);
        ibEditarFotoCinco = (ImageButton)findViewById(R.id.ibEditarFotoCinco);

        ibRemoverFotoDois = (ImageButton)findViewById(R.id.ibRemoverFotoDois);
        ibRemoverFotoTres = (ImageButton)findViewById(R.id.ibRemoverFotoTres);
        ibRemoverFotoQuatro = (ImageButton)findViewById(R.id.ibRemoverFotoQuatro);
        ibRemoverFotoCinco = (ImageButton)findViewById(R.id.ibRemoverFotoCinco);

        btnMenuEditarFotos = (ImageButton) findViewById(R.id.btnMenuEditarFotos);
        btnSalvarFotos = (Button) findViewById(R.id.btnSalvarFotos);
        btnExcluirAnuncio = (ImageButton)findViewById(R.id.btnExcluirAnuncio);

        pbEditarAnuncioFotosLayout = (ConstraintLayout)findViewById(R.id.pbEditarAnuncioFotosLayout);
        pbEditarAnuncioFotosLayoutRemover = (ConstraintLayout)findViewById(R.id.pbEditarAnuncioFotosLayoutRemover);

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        habilitarSwipeMenu();
        carregarExtras();

        atualizadoPrincipal = atualizadoDois = atualizadoTres = atualizadoQuatro = atualizadoCinco = false;
        urlImagemPrincipalNovo = urlImagemDoisNovo = urlImagemTresNovo = urlImagemQuatroNovo = urlImagemCincoNovo = "";

        camera = galeria = false;

        ibEditarFotoPrincipal.setOnClickListener(view -> editarImagemPrincipal());

        ibEditarFotoDois.setOnClickListener(view -> editarImagemDois());

        ibEditarFotoTres.setOnClickListener(view -> editarImagemTres());

        ibEditarFotoQuatro.setOnClickListener(view -> editarImagemQuatro());

        ibEditarFotoCinco.setOnClickListener(view -> editarImagemCinco());

        ibRemoverFotoDois.setOnClickListener(view -> removerImagemDois());

        ibRemoverFotoTres.setOnClickListener(view -> removerImagemTres());

        ibRemoverFotoQuatro.setOnClickListener(view -> removerImagemQuatro());

        ibRemoverFotoCinco.setOnClickListener(view -> removerImagemCinco());

        btnSalvarFotos.setOnClickListener(view -> salvarImagens());

        btnMenuEditarFotos.setOnClickListener(view -> {
            Intent i = new Intent(EditarMeuAnuncioFotos.this, MenuActivity.class);

            i.putExtra("callerIntent", "EditarMeuAnuncioFotos");

            i.putExtra("idAnuncio", idAnuncio);

            i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
            i.putExtra("urlImagemDois", urlImagemDois);
            i.putExtra("urlImagemTres", urlImagemTres);
            i.putExtra("urlImagemQuatro", urlImagemQuatro);
            i.putExtra("urlImagemCinco", urlImagemCinco);

            i.putExtra("urlImagemPrincipalNovo", urlImagemPrincipalNovo);
            i.putExtra("urlImagemDoisNovo", urlImagemDoisNovo);
            i.putExtra("urlImagemTresNovo", urlImagemTresNovo);
            i.putExtra("urlImagemQuatroNovo", urlImagemQuatroNovo);
            i.putExtra("urlImagemCincoNovo", urlImagemCincoNovo);

            startActivity(i);
            Transition.enterTransition(EditarMeuAnuncioFotos.this);
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

    public void uploadFile() {

        if(filePathImagemPrincipalNovo!=null && imagemPrincipal) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Aguarde...");
            progressDialog.show();

            idImagemPrincipal = UUID.randomUUID().toString();

            Log.i("NOVO ID", idImagemPrincipal);

            imagem = banco.getImagem(idImagemPrincipal);

            //final StorageReference ref = storageReference.child("images/"+ idImagemPrincipal);

            imagem.putFile(filePathImagemPrincipalNovo)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            imagem.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    urlImagemPrincipalNovo = String.valueOf(uri);
                                    atualizadoPrincipal = true;

                                    // Colocando a imagem novamente:

                                    Glide.with(EditarMeuAnuncioFotos.this)
                                            .load(urlImagemPrincipalNovo)
                                            .into(ivFotoPrincipal);

                                    progressDialog.dismiss();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem carregada com sucesso", Toast.LENGTH_SHORT);

                                    ////////////////////////////////////////////////////////

                                    // Removendo anterior imagem do storage:

                                    if(!urlImagemPrincipal.equals("")){

                                        banco.removerImagem(urlImagemPrincipal);
                                        urlImagemPrincipal = urlImagemPrincipalNovo;
                                    }
                                    else{
                                        urlImagemPrincipal = urlImagemPrincipalNovo;
                                    }

                                    /////////////////////////////////////////////////////////
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

        if(filePathImagemDoisNovo!=null && imagemDois) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Aguarde...");
            progressDialog.show();

            idImagemDois = UUID.randomUUID().toString();

            imagem = banco.getImagem(idImagemDois);

            //final StorageReference ref = storageReference.child("images/"+ idImagemDois);

            imagem.putFile(filePathImagemDoisNovo)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            imagem.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    urlImagemDoisNovo = String.valueOf(uri);
                                    atualizadoDois = true;

                                    // Colocando a imagem novamente:

                                    Glide.with(EditarMeuAnuncioFotos.this)
                                            .load(urlImagemDoisNovo)
                                            .into(ivFotoDois);

                                    progressDialog.dismiss();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem carregada com sucesso", Toast.LENGTH_SHORT);
                                    ibRemoverFotoDois.setVisibility(View.VISIBLE);

                                    ////////////////////////////////////////////////////////

                                    // Removendo anterior imagem do storage:

                                    if(!urlImagemDois.equals("")){

                                        banco.removerImagem(urlImagemDois);
                                        urlImagemDois = urlImagemDoisNovo;
                                    }
                                    else{
                                        urlImagemDois = urlImagemDoisNovo; // A imagem dois não havia sido adicionada
                                    }

                                    /////////////////////////////////////////////////////////
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

        if(filePathImagemTresNovo!=null && imagemTres) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Aguarde...");
            progressDialog.show();

            idImagemTres = UUID.randomUUID().toString();

            imagem = banco.getImagem(idImagemTres);

            //final StorageReference ref = storageReference.child("images/"+ idImagemTres);

            imagem.putFile(filePathImagemTresNovo)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            imagem.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    urlImagemTresNovo = String.valueOf(uri);
                                    atualizadoTres = true;

                                    // Colocando a imagem novamente:

                                    Glide.with(EditarMeuAnuncioFotos.this)
                                            .load(urlImagemTresNovo)
                                            .into(ivFotoTres);

                                    progressDialog.dismiss();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem carregada com sucesso", Toast.LENGTH_SHORT);
                                    ibRemoverFotoTres.setVisibility(View.VISIBLE);

                                    ////////////////////////////////////////////////////////

                                    // Removendo anterior imagem do storage:

                                    if(!urlImagemTres.equals("")){

                                        banco.removerImagem(urlImagemTres);
                                        urlImagemTres = urlImagemTresNovo;
                                    }
                                    else{
                                        urlImagemTres = urlImagemTresNovo; // A imagem três não havia sido adicionada
                                    }

                                    /////////////////////////////////////////////////////////
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

        if(filePathImagemQuatroNovo!=null && imagemQuatro) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Aguarde...");
            progressDialog.show();

            idImagemQuatro = UUID.randomUUID().toString();

            imagem = banco.getImagem(idImagemQuatro);

            //final StorageReference ref = storageReference.child("images/"+ idImagemQuatro);

            imagem.putFile(filePathImagemQuatroNovo)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            imagem.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    urlImagemQuatroNovo = String.valueOf(uri);
                                    atualizadoQuatro = true;

                                    // Colocando a imagem novamente:

                                    Glide.with(EditarMeuAnuncioFotos.this)
                                            .load(urlImagemQuatroNovo)
                                            .into(ivFotoQuatro);

                                    progressDialog.dismiss();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem carregada com sucesso", Toast.LENGTH_SHORT);
                                    ibRemoverFotoQuatro.setVisibility(View.VISIBLE);

                                    ////////////////////////////////////////////////////////

                                    // Removendo anterior imagem do storage:

                                    if(!urlImagemQuatro.equals("")){

                                        banco.removerImagem(urlImagemQuatro);
                                        urlImagemQuatro = urlImagemQuatroNovo;
                                    }
                                    else{
                                        urlImagemQuatro = urlImagemQuatroNovo; // A imagem quatro não havia sido adicionada
                                    }

                                    /////////////////////////////////////////////////////////
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

        if(filePathImagemCincoNovo!=null && imagemCinco) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Aguarde...");
            progressDialog.show();

            idImagemCinco = UUID.randomUUID().toString();

            imagem = banco.getImagem(idImagemCinco);

            //final StorageReference ref = storageReference.child("images/"+ idImagemCinco);

            imagem.putFile(filePathImagemCincoNovo)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            imagem.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    urlImagemCincoNovo = String.valueOf(uri);
                                    atualizadoCinco = true;

                                    // Colocando a imagem novamente:

                                    Glide.with(EditarMeuAnuncioFotos.this)
                                            .load(urlImagemCincoNovo)
                                            .into(ivFotoCinco);

                                    progressDialog.dismiss();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Imagem carregada com sucesso", Toast.LENGTH_SHORT);
                                    ibRemoverFotoCinco.setVisibility(View.VISIBLE);

                                    ////////////////////////////////////////////////////////

                                    // Removendo anterior imagem do storage:

                                    if(!urlImagemCinco.equals("")){

                                        banco.removerImagem(urlImagemCinco);
                                        urlImagemCinco = urlImagemCincoNovo;
                                    }
                                    else{
                                        urlImagemCinco = urlImagemCincoNovo; // A imagem cinco não havia sido adicionada
                                    }

                                    /////////////////////////////////////////////////////////
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
                        Log.i("MAIN", "carregando anúncios...");
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

        /////////////////////////////////////////////////////////
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

                            filePathImagemPrincipalNovo = data.getData();
                            ivFotoPrincipal.setImageURI(filePathImagemPrincipalNovo);

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(filePathImagemPrincipalNovo.getPath());
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemPrincipalNovo = Uri.fromFile(imagemComprimida);

                            uploadFile();
                        }
                        if (filePathImagemPrincipalNovo == null && arquivoImagem != null) {

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(arquivoImagem);
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemPrincipalNovo = Uri.fromFile(imagemComprimida);
                            ivFotoPrincipal.setImageURI(filePathImagemPrincipalNovo);

                            uploadFile();
                        }
                    }
                }
            }

            if(imagemDois){
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == 2) {
                        if (data != null) {

                            filePathImagemDoisNovo = data.getData();
                            ivFotoDois.setImageURI(filePathImagemDoisNovo);

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(filePathImagemDoisNovo.getPath());
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemDoisNovo = Uri.fromFile(imagemComprimida);

                            uploadFile();
                        }
                        if (filePathImagemDoisNovo == null && arquivoImagem != null) {

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(arquivoImagem);
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemDoisNovo = Uri.fromFile(imagemComprimida);
                            ivFotoDois.setImageURI(filePathImagemDoisNovo);

                            uploadFile();
                        }
                    }
                }
            }

            if(imagemTres){
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == 2) {
                        if (data != null) {

                            filePathImagemTresNovo = data.getData();
                            ivFotoTres.setImageURI(filePathImagemTresNovo);

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(filePathImagemTresNovo.getPath());
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemTresNovo = Uri.fromFile(imagemComprimida);

                            uploadFile();
                        }
                        if (filePathImagemTresNovo == null && arquivoImagem != null) {

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(arquivoImagem);
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemTresNovo = Uri.fromFile(imagemComprimida);
                            ivFotoTres.setImageURI(filePathImagemTresNovo);

                            uploadFile();
                        }
                    }
                }
            }

            if(imagemQuatro){
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == 2) {
                        if (data != null) {

                            filePathImagemQuatroNovo = data.getData();
                            ivFotoQuatro.setImageURI(filePathImagemQuatroNovo);

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(filePathImagemQuatroNovo.getPath());
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemQuatroNovo = Uri.fromFile(imagemComprimida);

                            uploadFile();
                        }
                        if (filePathImagemQuatroNovo == null && arquivoImagem != null) {

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(arquivoImagem);
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemQuatroNovo = Uri.fromFile(imagemComprimida);
                            ivFotoQuatro.setImageURI(filePathImagemQuatroNovo);

                            uploadFile();
                        }
                    }
                }
            }

            if(imagemCinco){
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == 2) {
                        if (data != null) {

                            filePathImagemCincoNovo = data.getData();
                            ivFotoCinco.setImageURI(filePathImagemCincoNovo);

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(filePathImagemCincoNovo.getPath());
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemCincoNovo = Uri.fromFile(imagemComprimida);

                            uploadFile();
                        }
                        if (filePathImagemCincoNovo == null && arquivoImagem != null) {

                            // Comprimindo imagem antes de realizar upload:
                            File imagemParaComprimir = new File(arquivoImagem);
                            File imagemComprimida=null;

                            try {
                                imagemComprimida = new Compressor(this).compressToFile(imagemParaComprimir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            filePathImagemCincoNovo = Uri.fromFile(imagemComprimida);
                            ivFotoCinco.setImageURI(filePathImagemCincoNovo);

                            uploadFile();
                        }
                    }
                }
            }
        }

        // Origem == Galeria
        if(galeria){
            if(requestCode==idPicImage && resultCode==RESULT_OK && data!=null && data.getData()!=null && imagemPrincipal) {
                filePathImagemPrincipalNovo = data.getData();
                uploadFile();
            }

            if(requestCode==idPicImage && resultCode==RESULT_OK && data!=null && data.getData()!=null && imagemDois) {
                filePathImagemDoisNovo = data.getData();
                uploadFile();
            }

            if(requestCode==idPicImage && resultCode==RESULT_OK && data!=null && data.getData()!=null && imagemTres) {
                filePathImagemTresNovo = data.getData();
                uploadFile();
            }

            if(requestCode==idPicImage && resultCode==RESULT_OK && data!=null && data.getData()!=null && imagemQuatro) {
                filePathImagemQuatroNovo = data.getData();
                uploadFile();
            }

            if(requestCode==idPicImage && resultCode==RESULT_OK && data!=null && data.getData()!=null && imagemCinco) {
                filePathImagemCincoNovo = data.getData();
                uploadFile();
            }
        }
    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(EditarMeuAnuncioFotos.this, MeuAnuncioFotos.class);

        // Remover imagens (novas) adicionadas. mas não salvas...
        removerFotos();

        i.putExtra("callerIntent", "EditarMeuAnuncioFotos");

        i.putExtra("idAnuncio", idAnuncio);

        i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
        i.putExtra("urlImagemDois", urlImagemDois);
        i.putExtra("urlImagemTres", urlImagemTres);
        i.putExtra("urlImagemQuatro", urlImagemQuatro);
        i.putExtra("urlImagemCinco", urlImagemCinco);

        startActivity(i);
        Transition.enterTransition(EditarMeuAnuncioFotos.this);
        finish();
    }

    public void habilitarSwipeMenu(){

        scrollView.setOnTouchListener(new OnSwipeTouchListener(EditarMeuAnuncioFotos.this) {
            public void onSwipeRight() {
                //Toast.makeText(getApplicationContext(), "RIGHT", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(EditarMeuAnuncioFotos.this, MenuActivity.class));
                Intent i = new Intent(EditarMeuAnuncioFotos.this, MenuActivity.class);

                i.putExtra("callerIntent", "EditarMeuAnuncioFotos");

                i.putExtra("idAnuncio", idAnuncio);

                i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                i.putExtra("urlImagemDois", urlImagemDois);
                i.putExtra("urlImagemTres", urlImagemTres);
                i.putExtra("urlImagemQuatro", urlImagemQuatro);
                i.putExtra("urlImagemCinco", urlImagemCinco);

                i.putExtra("urlImagemPrincipalNovo", urlImagemPrincipalNovo);
                i.putExtra("urlImagemDoisNovo", urlImagemDoisNovo);
                i.putExtra("urlImagemTresNovo", urlImagemTresNovo);
                i.putExtra("urlImagemQuatroNovo", urlImagemQuatroNovo);
                i.putExtra("urlImagemCincoNovo", urlImagemCincoNovo);

                startActivity(i);
                Transition.enterTransition(EditarMeuAnuncioFotos.this);
                finish();
            }
        });
    }

    public void carregarExtras(){

        extras = getIntent().getExtras();

        // Recuperando dados do Bundle (MeuAnuncioFotos) ou MenuActivity:
        if(extras!=null){

            callerIntent = extras.getString("callerIntent");

            if(callerIntent.equals("MeuAnuncioFotos")){

                idAnuncio = extras.getString("idAnuncio");

                urlImagemPrincipal = extras.getString("urlImagemPrincipal");
                urlImagemDois = extras.getString("urlImagemDois");
                urlImagemTres = extras.getString("urlImagemTres");
                urlImagemQuatro = extras.getString("urlImagemQuatro");
                urlImagemCinco = extras.getString("urlImagemCinco");

                if(urlImagemDois.equals("")){
                    ibRemoverFotoDois.setVisibility(View.GONE);
                }

                if(urlImagemTres.equals("")){
                    ibRemoverFotoTres.setVisibility(View.GONE);
                }

                if(urlImagemQuatro.equals("")){
                    ibRemoverFotoQuatro.setVisibility(View.GONE);
                }

                if(urlImagemCinco.equals("")){
                    ibRemoverFotoCinco.setVisibility(View.GONE);
                }

                Glide.with(getApplicationContext())
                        .load(urlImagemPrincipal)
                        .into(ivFotoPrincipal);

                if(!urlImagemDois.equals("")){
                    Glide.with(getApplicationContext())
                            .load(urlImagemDois)
                            .into(ivFotoDois);
                }

                if(!urlImagemTres.equals("")){
                    Glide.with(getApplicationContext())
                            .load(urlImagemTres)
                            .into(ivFotoTres);
                }

                if(!urlImagemQuatro.equals("")){
                    Glide.with(getApplicationContext())
                            .load(urlImagemQuatro)
                            .into(ivFotoQuatro);
                }

                if(!urlImagemCinco.equals("")){
                    Glide.with(getApplicationContext())
                            .load(urlImagemCinco)
                            .into(ivFotoCinco);
                }
            }
        }
    }

    public void editarImagemPrincipal(){
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

                selecionarImagem(EditarMeuAnuncioFotos.this);
            }
        });
    }

    public void editarImagemDois(){
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

                selecionarImagem(EditarMeuAnuncioFotos.this);
            }
        });
    }

    public void editarImagemTres(){
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

                selecionarImagem(EditarMeuAnuncioFotos.this);
            }
        });
    }

    public void editarImagemQuatro(){
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

                selecionarImagem(EditarMeuAnuncioFotos.this);
            }
        });
    }

    public void editarImagemCinco(){
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

                selecionarImagem(EditarMeuAnuncioFotos.this);
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

                                        banco.setImagemDois(idAnuncio, "");
                                        atualizadoDois = true;

                                        ////////////////////////////////////////////////////////

                                        // Removendo imagem do storage:

                                        if(!urlImagemDois.equals("")){

                                            banco.removerImagem(urlImagemDois);
                                            urlImagemDois = "";
                                        }

                                                /*
                                                if(!urlImagemDoisNovo.equals("")){

                                                    banco.removerImagem(urlImagemDoisNovo);
                                                }

                                                 */

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

                                        /////////////////////////////////////////////////////////

                                        //urlImagemDoisNovo = "";

                                        ibRemoverFotoDois.setVisibility(View.INVISIBLE);

                                                /*
                                                Glide.with(EditarMeuAnuncioFotos.this)
                                                        .load(urlImagemDoisNovo)
                                                        .into(ivFotoDois);

                                                 */

                                        Glide.with(EditarMeuAnuncioFotos.this)
                                                .load(urlImagemDois)
                                                .into(ivFotoDois);

                                        //Toast.makeText(MeuAnuncioActivity.this, "Anúncio Excluído com Sucesso!", Toast.LENGTH_SHORT).show();
                                        //CustomToast.mostrarMensagem(getApplicationContext(), "Imagem removida com sucesso", Toast.LENGTH_SHORT);
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

                                        banco.setImagemTres(idAnuncio, "");
                                        atualizadoTres = true;

                                        ////////////////////////////////////////////////////////

                                        // Removendo imagem do storage:

                                        if(!urlImagemTres.equals("")){

                                            banco.removerImagem(urlImagemTres);
                                            urlImagemTres = "";
                                        }

                                                /*
                                                if(!urlImagemTresNovo.equals("")){

                                                    banco.removerImagem(urlImagemTresNovo);
                                                }

                                                 */

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

                                        /////////////////////////////////////////////////////////

                                        //urlImagemTresNovo = "";

                                        ibRemoverFotoTres.setVisibility(View.INVISIBLE);

                                                /*
                                                Glide.with(EditarMeuAnuncioFotos.this)
                                                        .load(urlImagemTresNovo)
                                                        .into(ivFotoTres);

                                                 */

                                        Glide.with(EditarMeuAnuncioFotos.this)
                                                .load(urlImagemTres)
                                                .into(ivFotoTres);

                                        //Toast.makeText(MeuAnuncioActivity.this, "Anúncio Excluído com Sucesso!", Toast.LENGTH_SHORT).show();
                                        //CustomToast.mostrarMensagem(getApplicationContext(), "Imagem removida com sucesso", Toast.LENGTH_SHORT);
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

                                        banco.setImagemQuatro(idAnuncio, "");
                                        atualizadoQuatro = true;

                                        ////////////////////////////////////////////////////////

                                        // Removendo imagem do storage:

                                        if(!urlImagemQuatro.equals("")){

                                            banco.removerImagem(urlImagemQuatro);
                                            urlImagemQuatro = "";
                                        }

                                                /*
                                                if(!urlImagemQuatroNovo.equals("")){

                                                    banco.removerImagem(urlImagemQuatroNovo);
                                                }

                                                 */

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

                                        /////////////////////////////////////////////////////////

                                        urlImagemQuatroNovo = "";

                                        ibRemoverFotoQuatro.setVisibility(View.INVISIBLE);

                                                /*
                                                Glide.with(EditarMeuAnuncioFotos.this)
                                                        .load(urlImagemQuatroNovo)
                                                        .into(ivFotoQuatro);

                                                 */

                                        Glide.with(EditarMeuAnuncioFotos.this)
                                                .load(urlImagemQuatro)
                                                .into(ivFotoQuatro);

                                        //Toast.makeText(MeuAnuncioActivity.this, "Anúncio Excluído com Sucesso!", Toast.LENGTH_SHORT).show();
                                        //CustomToast.mostrarMensagem(getApplicationContext(), "Imagem removida com sucesso", Toast.LENGTH_SHORT);
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

                                        banco.setImagemCinco(idAnuncio, "");
                                        atualizadoCinco = true;

                                        ////////////////////////////////////////////////////////

                                        // Removendo imagem do storage:

                                        if(!urlImagemCinco.equals("")){

                                            banco.removerImagem(urlImagemCinco);
                                            urlImagemCinco = "";
                                        }

                                                /*
                                                if(!urlImagemCincoNovo.equals("")){

                                                    banco.removerImagem(urlImagemCincoNovo);
                                                }

                                                 */

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

                                        /////////////////////////////////////////////////////////

                                        //urlImagemCincoNovo = "";

                                        ibRemoverFotoCinco.setVisibility(View.INVISIBLE);

                                                /*
                                                Glide.with(EditarMeuAnuncioFotos.this)
                                                        .load(urlImagemCincoNovo)
                                                        .into(ivFotoCinco);

                                                 */

                                        Glide.with(EditarMeuAnuncioFotos.this)
                                                .load(urlImagemCinco)
                                                .into(ivFotoCinco);

                                        //Toast.makeText(MeuAnuncioActivity.this, "Anúncio Excluído com Sucesso!", Toast.LENGTH_SHORT).show();
                                        //CustomToast.mostrarMensagem(getApplicationContext(), "Imagem removida com sucesso", Toast.LENGTH_SHORT);
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

    public void salvarImagens(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                desabilitarUI();

                // Prevenindo clique duplo por um limite de 3 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                if (atualizadoPrincipal) {

                    //banco.setImagemPrincipal(idAnuncio, urlImagemPrincipalNovo);
                    banco.setImagemPrincipal(idAnuncio, urlImagemPrincipal);
                }

                if (atualizadoDois) {

                    //banco.setImagemDois(idAnuncio, urlImagemDoisNovo);
                    banco.setImagemDois(idAnuncio, urlImagemDois);
                }

                if (atualizadoTres) {

                    //banco.setImagemTres(idAnuncio, urlImagemTresNovo);
                    banco.setImagemTres(idAnuncio, urlImagemTres);
                }

                if (atualizadoQuatro) {

                    //banco.setImagemQuatro(idAnuncio, urlImagemQuatroNovo);
                    banco.setImagemQuatro(idAnuncio, urlImagemQuatro);
                }

                if (atualizadoCinco) {

                    //banco.setImagemCinco(idAnuncio, urlImagemCincoNovo);
                    banco.setImagemCinco(idAnuncio, urlImagemCinco);
                }

                if (atualizadoPrincipal || atualizadoDois || atualizadoTres || atualizadoQuatro || atualizadoCinco) {

                    pbEditarAnuncioFotosLayout.setVisibility(View.VISIBLE);

                    // Thread para aguardar o salvamento das alterações:
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    Log.i("MAIN", "salvando alterações...");
                                    runOnUiThread( new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                CustomToast.mostrarMensagem(getApplicationContext(), "Alterações salvas com sucesso", Toast.LENGTH_SHORT);
                                                startActivity(new Intent(EditarMeuAnuncioFotos.this, MeusAnunciosActivity.class));
                                                Transition.enterTransition(EditarMeuAnuncioFotos.this);
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
                else {
                    CustomToast.mostrarMensagem(getApplicationContext(), "Não houveram alterações", Toast.LENGTH_SHORT);
                }
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

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Sim",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                        pbEditarAnuncioFotosLayoutRemover.setVisibility(View.VISIBLE);
                                        desabilitarUI();

                                        banco.removerAnuncio(sessao.getIdUsuario(), idAnuncio);

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

                                                                    startActivity(new Intent(EditarMeuAnuncioFotos.this, MeusAnunciosActivity.class));
                                                                    Transition.enterTransition(EditarMeuAnuncioFotos.this);
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
        ibEditarFotoPrincipal.setEnabled(true);
        ibEditarFotoDois.setEnabled(true);
        ibEditarFotoTres.setEnabled(true);
        ibEditarFotoQuatro.setEnabled(true);
        ibEditarFotoCinco.setEnabled(true);

        ibRemoverFotoDois.setEnabled(true);
        ibRemoverFotoTres.setEnabled(true);
        ibRemoverFotoQuatro.setEnabled(true);
        ibRemoverFotoCinco.setEnabled(true);
    }

    // Durante a execução da Progress Bar:
    public void desabilitarUI(){
        ibEditarFotoPrincipal.setEnabled(false);
        ibEditarFotoDois.setEnabled(false);
        ibEditarFotoTres.setEnabled(false);
        ibEditarFotoQuatro.setEnabled(false);
        ibEditarFotoCinco.setEnabled(false);

        ibRemoverFotoDois.setEnabled(false);
        ibRemoverFotoTres.setEnabled(false);
        ibRemoverFotoQuatro.setEnabled(false);
        ibRemoverFotoCinco.setEnabled(false);
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

    public void verificarPermissao(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(EditarMeuAnuncioFotos.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(EditarMeuAnuncioFotos.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.i("PERMISSAO", "permissão não concedida");
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(EditarMeuAnuncioFotos.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_STORAGE);

                Log.i("PERMISSAO", "permitir acesso?");
            }
        } else {
            Log.i("PERMISSAO", "já possui acesso");
        }
    }

    public void removerFotos(){

        if(!urlImagemPrincipalNovo.equals("")){
            banco.removerImagem(urlImagemPrincipalNovo);
        }

        if(!urlImagemDoisNovo.equals("")){
            banco.removerImagem(urlImagemDoisNovo);
        }

        if(!urlImagemTresNovo.equals("")){
            banco.removerImagem(urlImagemTresNovo);
        }

        if(!urlImagemQuatroNovo.equals("")){
            banco.removerImagem(urlImagemQuatroNovo);
        }

        if(!urlImagemCincoNovo.equals("")){
            banco.removerImagem(urlImagemCincoNovo);
        }
    }
}
