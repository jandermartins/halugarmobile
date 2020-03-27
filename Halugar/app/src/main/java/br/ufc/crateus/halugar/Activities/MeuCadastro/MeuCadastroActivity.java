package br.ufc.crateus.halugar.Activities.MeuCadastro;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

import br.ufc.crateus.halugar.Activities.Sessao.EntrarActivity;
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

public class MeuCadastroActivity extends AppCompatActivity {

    TextView tvNomeCompletoCadastro, tvTelefoneCadastro, tvEmailCadastro;
    Button btnAtualizarConta;
    LoginButton btnSignInFacebookValidation;
    ImageButton btnMenuMeuCadastro, btnExcluirConta;

    String anuncioKey;
    String usuarioId, usuarioKey, nome, email, telefone;

    final Context context = this;

    ConstraintLayout pbMeuCadastroLayout;
    ConstraintLayout pbMeuCadastroLayoutRemover;

    Banco banco;
    Sessao sessao;

    View viewMenu;

    boolean googleProvider, facebookProvider, emailProvider;

    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;

    final int RC_SIGN_IN = 9001;

    CallbackManager mCallbackManager;
    boolean possuiAnuncio;

    SharedPreferences credentials;

    Bundle extras;
    String callerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meu_cadastro);

        tvNomeCompletoCadastro = (TextView) findViewById(R.id.tvNomeCompletoCadastro);
        tvTelefoneCadastro = (TextView) findViewById(R.id.tvTelefoneCadastro);
        tvEmailCadastro = (TextView) findViewById(R.id.tvEmailCadastro);

        btnAtualizarConta = (Button) findViewById(R.id.btnAtualizarConta);
        btnExcluirConta = (ImageButton) findViewById(R.id.btnExcluirConta);
        btnMenuMeuCadastro = (ImageButton)findViewById(R.id.btnMenuMeuCadastro);

        btnSignInFacebookValidation = (LoginButton)findViewById(R.id.btnSignInFacebookValidation);

        pbMeuCadastroLayout = (ConstraintLayout)findViewById(R.id.pbMeuCadastroLayout);
        pbMeuCadastroLayoutRemover = (ConstraintLayout)findViewById(R.id.pbMeuCadastroLayoutRemover);

        viewMenu = findViewById(android.R.id.content).getRootView();

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        credentials = getSharedPreferences("credentials", MODE_PRIVATE);

        usuarioId = credentials.getString("userId", "-");

        googleProvider = facebookProvider = emailProvider = false;

        possuiAnuncio = false;

        verificarProvedor();

        carregarExtras();
        habilitarSwipeMenu();
        carregarInformacoes();

        btnAtualizarConta.setOnClickListener(view -> atualizarConta());

        btnExcluirConta.setOnClickListener(view -> removerConta());

        btnMenuMeuCadastro.setOnClickListener(view -> {

            Intent i = new Intent(MeuCadastroActivity.this, MenuActivity.class);

            i.putExtra("callerIntent", "MeuCadastroActivity");

            startActivity(i);
            Transition.enterTransition(MeuCadastroActivity.this);
            finish();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
                pbMeuCadastroLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(MeuCadastroActivity.this, MainActivity.class);

        startActivity(i);
        Transition.backTransition(MeuCadastroActivity.this);
        finish();
    }

    public void habilitarSwipeMenu(){

        viewMenu.setOnTouchListener(new OnSwipeTouchListener(MeuCadastroActivity.this) {
            public void onSwipeRight() {

                Intent i = new Intent(MeuCadastroActivity.this, MenuActivity.class);

                i.putExtra("callerIntent", "MeuCadastroActivity");

                startActivity(i);
                Transition.enterTransition(MeuCadastroActivity.this);
                finish();
            }
        });
    }

    public void carregarExtras(){

        // Solução para corrigir o bug da atualização de senha, que torna o resultado de sessao.getIdUsuario() nulo:
        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent");
        }
    }

    public void carregarInformacoes(){

        // Thread para aguardar o carregamento das informações:
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Log.i("MAIN", "carregando informações...");
                        runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    pbMeuCadastroLayout.setVisibility(View.INVISIBLE);
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

        banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot usuario : dataSnapshot.getChildren()){

                    if (usuarioId.equals(usuario.getValue(Usuario.class).getuId())) {

                        nome = String.valueOf(usuario.getValue(Usuario.class).getNomeCompleto());
                        email = String.valueOf(usuario.getValue(Usuario.class).getEmail());
                        telefone = String.valueOf(usuario.getValue(Usuario.class).getTelefone());

                        tvNomeCompletoCadastro.setText(usuario.getValue(Usuario.class).getNomeCompleto());
                        tvEmailCadastro.setText(usuario.getValue(Usuario.class).getEmail());

                        if(telefone.equals("")){
                            tvTelefoneCadastro.setText("Não informado");
                        }
                        else{
                            tvTelefoneCadastro.setText(Formatacao.formatarTelefone(String.valueOf(usuario.getValue(Usuario.class).getTelefone())));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void atualizarConta(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                Intent i = new Intent(MeuCadastroActivity.this, EditarCadastroActivity.class);

                i.putExtra("callerIntent", "MeuCadastroActivity");
                i.putExtra("idUsuario", usuarioId);

                startActivity(i);
                Transition.enterTransition(MeuCadastroActivity.this);
                finish();
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

                                            pbMeuCadastroLayoutRemover.setVisibility(View.VISIBLE);
                                            desabilitarUI();

                                            if(googleProvider){
                                                // Configure Google Sign In
                                                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                        .requestIdToken(getString(R.string.default_web_client_id))
                                                        .requestEmail()
                                                        .build();

                                                mGoogleSignInClient = GoogleSignIn.getClient(MeuCadastroActivity.this, gso);

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
                                                        Log.d("LOGOUT", "facebook:onSuccess:" + loginResult);
                                                        handleFacebookAccessToken(loginResult.getAccessToken());
                                                    }

                                                    @Override
                                                    public void onCancel() {
                                                        Log.d("LOGOUT", "facebook:onCancel");
                                                    }

                                                    @Override
                                                    public void onError(FacebookException error) {
                                                        Log.d("LOGOUT", "facebook:onError", error);
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
                                            pbMeuCadastroLayoutRemover.setVisibility(View.VISIBLE);
                                            desabilitarUI();

                                            usuarioId = sessao.getIdUsuario(); // Para realizar remoção das tabelas

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

                                                                            if (sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())) {

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
                                                                                                                                    startActivity(new Intent(MeuCadastroActivity.this, MainActivity.class));
                                                                                                                                    Transition.enterTransition(MeuCadastroActivity.this);
                                                                                                                                    finish();
                                                                                                                                }
                                                                                                                                else{
                                                                                                                                    Log.i("CONTA", "falha ao remover usuário @ authentication");
                                                                                                                                    //Toast.makeText(getApplicationContext(), "Falha ao excluir conta!", Toast.LENGTH_LONG).show();
                                                                                                                                    pbMeuCadastroLayoutRemover.setVisibility(View.INVISIBLE);
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
                                                                                                                                startActivity(new Intent(MeuCadastroActivity.this, MainActivity.class));
                                                                                                                                Transition.enterTransition(MeuCadastroActivity.this);
                                                                                                                                finish();
                                                                                                                            }
                                                                                                                            else{
                                                                                                                                Log.i("CONTA", "falha ao remover usuário @ authentication");
                                                                                                                                //Toast.makeText(getApplicationContext(), "Falha ao excluir conta!", Toast.LENGTH_LONG).show();
                                                                                                                                pbMeuCadastroLayoutRemover.setVisibility(View.INVISIBLE);
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
                                                                            if (sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())) {

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
                                                                pbMeuCadastroLayoutRemover.setVisibility(View.INVISIBLE);
                                                                habilitarUI();
                                                                CustomToast.mostrarMensagem(MeuCadastroActivity.this, "Senha incorreta", Toast.LENGTH_SHORT);
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

    // Desabilitar ao realizar exclusão da conta:
    public void desabilitarUI(){
        //btnMenuMeuCadastro.setEnabled(false);
        btnExcluirConta.setEnabled(false);
        btnAtualizarConta.setEnabled(false);
    }

    // Reabilitar caso a exclusão falhe:
    public void habilitarUI(){
        //btnMenuMeuCadastro.setEnabled(true);
        btnExcluirConta.setEnabled(true);
        btnAtualizarConta.setEnabled(true);
    }

    // Verificação de provedor para autenticar usuário no processo de remoção de conta:
    public void verificarProvedor(){
        if(sessao.getUsuario()!=null){
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

            pbMeuCadastroLayoutRemover.setVisibility(View.INVISIBLE);
            habilitarUI();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("CADASTRO", "firebaseAuthWithGoogle:" + acct.getId());

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

                                        Log.i("CADASTRO", "excluindo anuncios do usuario @ anuncio");

                                        if (sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())) {

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

                                                                                                Log.i("CADASTRO", "usuário removido com sucesso @ authentication");

                                                                                                // Removendo credenciais do usuário do Shared Preferences:
                                                                                                context.getSharedPreferences("credentials", 0).edit().clear().apply();

                                                                                                // Remoção concluída com sucesso...
                                                                                                CustomToast.mostrarMensagem(getApplicationContext(), "Conta removida com sucesso", Toast.LENGTH_SHORT);

                                                                                                Sessao.logout();

                                                                                                startActivity(new Intent(MeuCadastroActivity.this, MainActivity.class));
                                                                                                Transition.enterTransition(MeuCadastroActivity.this);
                                                                                                finish();
                                                                                            }
                                                                                            else{
                                                                                                Log.i("CADASTRO", "falha ao remover usuário @ authentication");
                                                                                                //Toast.makeText(getApplicationContext(), "Falha ao excluir conta!", Toast.LENGTH_LONG).show();
                                                                                                pbMeuCadastroLayoutRemover.setVisibility(View.INVISIBLE);
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

                                                                                            startActivity(new Intent(MeuCadastroActivity.this, MainActivity.class));
                                                                                            Transition.enterTransition(MeuCadastroActivity.this);
                                                                                            finish();
                                                                                        }
                                                                                        else{
                                                                                            Log.i("CADASTRO", "falha ao remover usuário @ authentication");
                                                                                            //Toast.makeText(getApplicationContext(), "Falha ao excluir conta!", Toast.LENGTH_LONG).show();
                                                                                            pbMeuCadastroLayoutRemover.setVisibility(View.INVISIBLE);
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
                                        if (sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())) {

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
                            Log.w("CONTA", "signInWithCredential:failure", task.getException());
                            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao remover conta", Toast.LENGTH_SHORT);
                            pbMeuCadastroLayoutRemover.setVisibility(View.INVISIBLE);
                            habilitarUI();
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("CADASTRO", "handleFacebookAccessToken:" + token);

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

                                        if (sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())) {

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

                                                                                                startActivity(new Intent(MeuCadastroActivity.this, MainActivity.class));
                                                                                                Transition.enterTransition(MeuCadastroActivity.this);
                                                                                                finish();
                                                                                            }
                                                                                            else{
                                                                                                Log.i("CADASTRO", "falha ao remover usuário @ authentication");
                                                                                                //Toast.makeText(getApplicationContext(), "Falha ao excluir conta!", Toast.LENGTH_LONG).show();
                                                                                                pbMeuCadastroLayoutRemover.setVisibility(View.INVISIBLE);
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

                                                                                            startActivity(new Intent(MeuCadastroActivity.this, MainActivity.class));
                                                                                            Transition.enterTransition(MeuCadastroActivity.this);
                                                                                            finish();
                                                                                        }
                                                                                        else{
                                                                                            Log.i("CADASTRO", "falha ao remover usuário @ authentication");
                                                                                            //Toast.makeText(getApplicationContext(), "Falha ao excluir conta!", Toast.LENGTH_LONG).show();
                                                                                            pbMeuCadastroLayoutRemover.setVisibility(View.INVISIBLE);
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
                                        if (sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())) {

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

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("CADASTRO", "signInWithCredential:failure", task.getException());
                            pbMeuCadastroLayoutRemover.setVisibility(View.INVISIBLE);
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
}
