package br.ufc.crateus.halugar.Activities.MeuCadastro;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import br.ufc.crateus.halugar.Activities.MeusAnuncios.EditarMeuAnuncioDados;
import br.ufc.crateus.halugar.Activities.main.MainActivity;
import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.Model.Anuncio;
import br.ufc.crateus.halugar.Model.Usuario;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.Formatacao;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;
import br.ufc.crateus.halugar.Util.Validacao;

public class EditarCadastroActivity extends AppCompatActivity {

    ImageButton btnExcluirContaEditar, btnMenuEditarCadastro;
    EditText etNomeCompletoAtualizar, etTelefoneAtualizar, etSenhaAtual, etNovaSenha;
    Button btnAtualizarDados;
    LoginButton btnSignInFacebookValidation;
    CheckBox cbSenhaAtual, cbNovaSenha;
    TextView labelNome, labelTelefone, labelAtualizarSenha, labelSenhaAtual, labelNovaSenha;

    boolean nomeInvalido, telefoneInvalido, senhaAtualInvalida, senhaAtualNaoDigitada, novaSenhaInvalida, novaSenhaNaoDigitada, senhasIguais;
    boolean atualizarNome, atualizarTelefone, atualizarSenhaAtual, atualizarSenhaNova, senhaTamanhoInvalido;

    View viewMenu;
    final Context context = this;

    String nomeAtual="", telefoneAtual="", senhaAtual="", novaSenha="";
    String anuncioKey, usuarioKey;

    ConstraintLayout pbEditarCadastroLayout;
    ConstraintLayout pbEditarCadastroLayoutRemover;

    long ultimoClique = 0;

    Banco banco;
    Sessao sessao;

    boolean googleProvider, facebookProvider, emailProvider;

    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;

    final int RC_SIGN_IN = 9001;

    CallbackManager mCallbackManager;

    boolean possuiAnuncio;

    SharedPreferences credentials;

    Bundle extras;
    String nome, idUsuario, callerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_cadastro);

        etNomeCompletoAtualizar = (EditText)findViewById(R.id.etNomeCompletoAtualizar);
        etTelefoneAtualizar = (EditText)findViewById(R.id.etTelefoneAtualizar);
        etSenhaAtual = (EditText)findViewById(R.id.etSenhaAtual);
        etNovaSenha = (EditText)findViewById(R.id.etNovaSenha);
        btnAtualizarDados = (Button)findViewById(R.id.btnAtualizarDados);
        cbSenhaAtual = (CheckBox)findViewById(R.id.cbSenhaAtual);
        cbNovaSenha = (CheckBox)findViewById(R.id.cbNovaSenha);
        btnMenuEditarCadastro = (ImageButton)findViewById(R.id.btnMenuEditarCadastro);
        btnExcluirContaEditar = (ImageButton)findViewById(R.id.btnExcluirContaEditar);

        labelNome = (TextView)findViewById(R.id.labelNome);
        labelTelefone = (TextView)findViewById(R.id.labelTelefone);
        labelAtualizarSenha = (TextView)findViewById(R.id.labelAtualizarSenha);
        labelSenhaAtual = (TextView)findViewById(R.id.labelSenhaAtual);
        labelNovaSenha = (TextView)findViewById(R.id.labelNovaSenha);

        btnSignInFacebookValidation = (LoginButton)findViewById(R.id.btnSignInFacebookValidation);

        viewMenu = findViewById(android.R.id.content).getRootView();

        pbEditarCadastroLayout = (ConstraintLayout)findViewById(R.id.pbEditarCadastroLayout);
        pbEditarCadastroLayoutRemover = (ConstraintLayout)findViewById(R.id.pbEditarCadastroLayoutRemover);

        etNomeCompletoAtualizar.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        credentials = getSharedPreferences("credentials", MODE_PRIVATE);

        idUsuario = credentials.getString("userId", "-");

        googleProvider = facebookProvider = emailProvider = false;

        possuiAnuncio = false;

        habilitarSwipeMenu();

        verificarProvedor();
        configurarUI();

        carregarExtras();
        carregarInformacoes();

        btnExcluirContaEditar.setOnClickListener(view -> removerConta());

        btnAtualizarDados.setOnClickListener(view -> atualizarDados());

        cbSenhaAtual.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // checkbox status is changed from uncheck to checked.
            if (!isChecked) {
                // hide password
                etSenhaAtual.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                // show password
                etSenhaAtual.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });

        cbNovaSenha.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // checkbox status is changed from uncheck to checked.
            if (!isChecked) {
                // hide password
                etNovaSenha.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                // show password
                etNovaSenha.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });

        btnMenuEditarCadastro.setOnClickListener(view -> {

            Intent i = new Intent(EditarCadastroActivity.this, MenuActivity.class);

            i.putExtra("callerIntent", "EditarCadastroActivity");

            i.putExtra("Nome", etNomeCompletoAtualizar.getText().toString());
            i.putExtra("Telefone", etTelefoneAtualizar.getText().toString());
            i.putExtra("SenhaAtual", etSenhaAtual.getText().toString());
            i.putExtra("NovaSenha", etNovaSenha.getText().toString());

            startActivity(i);
            Transition.enterTransition(EditarCadastroActivity.this);
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
    public void onBackPressed() {

        if(callerIntent.equals("MeuCadastroActivity")){

            Intent i = new Intent(EditarCadastroActivity.this, MeuCadastroActivity.class);

            i.putExtra("callerIntent", "EditarCadastroActivity");

            startActivity(i);
            Transition.backTransition(EditarCadastroActivity.this);
            finish();
        }
    }

    public void habilitarSwipeMenu(){
        viewMenu.setOnTouchListener(new OnSwipeTouchListener(EditarCadastroActivity.this) {
            public void onSwipeRight() {

                Intent i = new Intent(EditarCadastroActivity.this, MenuActivity.class);

                i.putExtra("callerIntent", "EditarCadastroActivity");

                i.putExtra("Nome", etNomeCompletoAtualizar.getText().toString());
                i.putExtra("Telefone", etTelefoneAtualizar.getText().toString());
                i.putExtra("SenhaAtual", etSenhaAtual.getText().toString());
                i.putExtra("NovaSenha", etNovaSenha.getText().toString());

                startActivity(i);
                Transition.enterTransition(EditarCadastroActivity.this);
                finish();
            }
        });
    }

    public void carregarInformacoes(){

        banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot usuario : dataSnapshot.getChildren()){
                    if (idUsuario.equals(usuario.getValue(Usuario.class).getuId())) {

                        usuarioKey = usuario.getKey();

                        nomeAtual = String.valueOf(usuario.getValue(Usuario.class).getNomeCompleto());
                        telefoneAtual = String.valueOf(usuario.getValue(Usuario.class).getTelefone());

                        // Exibição dos dados atuais do usuário:
                        etNomeCompletoAtualizar.setHint(nomeAtual);

                        if(telefoneAtual.equals("")){
                            etTelefoneAtualizar.setHint("Não informado");
                        }
                        else{
                            //etTelefoneAtualizar.setHint(Formatacao.formatarTelefone(telefoneAtual));
                            etTelefoneAtualizar.setText(telefoneAtual);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void removerConta(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                if(googleProvider || facebookProvider){
                    // Exibir tela para confirmar exclusão:

                    // get prompts.xml view
                    LayoutInflater li = LayoutInflater.from(context);
                    View removerConta = li.inflate(R.layout.excluir_conta, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(removerConta);
                    alertDialogBuilder.setTitle("Confirmação");

                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("Sim",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {

                                            pbEditarCadastroLayoutRemover.setVisibility(View.VISIBLE);
                                            desabilitarUI();

                                            if(googleProvider){
                                                // Configure Google Sign In
                                                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                        .requestIdToken(getString(R.string.default_web_client_id))
                                                        .requestEmail()
                                                        .build();

                                                mGoogleSignInClient = GoogleSignIn.getClient(EditarCadastroActivity.this, gso);

                                                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                                                startActivityForResult(signInIntent, RC_SIGN_IN);
                                            }

                                            if(facebookProvider){

                                                Sessao.logout();

                                                FacebookSdk.sdkInitialize(getApplicationContext());

                                                // Initialize Facebook Login button
                                                mCallbackManager = CallbackManager.Factory.create();

                                                // Simulando autenticação de login na conta:
                                                LoginButton loginButton = findViewById(R.id.btnSignInFacebookValidation);
                                                loginButton.setReadPermissions("email", "public_profile");

                                                btnSignInFacebookValidation.performClick();

                                                loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                                                    @Override
                                                    public void onSuccess(LoginResult loginResult) {
                                                        Log.d("CONTA", "facebook:onSuccess:" + loginResult);
                                                        handleFacebookAccessToken(loginResult.getAccessToken());
                                                    }

                                                    @Override
                                                    public void onCancel() {
                                                        Log.d("CONTA", "facebook:onCancel");
                                                    }

                                                    @Override
                                                    public void onError(FacebookException error) {
                                                        Log.d("CONTA", "facebook:onError", error);
                                                    }
                                                });
                                            }
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

                if(emailProvider){
                    // get prompts.xml view
                    LayoutInflater li = LayoutInflater.from(context);
                    View removerConta = li.inflate(R.layout.excluir_conta, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(removerConta);
                    alertDialogBuilder.setTitle("Confirmação");

                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("Sim",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            pbEditarCadastroLayoutRemover.setVisibility(View.VISIBLE);
                                            desabilitarUI();

                                            // Realizando autenticação do usuário:
                                            AuthCredential credential = EmailAuthProvider.getCredential(sessao.getEmailUsuario(), credentials.getString("userPassword", "-"));

                                            // Prompt the user to re-provide their sign-in credentials
                                            sessao.getUsuario().reauthenticate(credential)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            // Se a autenticação deu certo...
                                                            if (task.isSuccessful()) {
                                                                // Removendo anúncios cadastrados pelo usuário da tabela anuncio e suas respectivas imagens:
                                                                banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        for(DataSnapshot usuario : dataSnapshot.getChildren()){

                                                                            Log.i("CONTA", "excluindo anuncios do usuario @ anuncio");

                                                                            if (idUsuario.equals(usuario.getValue(Usuario.class).getuId())) {

                                                                                usuarioKey = usuario.getKey(); // Para realizar removeção das tabelas

                                                                                // Percorrendo tabela anuncio e removendo ocorrências:
                                                                                banco.getTabelaAnuncio().addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                        for (DataSnapshot anuncio : dataSnapshot.getChildren()){
                                                                                            if(usuarioKey.equals(anuncio.getValue(Anuncio.class).getKeyUsuario())){

                                                                                                anuncioKey = anuncio.getKey();

                                                                                                possuiAnuncio = true;

                                                                                                String urlImagemPrincipal, urlImagemDois, urlImagemTres, urlImagemQuatro, urlImagemCinco;

                                                                                                urlImagemPrincipal = anuncio.getValue(Anuncio.class).getUrlImagemPrincipal();
                                                                                                urlImagemDois = anuncio.getValue(Anuncio.class).getUrlImagemDois();
                                                                                                urlImagemTres = anuncio.getValue(Anuncio.class).getUrlImagemTres();
                                                                                                urlImagemQuatro = anuncio.getValue(Anuncio.class).getUrlImagemQuatro();
                                                                                                urlImagemCinco = anuncio.getValue(Anuncio.class).getUrlImagemCinco();

                                                                                                // Remover imagens referentes ao anúncio do Storage:
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

                                                                                                // Removendo o registro do anúncio da tabela anuncio:
                                                                                                banco.getTabelaAnuncio().child(anuncioKey).removeValue();

                                                                                                // Thread para aguardar as operações do banco serem executadas:
                                                                                                Handler handler = new Handler();
                                                                                                handler.postDelayed(new Runnable() {
                                                                                                    public void run() {
                                                                                                        Thread thread = new Thread() {
                                                                                                            @Override
                                                                                                            public void run() {
                                                                                                                runOnUiThread( new Runnable() {
                                                                                                                    @Override
                                                                                                                    public void run() {
                                                                                                                        // Removendo usuário (Authentication) - Só pode executar após os outros processos:
                                                                                                                        sessao.getUsuario().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if (task.isSuccessful()) {

                                                                                                                                    Log.i("CONTA", "usuário removido com sucesso @ authentication");

                                                                                                                                    // Removendo credenciais do usuário do Shared Preferences:
                                                                                                                                    context.getSharedPreferences("credentials", 0).edit().clear().apply();

                                                                                                                                    // Remoção concluída com sucesso...
                                                                                                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Conta removida com sucesso", Toast.LENGTH_SHORT);

                                                                                                                                    Sessao.logout();

                                                                                                                                    startActivity(new Intent(EditarCadastroActivity.this, MainActivity.class));
                                                                                                                                    Transition.enterTransition(EditarCadastroActivity.this);
                                                                                                                                    finish();
                                                                                                                                }
                                                                                                                                else{

                                                                                                                                    Log.i("CONTA", "falha ao remover usuário @ authentication");
                                                                                                                                    //Toast.makeText(getApplicationContext(), "Falha ao excluir conta!", Toast.LENGTH_LONG).show();
                                                                                                                                    pbEditarCadastroLayoutRemover.setVisibility(View.INVISIBLE);
                                                                                                                                    habilitarUI();
                                                                                                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao remover conta", Toast.LENGTH_SHORT);
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                                    }
                                                                                                                });
                                                                                                            }
                                                                                                        };

                                                                                                        thread.start();
                                                                                                    }
                                                                                                }, 5000);
                                                                                            }
                                                                                        }

                                                                                        // Usuário não possui anúncio cadastrado, apenas remover do authentication:
                                                                                        if(!possuiAnuncio){
                                                                                            Log.i("CADASTRO", "Usuário não possui anúncios cadastrados...");
                                                                                            // Thread para aguardar as operações do banco serem executadas:
                                                                                            Handler handler = new Handler();
                                                                                            handler.postDelayed(new Runnable() {
                                                                                                public void run() {
                                                                                                    Thread thread = new Thread() {
                                                                                                        @Override
                                                                                                        public void run() {
                                                                                                            runOnUiThread( new Runnable() {
                                                                                                                @Override
                                                                                                                public void run() {
                                                                                                                    // Removendo usuário (Authentication) - Só pode executar após os outros processos:
                                                                                                                    sessao.getUsuario().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                            if (task.isSuccessful()) {

                                                                                                                                Log.i("CONTA", "usuário removido com sucesso @ authentication");

                                                                                                                                // Removendo credenciais do usuário do Shared Preferences:
                                                                                                                                context.getSharedPreferences("credentials", 0).edit().clear().apply();

                                                                                                                                // Remoção concluída com sucesso...
                                                                                                                                CustomToast.mostrarMensagem(getApplicationContext(), "Conta removida com sucesso", Toast.LENGTH_SHORT);

                                                                                                                                Sessao.logout();

                                                                                                                                startActivity(new Intent(EditarCadastroActivity.this, MainActivity.class));
                                                                                                                                Transition.enterTransition(EditarCadastroActivity.this);
                                                                                                                                finish();
                                                                                                                            }
                                                                                                                            else{

                                                                                                                                Log.i("CONTA", "falha ao remover usuário @ authentication");
                                                                                                                                //Toast.makeText(getApplicationContext(), "Falha ao excluir conta!", Toast.LENGTH_LONG).show();
                                                                                                                                pbEditarCadastroLayoutRemover.setVisibility(View.INVISIBLE);
                                                                                                                                habilitarUI();
                                                                                                                                CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao remover conta", Toast.LENGTH_SHORT);
                                                                                                                            }
                                                                                                                        }
                                                                                                                    });
                                                                                                                }
                                                                                                            });
                                                                                                        }
                                                                                                    };

                                                                                                    thread.start();
                                                                                                }
                                                                                            }, 3000);
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

                                                                // Removendo usuário da tabela usuario:
                                                                banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        for(DataSnapshot usuario : dataSnapshot.getChildren()){
                                                                            if (idUsuario.equals(usuario.getValue(Usuario.class).getuId())) {

                                                                                Log.i("CONTA", "excluindo usuario @ usuario");

                                                                                usuarioKey = usuario.getKey();
                                                                                banco.getTabelaUsuario().child(usuarioKey).removeValue();
                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            }
                                                            else{

                                                                Log.i("CONTA", "Falha na reautenticação");
                                                                pbEditarCadastroLayoutRemover.setVisibility(View.INVISIBLE);
                                                                habilitarUI();
                                                                CustomToast.mostrarMensagem(EditarCadastroActivity.this, "Senha incorreta", Toast.LENGTH_SHORT);
                                                            }
                                                        }
                                                    });
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
            }
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
            // Signed in successfully.
            Log.w("CONTA", "signInResult:successfull");
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("CONTA", "signInResult:failed code=" + e.getStatusCode());
            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao remover conta", Toast.LENGTH_SHORT);

            pbEditarCadastroLayoutRemover.setVisibility(View.INVISIBLE);
            habilitarUI();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("CONTA", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        sessao.getAutenticacaoUsuario().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Removendo anúncios cadastrados pelo usuário da tabela anuncio e suas respectivas imagens:
                            banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot usuario : dataSnapshot.getChildren()){

                                        Log.i("CONTA", "excluindo anuncios do usuario @ anuncio");

                                        if (idUsuario.equals(usuario.getValue(Usuario.class).getuId())) {

                                            usuarioKey = usuario.getKey(); // Para realizar removeção das tabelas

                                            // Percorrendo tabela anuncio e removendo ocorrências:
                                            banco.getTabelaAnuncio().addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot anuncio : dataSnapshot.getChildren()){
                                                        if(usuarioKey.equals(anuncio.getValue(Anuncio.class).getKeyUsuario())){

                                                            anuncioKey = anuncio.getKey();

                                                            possuiAnuncio = true;

                                                            String urlImagemPrincipal, urlImagemDois, urlImagemTres, urlImagemQuatro, urlImagemCinco;

                                                            urlImagemPrincipal = anuncio.getValue(Anuncio.class).getUrlImagemPrincipal();
                                                            urlImagemDois = anuncio.getValue(Anuncio.class).getUrlImagemDois();
                                                            urlImagemTres = anuncio.getValue(Anuncio.class).getUrlImagemTres();
                                                            urlImagemQuatro = anuncio.getValue(Anuncio.class).getUrlImagemQuatro();
                                                            urlImagemCinco = anuncio.getValue(Anuncio.class).getUrlImagemCinco();

                                                            // Remover imagens referentes ao anúncio do Storage:
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

                                                            // Removendo o registro do anúncio da tabela anuncio:
                                                            banco.getTabelaAnuncio().child(anuncioKey).removeValue();

                                                            // Thread para aguardar as operações do database/storage serem executadas:
                                                            Handler handler = new Handler();
                                                            handler.postDelayed(new Runnable() {
                                                                public void run() {
                                                                    Thread thread = new Thread() {
                                                                        @Override
                                                                        public void run() {
                                                                            runOnUiThread( new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    // Removendo usuário (Authentication)
                                                                                    // Só pode executar após os outros processos de remoção, pois o usuário perde a autenticação:
                                                                                    sessao.getUsuario().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {

                                                                                                Log.i("CONTA", "usuário removido com sucesso @ authentication");

                                                                                                // Removendo credenciais do usuário do Shared Preferences:
                                                                                                context.getSharedPreferences("credentials", 0).edit().clear().apply();

                                                                                                // Remoção concluída com sucesso...
                                                                                                CustomToast.mostrarMensagem(getApplicationContext(), "Conta removida com sucesso", Toast.LENGTH_SHORT);

                                                                                                Sessao.logout();

                                                                                                startActivity(new Intent(EditarCadastroActivity.this, MainActivity.class));
                                                                                                Transition.enterTransition(EditarCadastroActivity.this);
                                                                                                finish();
                                                                                            }
                                                                                            else{

                                                                                                Log.i("CONTA", "falha ao remover usuário @ authentication");
                                                                                                //Toast.makeText(getApplicationContext(), "Falha ao excluir conta!", Toast.LENGTH_LONG).show();
                                                                                                pbEditarCadastroLayoutRemover.setVisibility(View.INVISIBLE);
                                                                                                habilitarUI();
                                                                                                CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao remover conta", Toast.LENGTH_SHORT);
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                        }
                                                                    };

                                                                    thread.start();
                                                                }
                                                            }, 5000);
                                                        }
                                                    }

                                                    // Caso o usuário não tenha anúncio cadastrado, apenas remover do authentication:
                                                    if(!possuiAnuncio){
                                                        Log.i("CADASTRO", "usuário não tem anúncios cadastrados...");

                                                        // Thread para aguardar as operações do database/storage serem executadas:
                                                        Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            public void run() {
                                                                Thread thread = new Thread() {
                                                                    @Override
                                                                    public void run() {
                                                                        runOnUiThread( new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                // Removendo usuário (Authentication)
                                                                                // Só pode executar após os outros processos de remoção, pois o usuário perde a autenticação:
                                                                                sessao.getUsuario().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {

                                                                                            Log.i("CADASTRO", "usuário removido com sucesso @ authentication");

                                                                                            // Removendo credenciais do usuário do Shared Preferences:
                                                                                            context.getSharedPreferences("credentials", 0).edit().clear().apply();

                                                                                            // Remoção concluída com sucesso...
                                                                                            CustomToast.mostrarMensagem(getApplicationContext(), "Conta removida com sucesso", Toast.LENGTH_SHORT);
                                                                                            Sessao.logout();
                                                                                            startActivity(new Intent(EditarCadastroActivity.this, MainActivity.class));
                                                                                            Transition.enterTransition(EditarCadastroActivity.this);
                                                                                            finish();
                                                                                        }
                                                                                        else{
                                                                                            Log.i("CADASTRO", "falha ao remover usuário @ authentication");
                                                                                            //Toast.makeText(getApplicationContext(), "Falha ao excluir conta!", Toast.LENGTH_LONG).show();
                                                                                            pbEditarCadastroLayoutRemover.setVisibility(View.INVISIBLE);
                                                                                            habilitarUI();
                                                                                            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao remover conta", Toast.LENGTH_SHORT);
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                };

                                                                thread.start();
                                                            }
                                                        }, 3000);
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

                            // Removendo usuário da tabela usuario:
                            banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot usuario : dataSnapshot.getChildren()){
                                        if (idUsuario.equals(usuario.getValue(Usuario.class).getuId())) {

                                            Log.i("CONTA", "excluindo usuario @ usuario");

                                            usuarioKey = usuario.getKey();
                                            banco.getTabelaUsuario().child(usuarioKey).removeValue();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("CONTA", "signInWithCredential:failure", task.getException());
                            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao remover conta", Toast.LENGTH_SHORT);
                            pbEditarCadastroLayoutRemover.setVisibility(View.INVISIBLE);
                            habilitarUI();
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("CONTA", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        sessao.getAutenticacaoUsuario().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("CADASTRO", "signInWithCredential:success");

                            // Removendo anúncios cadastrados pelo usuário da tabela anuncio e suas respectivas imagens:
                            banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot usuario : dataSnapshot.getChildren()){

                                        Log.i("CADASTRO", "excluindo anuncios do usuario @ anuncio");

                                        if (idUsuario.equals(usuario.getValue(Usuario.class).getuId())) {

                                            usuarioKey = usuario.getKey(); // Para realizar removeção das tabelas

                                            // Percorrendo tabela anuncio e removendo ocorrências (se existirem):
                                            banco.getTabelaAnuncio().addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot anuncio : dataSnapshot.getChildren()){
                                                        if(usuarioKey.equals(anuncio.getValue(Anuncio.class).getKeyUsuario())){

                                                            anuncioKey = anuncio.getKey();

                                                            possuiAnuncio = true;

                                                            String urlImagemPrincipal, urlImagemDois, urlImagemTres, urlImagemQuatro, urlImagemCinco;

                                                            urlImagemPrincipal = anuncio.getValue(Anuncio.class).getUrlImagemPrincipal();
                                                            urlImagemDois = anuncio.getValue(Anuncio.class).getUrlImagemDois();
                                                            urlImagemTres = anuncio.getValue(Anuncio.class).getUrlImagemTres();
                                                            urlImagemQuatro = anuncio.getValue(Anuncio.class).getUrlImagemQuatro();
                                                            urlImagemCinco = anuncio.getValue(Anuncio.class).getUrlImagemCinco();

                                                            // Remover imagens referentes ao anúncio do Storage:
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

                                                            // Removendo o registro do anúncio da tabela anuncio:
                                                            banco.getTabelaAnuncio().child(anuncioKey).removeValue();

                                                            // Thread para aguardar as operações do database/storage serem executadas:
                                                            Handler handler = new Handler();
                                                            handler.postDelayed(new Runnable() {
                                                                public void run() {
                                                                    Thread thread = new Thread() {
                                                                        @Override
                                                                        public void run() {
                                                                            runOnUiThread( new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    // Removendo usuário (Authentication)
                                                                                    // Só pode executar após os outros processos de remoção, pois o usuário perde a autenticação:
                                                                                    sessao.getUsuario().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {

                                                                                                Log.i("CADASTRO", "usuário removido com sucesso @ authentication");

                                                                                                // Removendo credenciais do usuário do Shared Preferences:
                                                                                                context.getSharedPreferences("credentials", 0).edit().clear().apply();

                                                                                                // Remoção concluída com sucesso...
                                                                                                CustomToast.mostrarMensagem(getApplicationContext(), "Conta removida com sucesso", Toast.LENGTH_SHORT);

                                                                                                Sessao.logout();

                                                                                                startActivity(new Intent(EditarCadastroActivity.this, MainActivity.class));
                                                                                                Transition.enterTransition(EditarCadastroActivity.this);
                                                                                                finish();
                                                                                            }
                                                                                            else{
                                                                                                Log.i("CADASTRO", "falha ao remover usuário @ authentication");
                                                                                                //Toast.makeText(getApplicationContext(), "Falha ao excluir conta!", Toast.LENGTH_LONG).show();
                                                                                                pbEditarCadastroLayoutRemover.setVisibility(View.INVISIBLE);
                                                                                                habilitarUI();
                                                                                                CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao remover conta", Toast.LENGTH_SHORT);
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                        }
                                                                    };

                                                                    thread.start();
                                                                }
                                                            }, 5000);
                                                        }
                                                    }

                                                    // Caso o usuário não tenha anúncio cadastrado, apenas remover do authentication:
                                                    if(!possuiAnuncio){
                                                        Log.i("CADASTRO", "usuário não tem anúncios cadastrados...");

                                                        // Thread para aguardar as operações do database/storage serem executadas:
                                                        Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            public void run() {
                                                                Thread thread = new Thread() {
                                                                    @Override
                                                                    public void run() {
                                                                        runOnUiThread( new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                // Removendo usuário (Authentication)
                                                                                // Só pode executar após os outros processos de remoção, pois o usuário perde a autenticação:
                                                                                sessao.getUsuario().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {

                                                                                            Log.i("CADASTRO", "usuário removido com sucesso @ authentication");

                                                                                            // Removendo credenciais do usuário do Shared Preferences:
                                                                                            context.getSharedPreferences("credentials", 0).edit().clear().apply();

                                                                                            // Remoção concluída com sucesso...
                                                                                            CustomToast.mostrarMensagem(getApplicationContext(), "Conta removida com sucesso", Toast.LENGTH_SHORT);

                                                                                            Sessao.logout();

                                                                                            startActivity(new Intent(EditarCadastroActivity.this, MainActivity.class));
                                                                                            Transition.enterTransition(EditarCadastroActivity.this);
                                                                                            finish();
                                                                                        }
                                                                                        else{
                                                                                            Log.i("CADASTRO", "falha ao remover usuário @ authentication");
                                                                                            //Toast.makeText(getApplicationContext(), "Falha ao excluir conta!", Toast.LENGTH_LONG).show();
                                                                                            pbEditarCadastroLayoutRemover.setVisibility(View.INVISIBLE);
                                                                                            habilitarUI();
                                                                                            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao remover conta", Toast.LENGTH_SHORT);
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                };

                                                                thread.start();
                                                            }
                                                        }, 3000);
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

                            // Removendo usuário da tabela usuario:
                            banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot usuario : dataSnapshot.getChildren()){
                                        if (idUsuario.equals(usuario.getValue(Usuario.class).getuId())) {

                                            Log.i("CADASTRO", "excluindo usuario @ usuario");

                                            usuarioKey = usuario.getKey();
                                            banco.getTabelaUsuario().child(usuarioKey).removeValue();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                        else {

                            // If sign in fails, display a message to the user.
                            Log.w("CADASTRO", "signInWithCredential:failure", task.getException());
                            pbEditarCadastroLayoutRemover.setVisibility(View.INVISIBLE);
                            habilitarUI();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao remover conta", Toast.LENGTH_SHORT);
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(googleProvider){
            // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                // The Task returned from this call is always completed, no need to attach a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
        }

        if(facebookProvider){
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void atualizarDados(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                nomeInvalido = telefoneInvalido = senhaAtualInvalida = senhaAtualNaoDigitada = novaSenhaInvalida = novaSenhaNaoDigitada = senhasIguais = false;
                atualizarNome = atualizarTelefone = atualizarSenhaAtual = atualizarSenhaNova = senhaTamanhoInvalido = false;

                String novoNome = etNomeCompletoAtualizar.getText().toString().trim().replaceAll(" +", " ");
                String novoTelefone = etTelefoneAtualizar.getText().toString().trim().replaceAll(" +", " ");
                String senhaAtual = etSenhaAtual.getText().toString();
                String novaSenha = etNovaSenha.getText().toString();

                // Verificando quais os campos foram preenchidos:
                if(!novoNome.equals("")){
                    atualizarNome = true;
                }

                if(novoTelefone.equals("") || !novoTelefone.equals("")){
                    atualizarTelefone = true;
                }

                if(!senhaAtual.equals("")){
                    atualizarSenhaAtual = true;
                }

                if(!novaSenha.equals("")){
                    atualizarSenhaNova = true;
                }

                if(!atualizarNome && !atualizarTelefone && !atualizarSenhaAtual && !atualizarSenhaNova){
                    configurarCamposInvalidosUI();
                    CustomToast.mostrarMensagem(getApplicationContext(), "Não houveram alterações", Toast.LENGTH_SHORT);
                    return;
                }

                System.out.println("VALIDANDO - CAMPOS");
                Log.i("VALIDANDO", "Atualizar nome = " + atualizarNome);
                Log.i("VALIDANDO", "Atualizar telefone = " + atualizarTelefone);
                Log.i("VALIDANDO", "Atualizar senha atual = " + atualizarSenhaAtual);
                Log.i("VALIDANDO", "Atualizar nova senha = " + atualizarSenhaNova);

                // Validando as combinações de campos possíveis:

                String msgDadosInvalidos = "• Preencha os campos corretamente:\n";

                // Atualizar o NOME
                if(atualizarNome && !atualizarTelefone && !atualizarSenhaAtual && !atualizarSenhaNova){

                    if(Validacao.validarNome(novoNome)==false){
                        nomeInvalido = true;
                    }

                    if(nomeInvalido){
                        configurarCamposInvalidosUI();
                        CustomToast.mostrarMensagem(getApplicationContext(), "Preencha os campos corretamente", Toast.LENGTH_SHORT);
                    }
                    else{

                        configurarCamposInvalidosUI();
                        desabilitarUI();

                        // Prevenindo clique duplo por um limite de 3 segundos
                        if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                        ultimoClique = SystemClock.elapsedRealtime();

                        pbEditarCadastroLayout.setVisibility(View.VISIBLE);

                        banco.setNomeCompleto(usuarioKey, novoNome);

                        // Thread para aguardar o salvamento das alterações no banco:

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                Thread thread = new Thread() {
                                    @Override
                                    public void run() {
                                        Log.i("EDITAR CADASTRO", "salvando alterações...");
                                        runOnUiThread( new Runnable() {
                                            @Override
                                            public void run() {
                                                try {

                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Alterações salvas com sucesso", Toast.LENGTH_SHORT);

                                                    startActivity(new Intent(EditarCadastroActivity.this, MeuCadastroActivity.class));
                                                    Transition.enterTransition(EditarCadastroActivity.this);
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
                }
                else
                // Atualizar TELEFONE
                if(!atualizarNome && atualizarTelefone && !atualizarSenhaAtual && !atualizarSenhaNova){

                    // Validar campos:
                    if(!novoTelefone.equals("") && (novoTelefone.length()!=11 && novoTelefone.length()!=10) || ((novoTelefone.length()==11 || novoTelefone.length()==10) && Validacao.validarTelefone(novoTelefone)==false)){
                        telefoneInvalido = true;
                    }

                    if(telefoneInvalido){
                        configurarCamposInvalidosUI();
                        CustomToast.mostrarMensagem(getApplicationContext(), "Preencha os campos corretamente", Toast.LENGTH_SHORT);
                    }
                    else{

                        configurarCamposInvalidosUI();
                        desabilitarUI();

                        // Prevenindo clique duplo por um limite de 3 segundos
                        if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                        ultimoClique = SystemClock.elapsedRealtime();

                        pbEditarCadastroLayout.setVisibility(View.VISIBLE);

                        banco.setTelefone(usuarioKey, novoTelefone);

                        // Thread para aguardar o salvamento das alterações no banco:

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                Thread thread = new Thread() {
                                    @Override
                                    public void run() {
                                        Log.i("EDITAR CADASTRO", "salvando alterações...");
                                        runOnUiThread( new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Alterações salvas com sucesso", Toast.LENGTH_SHORT);
                                                    startActivity(new Intent(EditarCadastroActivity.this, MeuCadastroActivity.class));
                                                    Transition.enterTransition(EditarCadastroActivity.this);
                                                    finish();
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
                else
                // Atualizar SENHA (Solicitar nova senha)
                if(!atualizarNome && !atualizarTelefone && atualizarSenhaAtual && !atualizarSenhaNova){
                    novaSenhaNaoDigitada = true;
                    configurarCamposInvalidosUI();
                    CustomToast.mostrarMensagem(getApplicationContext(), "Digite a nova senha", Toast.LENGTH_SHORT);
                }
                else
                // Atualizar SENHA (Solicitar senha atual)
                if(!atualizarNome && !atualizarTelefone && !atualizarSenhaAtual && atualizarSenhaNova){

                    senhaAtualNaoDigitada = true;
                    configurarCamposInvalidosUI();
                    CustomToast.mostrarMensagem(getApplicationContext(), "Digite a senha atual", Toast.LENGTH_SHORT);
                }
                else
                //Atualizar SENHA (ambas as senhas digitadas)
                if(!atualizarNome && !atualizarTelefone && atualizarSenhaAtual && atualizarSenhaNova){

                    if((!senhaAtual.equals("") && senhaAtual.length()<6) || (!novaSenha.equals("") && novaSenha.length()<6)){
                        senhaAtualInvalida = novaSenhaInvalida = true;
                        senhaTamanhoInvalido = true;
                    }

                    if(!senhaTamanhoInvalido){
                        if(senhaAtual.equals(novaSenha)){
                            senhasIguais = true;
                        }
                    }

                    if(senhaAtualInvalida || novaSenhaInvalida || senhasIguais){

                        if(senhaAtualInvalida || novaSenhaInvalida){
                            configurarCamposInvalidosUI();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Preencha os campos corretamente", Toast.LENGTH_SHORT);
                        }
                        else{
                            configurarCamposInvalidosUI();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Senhas iguais", Toast.LENGTH_SHORT);
                        }
                    }
                    else{

                        desabilitarUI();
                        configurarCamposInvalidosUI();
                        pbEditarCadastroLayout.setVisibility(View.VISIBLE);

                        // Prevenindo clique duplo por um limite de 3 segundos
                        if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                        ultimoClique = SystemClock.elapsedRealtime();

                        AuthCredential credential = EmailAuthProvider.getCredential(sessao.getEmailUsuario(), senhaAtual);

                        // Prompt the user to re-provide their sign-in credentials
                        sessao.getUsuario().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    sessao.getUsuario().updatePassword(novaSenha).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                // Thread para adicionar um delay após o processo de reautenticação:
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    public void run() {
                                                        Thread thread = new Thread() {
                                                            @Override
                                                            public void run() {
                                                                Log.i("MAIN", "atualizando senha...");
                                                                runOnUiThread( new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            CustomToast.mostrarMensagem(getApplicationContext(), "Alterações salvas com sucesso", Toast.LENGTH_SHORT);
                                                                            startActivity(new Intent(EditarCadastroActivity.this, MeuCadastroActivity.class));
                                                                            Transition.enterTransition(EditarCadastroActivity.this);
                                                                            finish();
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
                                                //Toast.makeText(getApplicationContext(), "Falha ao atualizar dados!", Toast.LENGTH_LONG).show();
                                                CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao salvar alterações", Toast.LENGTH_SHORT);
                                                pbEditarCadastroLayout.setVisibility(View.INVISIBLE);
                                                Log.d("Atualizando", "Error password not updated");
                                                habilitarUI();
                                            }
                                        }
                                    });

                                } else {
                                    //Toast.makeText(getApplicationContext(), "Senha atual inválida!", Toast.LENGTH_LONG).show();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Senha atual incorreta", Toast.LENGTH_SHORT);
                                    pbEditarCadastroLayout.setVisibility(View.INVISIBLE);
                                    //alteracoes = true;
                                    Log.d("Atualizando", "Error auth failed");
                                    habilitarUI();
                                }
                            }
                        });
                    }
                }
                else
                // Atualizar NOME e TELEFONE:
                if(atualizarNome && atualizarTelefone && !atualizarSenhaAtual && !atualizarSenhaNova){

                    if(Validacao.validarNome(novoNome)==false){
                        nomeInvalido = true;
                    }

                    if(!novoTelefone.equals("") && (novoTelefone.length()!=11 && novoTelefone.length()!=10) || ((novoTelefone.length()==11 || novoTelefone.length()==10) && Validacao.validarTelefone(novoTelefone)==false)){
                        telefoneInvalido = true;
                    }

                    if(nomeInvalido || telefoneInvalido){

                        configurarCamposInvalidosUI();
                        CustomToast.mostrarMensagem(getApplicationContext(), "Preencha os campos corretamente", Toast.LENGTH_SHORT);
                    }
                    else{

                        desabilitarUI();
                        configurarCamposInvalidosUI();
                        pbEditarCadastroLayout.setVisibility(View.VISIBLE);

                        // Prevenindo clique duplo por um limite de 3 segundos
                        if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                        ultimoClique = SystemClock.elapsedRealtime();

                        //banco.getTabelaUsuario().child(usuarioKey).child("nomeCompleto").setValue(novoNome);
                        banco.setNomeCompleto(usuarioKey, novoNome);
                        //banco.getTabelaUsuario().child(usuarioKey).child("telefone").setValue(novoTelefone);
                        banco.setTelefone(usuarioKey, novoTelefone);

                        // Thread para aguardar o salvamento das alterações no banco:

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                Thread thread = new Thread() {
                                    @Override
                                    public void run() {
                                        Log.i("EDITAR CADASTRO", "salvando alterações...");
                                        runOnUiThread( new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Alterações salvas com sucesso", Toast.LENGTH_SHORT);
                                                    startActivity(new Intent(EditarCadastroActivity.this, MeuCadastroActivity.class));
                                                    Transition.enterTransition(EditarCadastroActivity.this);
                                                    finish();
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
                else
                // Atualizar NOME e SENHA (digitar nova senha):
                if(atualizarNome && !atualizarTelefone && atualizarSenhaAtual && !atualizarSenhaNova){

                    if(Validacao.validarNome(novoNome)==false){
                       nomeInvalido = true;
                    }

                    novaSenhaNaoDigitada = true;

                    configurarCamposInvalidosUI();
                    CustomToast.mostrarMensagem(getApplicationContext(), "Digite a nova senha", Toast.LENGTH_SHORT);
                }
                else
                // Atualizar NOME e SENHA (digitar senha atual):
                if(atualizarNome && !atualizarTelefone && !atualizarSenhaAtual && atualizarSenhaNova){

                    if(Validacao.validarNome(novoNome)==false){
                        nomeInvalido = true;
                    }

                    senhaAtualNaoDigitada = true;

                    configurarCamposInvalidosUI();
                    CustomToast.mostrarMensagem(getApplicationContext(), "Digite a senha atual", Toast.LENGTH_SHORT);
                }
                else
                // Atualizar NOME e SENHA (ambas digitadas):
                if(atualizarNome && !atualizarTelefone && atualizarSenhaAtual && atualizarSenhaNova){

                    if(Validacao.validarNome(novoNome)==false){
                        nomeInvalido = true;
                    }

                    if((!senhaAtual.equals("") && senhaAtual.length()<6) || (!novaSenha.equals("") && novaSenha.length()<6)){
                        senhaAtualInvalida = novaSenhaInvalida = true;
                        senhaTamanhoInvalido = true;
                    }

                    if(!senhaTamanhoInvalido){
                        if(senhaAtual.equals(novaSenha)){
                            senhasIguais = true;
                        }
                    }

                    if(senhaAtualInvalida || novaSenhaInvalida || senhasIguais || nomeInvalido){

                        if(senhaAtualInvalida || novaSenhaInvalida || nomeInvalido){
                            configurarCamposInvalidosUI();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Preencha os campos corretamente", Toast.LENGTH_SHORT);
                        }

                        if(senhasIguais){
                            configurarCamposInvalidosUI();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Senhas iguais", Toast.LENGTH_SHORT);
                        }
                    }
                    else{

                        pbEditarCadastroLayout.setVisibility(View.VISIBLE);
                        configurarCamposInvalidosUI();
                        desabilitarUI();

                        // Prevenindo clique duplo por um limite de 3 segundos
                        if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                        ultimoClique = SystemClock.elapsedRealtime();

                        // Atualizar senha:
                        AuthCredential credential = EmailAuthProvider.getCredential(sessao.getEmailUsuario(), senhaAtual);

                        // Prompt the user to re-provide their sign-in credentials
                        sessao.getUsuario().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    sessao.getUsuario().updatePassword(novaSenha).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                banco.setNomeCompleto(usuarioKey, novoNome);

                                                // Thread para adicionar um delay após o processo de reautenticação:
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    public void run() {
                                                        Thread thread = new Thread() {
                                                            @Override
                                                            public void run() {
                                                                Log.i("MAIN", "atualizando senha...");
                                                                runOnUiThread( new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            CustomToast.mostrarMensagem(getApplicationContext(), "Alterações salvas com sucesso", Toast.LENGTH_SHORT);
                                                                            startActivity(new Intent(EditarCadastroActivity.this, MeuCadastroActivity.class));
                                                                            Transition.enterTransition(EditarCadastroActivity.this);
                                                                            finish();
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
                                                //Toast.makeText(getApplicationContext(), "Falha ao atualizar dados!", Toast.LENGTH_LONG).show();
                                                CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao salvar alterações", Toast.LENGTH_SHORT);
                                                pbEditarCadastroLayout.setVisibility(View.INVISIBLE);
                                                Log.d("Atualizando", "Error password not updated");
                                                habilitarUI();
                                            }
                                        }
                                    });

                                } else {
                                    //Toast.makeText(getApplicationContext(), "Senha atual inválida!", Toast.LENGTH_LONG).show();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Senha atual incorreta", Toast.LENGTH_SHORT);
                                    pbEditarCadastroLayout.setVisibility(View.INVISIBLE);
                                    //alteracoes = true;
                                    Log.d("Atualizando", "Error auth failed");
                                    habilitarUI();
                                }
                            }
                        });
                    }
                }
                else
                // Atualizar TELEFONE e SENHA (digitar nova senha):
                if(!atualizarNome && atualizarTelefone && atualizarSenhaAtual && !atualizarSenhaNova){

                    Log.i("VALIDANDO", "atualizar telefone + digitar nova senha...");

                    if(!novoTelefone.equals("") && (novoTelefone.length()!=11 && novoTelefone.length()!=10) || ((novoTelefone.length()==11 || novoTelefone.length()==10) && Validacao.validarTelefone(novoTelefone)==false)){
                        telefoneInvalido = true;
                    }

                    novaSenhaNaoDigitada = true;

                    configurarCamposInvalidosUI();
                    CustomToast.mostrarMensagem(getApplicationContext(), "Digite a nova senha", Toast.LENGTH_SHORT);
                }
                else
                // Atualizar TELEFONE e SENHA (digitar senha atual):
                if(!atualizarNome && atualizarTelefone && !atualizarSenhaAtual && atualizarSenhaNova){

                    Log.i("VALIDANDO", "atualizar telefone + digitar senha atual...");

                    if(!novoTelefone.equals("") && (novoTelefone.length()!=11 && novoTelefone.length()!=10) || ((novoTelefone.length()==11 || novoTelefone.length()==10) && Validacao.validarTelefone(novoTelefone)==false)){
                        telefoneInvalido = true;
                    }

                    senhaAtualNaoDigitada = true;

                    configurarCamposInvalidosUI();
                    CustomToast.mostrarMensagem(getApplicationContext(), "Digite a senha atual", Toast.LENGTH_SHORT);
                }
                else
                // Atualizar TELEFONE e SENHA (ambas digitadas):
                if(!atualizarNome && atualizarTelefone && atualizarSenhaAtual && atualizarSenhaNova){

                    Log.i("VALIDANDO", "atualizar telefone + ambas as senhas digitadas...");

                    if(!novoTelefone.equals("") && (novoTelefone.length()!=11 && novoTelefone.length()!=10) || ((novoTelefone.length()==11 || novoTelefone.length()==10) && Validacao.validarTelefone(novoTelefone)==false)){
                        telefoneInvalido = true;
                    }

                    if((!senhaAtual.equals("") && senhaAtual.length()<6) || (!novaSenha.equals("") && novaSenha.length()<6)){
                        senhaAtualInvalida = novaSenhaInvalida = true;
                        senhaTamanhoInvalido = true;
                    }

                    if(!senhaTamanhoInvalido){
                        if(senhaAtual.equals(novaSenha)){
                            senhasIguais = true;
                        }
                    }

                    if(senhaAtualInvalida || novaSenhaInvalida || senhasIguais || telefoneInvalido){

                        if(senhaAtualInvalida || novaSenhaInvalida || telefoneInvalido){
                            configurarCamposInvalidosUI();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Preencha os campos corretamente", Toast.LENGTH_SHORT);
                        }

                        if(senhasIguais){
                            configurarCamposInvalidosUI();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Senhas iguais", Toast.LENGTH_SHORT);
                        }
                    }
                    else{

                        pbEditarCadastroLayout.setVisibility(View.VISIBLE);
                        configurarCamposInvalidosUI();
                        desabilitarUI();

                        // Prevenindo clique duplo por um limite de 3 segundos
                        if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                        ultimoClique = SystemClock.elapsedRealtime();

                        // Atualizar senha:

                        Log.i("AQUI", "Atualizando...");

                        AuthCredential credential = EmailAuthProvider.getCredential(sessao.getEmailUsuario(), senhaAtual);

                        // Prompt the user to re-provide their sign-in credentials
                        sessao.getUsuario().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    sessao.getUsuario().updatePassword(novaSenha).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                banco.setTelefone(usuarioKey, novoTelefone);

                                                // Thread para adicionar um delay após o processo de reautenticação:
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    public void run() {
                                                        Thread thread = new Thread() {
                                                            @Override
                                                            public void run() {
                                                                Log.i("MAIN", "atualizando senha...");
                                                                runOnUiThread( new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            CustomToast.mostrarMensagem(getApplicationContext(), "Alterações salvas com sucesso", Toast.LENGTH_SHORT);
                                                                            startActivity(new Intent(EditarCadastroActivity.this, MeuCadastroActivity.class));
                                                                            Transition.enterTransition(EditarCadastroActivity.this);
                                                                            finish();
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
                                                //Toast.makeText(getApplicationContext(), "Falha ao atualizar dados!", Toast.LENGTH_LONG).show();
                                                CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao salvar alterações", Toast.LENGTH_SHORT);
                                                pbEditarCadastroLayout.setVisibility(View.INVISIBLE);
                                                Log.d("Atualizando", "Error password not updated");
                                                habilitarUI();
                                            }
                                        }
                                    });

                                } else {
                                    //Toast.makeText(getApplicationContext(), "Senha atual inválida!", Toast.LENGTH_LONG).show();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Senha atual incorreta", Toast.LENGTH_SHORT);
                                    pbEditarCadastroLayout.setVisibility(View.INVISIBLE);
                                    //alteracoes = true;
                                    Log.d("Atualizando", "Error auth failed");
                                    habilitarUI();
                                }
                            }
                        });
                    }
                }
                else
                // ATUALIZAR NOME e TELEFONE e SENHA (digitar nova senha)
                if(atualizarNome && atualizarTelefone && atualizarSenhaAtual && !atualizarSenhaNova){

                    if(Validacao.validarNome(novoNome)==false){
                        nomeInvalido = true;
                    }

                    if(!novoTelefone.equals("") && (novoTelefone.length()!=11 && novoTelefone.length()!=10) || ((novoTelefone.length()==11 || novoTelefone.length()==10) && Validacao.validarTelefone(novoTelefone)==false)){
                        telefoneInvalido = true;
                    }

                    novaSenhaNaoDigitada = true;

                    configurarCamposInvalidosUI();
                    CustomToast.mostrarMensagem(getApplicationContext(), "Digite a nova senha", Toast.LENGTH_SHORT);
                }
                else
                // Atualizar NOME e TELEFONE e SENHA (digitar senha atual):
                if(atualizarNome && atualizarTelefone && !atualizarSenhaAtual && atualizarSenhaNova){

                    if(Validacao.validarNome(novoNome)==false){
                        nomeInvalido = true;
                    }

                    if(!novoTelefone.equals("") && (novoTelefone.length()!=11 && novoTelefone.length()!=10) || ((novoTelefone.length()==11 || novoTelefone.length()==10) && Validacao.validarTelefone(novoTelefone)==false)){
                        telefoneInvalido = true;
                    }

                    senhaAtualNaoDigitada = true;

                    configurarCamposInvalidosUI();
                    CustomToast.mostrarMensagem(getApplicationContext(), "Digite a senha atual", Toast.LENGTH_SHORT);
                }
                else
                // Atualizar NOME e TELEFONE e SENHA (ambas digitadas):
                if(atualizarNome && atualizarTelefone && atualizarSenhaAtual && atualizarSenhaNova){
                    if(Validacao.validarNome(novoNome)==false){
                        nomeInvalido = true;
                    }

                    if(!novoTelefone.equals("") && (novoTelefone.length()!=11 && novoTelefone.length()!=10) || ((novoTelefone.length()==11 || novoTelefone.length()==10) && Validacao.validarTelefone(novoTelefone)==false)){
                        telefoneInvalido = true;
                    }

                    if((!senhaAtual.equals("") && senhaAtual.length()<6) || (!novaSenha.equals("") && novaSenha.length()<6)){
                        senhaAtualInvalida = novaSenhaInvalida = true;
                        senhaTamanhoInvalido = true;
                    }

                    if(!senhaTamanhoInvalido){
                        if(senhaAtual.equals(novaSenha)){
                            senhasIguais = true;
                        }
                    }

                    if(senhaAtualInvalida || novaSenhaInvalida || senhasIguais || nomeInvalido || telefoneInvalido){

                        if(senhaAtualInvalida || novaSenhaInvalida || telefoneInvalido || nomeInvalido){
                            configurarCamposInvalidosUI();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Preencha os campos corretamente", Toast.LENGTH_SHORT);
                        }

                        if(senhasIguais){
                            configurarCamposInvalidosUI();
                            CustomToast.mostrarMensagem(getApplicationContext(), "Senhas iguais", Toast.LENGTH_SHORT);
                        }
                    }
                    else{

                        pbEditarCadastroLayout.setVisibility(View.VISIBLE);
                        configurarCamposInvalidosUI();
                        desabilitarUI();

                        // Prevenindo clique duplo por um limite de 3 segundos
                        if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                        ultimoClique = SystemClock.elapsedRealtime();

                        AuthCredential credential = EmailAuthProvider.getCredential(sessao.getEmailUsuario(), senhaAtual);

                        // Prompt the user to re-provide their sign-in credentials
                        sessao.getUsuario().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    sessao.getUsuario().updatePassword(novaSenha).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                banco.setNomeCompleto(usuarioKey, novoNome);
                                                banco.setTelefone(usuarioKey, novoTelefone);

                                                // Thread para adicionar um delay após o processo de reautenticação:
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    public void run() {
                                                        Thread thread = new Thread() {
                                                            @Override
                                                            public void run() {
                                                                Log.i("MAIN", "atualizando senha...");
                                                                runOnUiThread( new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            CustomToast.mostrarMensagem(getApplicationContext(), "Alterações salvas com sucesso", Toast.LENGTH_SHORT);
                                                                            startActivity(new Intent(EditarCadastroActivity.this, MeuCadastroActivity.class));
                                                                            Transition.enterTransition(EditarCadastroActivity.this);
                                                                            finish();
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
                                                //Toast.makeText(getApplicationContext(), "Falha ao atualizar dados!", Toast.LENGTH_LONG).show();
                                                CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao salvar alterações", Toast.LENGTH_SHORT);
                                                pbEditarCadastroLayout.setVisibility(View.INVISIBLE);
                                                Log.d("Atualizando", "Error password not updated");
                                                habilitarUI();
                                            }
                                        }
                                    });

                                } else {
                                    //Toast.makeText(getApplicationContext(), "Senha atual inválida!", Toast.LENGTH_LONG).show();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Senha atual incorreta", Toast.LENGTH_SHORT);
                                    pbEditarCadastroLayout.setVisibility(View.INVISIBLE);
                                    //alteracoes = true;
                                    Log.d("Atualizando", "Error auth failed");
                                    habilitarUI();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    // Se ocorrer alguma falha, habilitar novamente:
    public void habilitarUI(){
        etNomeCompletoAtualizar.setEnabled(true);
        etTelefoneAtualizar.setEnabled(true);
        etSenhaAtual.setEnabled(true);
        etNovaSenha.setEnabled(true);
        cbSenhaAtual.setEnabled(true);
        cbNovaSenha.setEnabled(true);
        //btnMenuEditarCadastro.setEnabled(true);
        btnExcluirContaEditar.setEnabled(true);
        btnAtualizarDados.setEnabled(true);
    }

    // Durante a execução da Progress Bar:
    public void desabilitarUI(){
        etNomeCompletoAtualizar.setEnabled(false);
        etTelefoneAtualizar.setEnabled(false);
        etSenhaAtual.setEnabled(false);
        etNovaSenha.setEnabled(false);
        cbSenhaAtual.setEnabled(false);
        cbNovaSenha.setEnabled(false);
        //btnMenuEditarCadastro.setEnabled(false);
        btnExcluirContaEditar.setEnabled(false);
        btnAtualizarDados.setEnabled(false);
    }

    public void verificarProvedor(){
        for (UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if (user.getProviderId().equals("google.com")) {
                Log.i("MAIN", "User is signed in with Google");
                googleProvider = true;
            }

            if (user.getProviderId().equals("facebook.com")) {
                Log.i("MAIN", "User is signed in with Facebook");
                facebookProvider = true;
            }

            if (user.getProviderId().equals("password")) {
                Log.i("MAIN", "User is signed in with Email/Password");
                emailProvider = true;
            }
        }
    }

    // Configuração da interface baseado ao provedor do usuário:
    public void configurarUI(){
        if(sessao.getUsuario()!=null){
            if (googleProvider || facebookProvider) {
                // Ocultar elementos para alteração de senha:
                labelAtualizarSenha.setVisibility(View.INVISIBLE);
                labelSenhaAtual.setVisibility(View.INVISIBLE);
                labelNovaSenha.setVisibility(View.INVISIBLE);
                etSenhaAtual.setVisibility(View.INVISIBLE);
                etNovaSenha.setVisibility(View.INVISIBLE);
                cbSenhaAtual.setVisibility(View.INVISIBLE);
                cbNovaSenha.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void carregarExtras(){

        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent");

            if(callerIntent.equals("MeuCadastroActivity")){

                carregarInformacoes();
            }

            if(callerIntent.equals("MenuActivity")){

                nome = extras.getString("Nome");
                telefoneAtual = extras.getString("Telefone");
                senhaAtual = extras.getString("SenhaAtual");
                novaSenha = extras.getString("NovaSenha");

                if(nome.equals("")){
                    etNomeCompletoAtualizar.setHint(nomeAtual);
                }
                else{
                    etNomeCompletoAtualizar.setText(nome);
                }

                if(telefoneAtual.equals("")){
                    etTelefoneAtualizar.setHint("Não informado");
                }
                else{
                    etTelefoneAtualizar.setText(telefoneAtual);
                }

                etSenhaAtual.setText(senhaAtual);
                etNovaSenha.setText(novaSenha);
            }
        }
    }

    public void configurarCamposInvalidosUI(){

        System.out.println("VALIDANDO - CONFIGURAR UI");

        Log.i("VALIDANDO", "NomeInvalido = " + nomeInvalido);
        Log.i("VALIDANDO", "TelefoneInvalido = " + telefoneInvalido);
        Log.i("VALIDANDO", "SenhaAtualInvalida = " + senhaAtualInvalida);
        Log.i("VALIDANDO", "NovaSenhaInvalida = " + novaSenhaInvalida);

        if(nomeInvalido){
            etNomeCompletoAtualizar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelNome.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etNomeCompletoAtualizar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelNome.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(telefoneInvalido){
            etTelefoneAtualizar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelTelefone.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etTelefoneAtualizar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelTelefone.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        if((!senhaAtualNaoDigitada && novaSenhaNaoDigitada) || (senhaAtualNaoDigitada && !novaSenhaNaoDigitada) ||
           (senhaAtualInvalida && !novaSenhaInvalida) || (!senhaAtualInvalida && novaSenhaInvalida) ||
           (senhaAtualInvalida && novaSenhaInvalida) || (senhasIguais)){

            etSenhaAtual.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelSenhaAtual.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelSenhaAtual.setText("Senha atual (6 ou mais caracteres)");

            etNovaSenha.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelNovaSenha.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelNovaSenha.setText("Nova senha (6 ou mais caracteres)");
        }
        else{
            etSenhaAtual.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelSenhaAtual.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelSenhaAtual.setText("Senha atual");

            etNovaSenha.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelNovaSenha.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelNovaSenha.setText("Nova senha");
        }
    }
}
