package br.ufc.crateus.halugar.Activities.Sessao;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.CompoundButton.OnCheckedChangeListener;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import br.ufc.crateus.halugar.Activities.main.MainActivity;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.Model.Usuario;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.Transition;
import br.ufc.crateus.halugar.Util.Validacao;

public class EntrarActivity extends AppCompatActivity {

    ImageButton btnVoltarEntrar;
    Button btnEntrar, btnEsqueciMinhaSenha, btnCriarConta, btnEntrarGoogle, btnEntrarFacebook;
    LoginButton btnSignInFacebook;
    EditText etEmailLogin, etSenhaLogin, etEmailRecuperarSenha;
    CheckBox cbSenha;
    TextView labelEmail, labelSenha;

    boolean emailVerified, possuiCadastro, flagVerificacao;
    boolean emailInvalido=false, senhaInvalida=false;

    final int RC_SIGN_IN = 9001;

    CallbackManager mCallbackManager;

    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;

    Sessao sessao;
    Banco banco;

    ConstraintLayout pbEntrarLayout;
    Context context = this;

    long ultimoClique = 0;

    boolean loginGoogle, loginFacebook;

    String email, senha, emailConfirmacao, userKey;

    SharedPreferences credentials;

    Bundle extras;
    String callerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrar);

        pbEntrarLayout = (ConstraintLayout)findViewById(R.id.pbEntrarLayout);

        btnEntrar = (Button) findViewById(R.id.btnEntrar);
        etEmailLogin = (EditText) findViewById(R.id.etEmailLogin);
        etSenhaLogin = (EditText) findViewById(R.id.etSenhaLogin);
        btnVoltarEntrar = (ImageButton) findViewById(R.id.btnVoltarEntrar);
        btnEsqueciMinhaSenha = (Button)findViewById(R.id.btnEsqueciMinhaSenha);
        btnEntrarGoogle = (Button)findViewById(R.id.btnEntrarGoogle);
        btnSignInFacebook = (LoginButton) findViewById(R.id.btnSignInFacebook);
        btnEntrarFacebook = (Button)findViewById(R.id.btnEntrarFacebook);
        btnCriarConta = (Button)findViewById(R.id.btnCriarConta);
        cbSenha = (CheckBox) findViewById(R.id.cbSenha);
        labelEmail = (TextView)findViewById(R.id.labelEmail);
        labelSenha = (TextView)findViewById(R.id.labelSenha);

        flagVerificacao = true;

        sessao = Sessao.getInstance();
        banco = Banco.getInstance();

        possuiCadastro = false;
        loginFacebook = loginGoogle = false;

        carregarInformacoes();

        credentials = getSharedPreferences("credentials", MODE_PRIVATE);

        verificarEmail();

        etEmailLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        btnEsqueciMinhaSenha.setOnClickListener(view -> recuperarSenha());

        btnVoltarEntrar.setOnClickListener(view -> voltar());

        btnEntrar.setOnClickListener(v -> entrar());

        btnCriarConta.setOnClickListener(view -> criarConta());

        btnEntrarGoogle.setOnClickListener(view -> signInGoogle());

        btnEntrarFacebook.setOnClickListener(view -> signInFacebook());

        cbSenha.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // checkbox status is changed from uncheck to checked.
                if (!isChecked) {
                    // hide password
                    etSenhaLogin.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    // show password
                    etSenhaLogin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
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
        voltar();
    }

    public void voltar(){

        flagVerificacao = false;
        startActivity(new Intent(EntrarActivity.this, MainActivity.class));
        Transition.backTransition(EntrarActivity.this);
        finish();
    }

    public void verificarEmail(){
        // Thread para verificar se o email foi validado antes que o usuário clique em ENTRAR:
        Thread thread = new Thread() {
            @Override
            public void run() {
                while(flagVerificacao) {
                    runOnUiThread( new Runnable() {
                        @Override
                        public void run() {
                            if(sessao.getUsuario()!=null && sessao.getAutenticacaoUsuario()!=null){
                                sessao.getAutenticacaoUsuario().getCurrentUser().reload();
                                emailVerified = sessao.getUsuario().isEmailVerified();
                                Log.i("CONTA - ENTRAR 1", "Validando (acabou de criar a conta): " + emailVerified);
                            }
                            else{
                                Log.i("CONTA - ENTRAR 2", "Validando (app reiniciado): " + emailVerified);
                            }
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
    }

    private void signInGoogle() {

        loginFacebook = false;
        loginGoogle = true;
        possuiCadastro = false;

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                // Prevenindo clique duplo por um limite de 5 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 5000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                // Configure Google Sign In
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(loginGoogle){
            // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                // The Task returned from this call is always completed, no need to attach a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
        }

        if(loginFacebook){
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
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
            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao entrar", Toast.LENGTH_SHORT);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("CONTA", "firebaseAuthWithGoogle:" + acct.getId());

        pbEntrarLayout.setVisibility(View.VISIBLE);
        desabilitarUI();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        sessao.getAutenticacaoUsuario().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("CONTA", "signInWithCredential:success");
                            flagVerificacao = false;

                            // Verificar se o usuário já estava cadastrado no banco:
                            banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot usuario : dataSnapshot.getChildren()){
                                        if(account.getEmail().equals(usuario.getValue(Usuario.class).getEmail())){
                                            possuiCadastro = true;
                                            userKey = usuario.getKey();
                                        }
                                    }

                                    // Se possuir cadastro, apenas entrar. Caso contrário, cadastrar no banco:
                                    if(possuiCadastro){

                                        // Definindo que o usuário possui conta e email verificado (utilizado durante o ciclo de vida do app):
                                        SharedPreferences.Editor editor = credentials.edit();

                                        editor.putBoolean("userVerified", true);
                                        editor.putString("userKey", userKey);
                                        editor.apply();

                                        startActivity(new Intent(EntrarActivity.this, MainActivity.class));
                                        finish();
                                        Transition.enterTransition(EntrarActivity.this);
                                    }
                                    else{
                                        // Cadastrando usuário no banco de dados:
                                        Usuario usuario = new Usuario(sessao.getUsuario().getUid(), "", account.getDisplayName(), account.getEmail(), "");
                                        banco.adicionarUsuario(usuario);

                                        Log.i("CONTA", "novo usuario = " + usuario.toString());
                                        Log.i("CONTA", "provider = " + sessao.getUsuario().getProviderData());

                                        // Definindo que o usuário possui conta e email verificado (utilizado durante o ciclo de vida do app):
                                        SharedPreferences.Editor editor = credentials.edit();

                                        editor.putBoolean("userVerified", true);
                                        editor.putString("userKey", userKey);
                                        editor.apply();

                                        startActivity(new Intent(EntrarActivity.this, MainActivity.class));
                                        finish();
                                        Transition.enterTransition(EntrarActivity.this);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("CONTA", "signInWithCredential:failure", task.getException());
                            CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao entrar", Toast.LENGTH_SHORT);
                            habilitarUI();
                        }
                    }
                });
    }

    public void signInFacebook(){

        loginFacebook = true;
        loginGoogle = false;
        possuiCadastro = false;

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                // Prevenindo clique duplo por um limite de 5 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 5000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                FacebookSdk.sdkInitialize(getApplicationContext());

                // Initialize Facebook Login button
                mCallbackManager = CallbackManager.Factory.create();

                LoginButton loginButton = findViewById(R.id.btnSignInFacebook);
                loginButton.setReadPermissions("email", "public_profile");

                btnSignInFacebook.performClick();

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
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("CONTA", "handleFacebookAccessToken:" + token);

        pbEntrarLayout.setVisibility(View.VISIBLE);
        desabilitarUI();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        sessao.getAutenticacaoUsuario().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("CONTA", "signInWithCredential:success");
                            flagVerificacao = false;

                            // Verificar se o usuário já estava cadastrado no banco:
                            banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot usuario : dataSnapshot.getChildren()){
                                        if(sessao.getUsuario().getEmail().equals(usuario.getValue(Usuario.class).getEmail())){
                                            possuiCadastro = true;
                                            userKey = usuario.getKey();
                                        }
                                    }

                                    // Se possuir cadastro, apenas entrar. Caso contrário, cadastrar no banco:
                                    if(possuiCadastro){
                                        // Definindo que o usuário possui conta e email verificado (utilizado durante o ciclo de vida do app):
                                        SharedPreferences.Editor editor = credentials.edit();
                                        editor.putBoolean("userVerified", true);
                                        editor.putString("userKey", userKey);
                                        editor.apply();

                                        startActivity(new Intent(EntrarActivity.this, MainActivity.class));
                                        finish();
                                        Transition.enterTransition(EntrarActivity.this);
                                    }
                                    else{
                                        // Cadastrando usuário no banco de dados:
                                        Usuario usuario = new Usuario(sessao.getUsuario().getUid(), "", sessao.getUsuario().getDisplayName(), sessao.getUsuario().getEmail(), "");
                                        banco.adicionarUsuario(usuario);

                                        Log.i("CONTA", "novo usuario = " + usuario.toString());
                                        Log.i("CONTA", "provider = " + sessao.getUsuario().getProviderData());

                                        // Definindo que o usuário possui conta e email verificado (utilizado durante o ciclo de vida do app):
                                        SharedPreferences.Editor editor = credentials.edit();
                                        editor.putBoolean("userVerified", true);
                                        editor.putString("userKey", userKey);
                                        editor.apply();

                                        startActivity(new Intent(EntrarActivity.this, MainActivity.class));
                                        finish();
                                        Transition.enterTransition(EntrarActivity.this);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("CONTA", "signInWithCredential:failure", task.getException());

                            // Mostrar mensagem para o usuário:

                            LayoutInflater li = LayoutInflater.from(context);
                            View verificarEmail = li.inflate(R.layout.custom_alert_dialog, null);
                            TextView tvEmailCadastrado = (TextView) verificarEmail.findViewById(R.id.tvMensagemAlertDialog);

                            if(loginFacebook){
                                tvEmailCadastrado.setText("O email desta conta Facebook já possui cadastro.");
                            }

                            if(loginGoogle){
                                tvEmailCadastrado.setText("O email desta conta Google já possui cadastro.");
                            }

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    context);

                            alertDialogBuilder.setTitle("Falha ao entrar");

                            // set prompts.xml to alertdialog builder
                            alertDialogBuilder.setView(verificarEmail);

                            // set dialog message
                            alertDialogBuilder
                                    .setCancelable(false)
                                    .setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,int id) {
                                                    pbEntrarLayout.setVisibility(View.INVISIBLE);
                                                    Sessao.logout();
                                                    startActivity(new Intent(EntrarActivity.this, EntrarActivity.class));
                                                    Transition.enterTransition(EntrarActivity.this);
                                                }
                                            });

                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();

                            // show it
                            alertDialog.show();

                            //CustomToast.mostrarMensagemCentralizada(getApplicationContext(), "O email desta conta Facebook\njá foi cadastrado");
                        }
                    }
                });
    }

    public void recuperarSenha(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                // Prevenindo clique duplo por um limite de 3 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View recuperarSenha = li.inflate(R.layout.recuperar_senha, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(recuperarSenha);
                alertDialogBuilder.setTitle("Confirme seu email:");

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Recuperar senha",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                        etEmailRecuperarSenha = recuperarSenha.findViewById(R.id.etEmailRecuperarSenha);
                                        emailConfirmacao = etEmailRecuperarSenha.getText().toString().trim();

                                        if(emailConfirmacao.equals("")){
                                            CustomToast.mostrarMensagem(getApplicationContext(), "Confirme seu email", Toast.LENGTH_SHORT);
                                        }
                                        else
                                        if(Validacao.validarEmail(emailConfirmacao)==false){
                                            CustomToast.mostrarMensagem(getApplicationContext(), "Email inválido", Toast.LENGTH_SHORT);
                                        }
                                        else{

                                            desabilitarUI();

                                            // Prevenindo clique duplo por um limite de 3 segundos
                                            if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                                            ultimoClique = SystemClock.elapsedRealtime();

                                            final ProgressDialog progressDialog = new ProgressDialog(context);
                                            progressDialog.setTitle("Aguarde...");
                                            progressDialog.setMessage("Procurando conta");
                                            progressDialog.show();

                                            // Thread utilizada para aguardar a resposta da autenticação do usuário e envio do link de recuperação:
                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                public void run() {
                                                    Thread thread = new Thread() {
                                                        @Override
                                                        public void run() {
                                                            runOnUiThread( new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    try {
                                                                        Log.i("CONTA", "validando email...");
                                                                        sessao.getAutenticacaoUsuario().sendPasswordResetEmail(etEmailRecuperarSenha.getText().toString())
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {

                                                                                            progressDialog.dismiss();

                                                                                            Log.d("CONTA", "Email sent.");
                                                                                            CustomToast.mostrarMensagem(getApplicationContext(), "Link para recuperação enviado", Toast.LENGTH_SHORT);

                                                                                            habilitarUI();
                                                                                        }
                                                                                        else{
                                                                                            Log.d("CONTA", "Email not sent.");
                                                                                            progressDialog.dismiss();

                                                                                            CustomToast.mostrarMensagem(getApplicationContext(), "Conta não encontrada", Toast.LENGTH_SHORT);
                                                                                            habilitarUI();
                                                                                        }
                                                                                    }
                                                                                });
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    };

                                                    thread.start();
                                                }
                                            }, 0);
                                        }
                                    }
                                })
                        .setNegativeButton("Cancelar",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
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

    public void criarConta(){
        flagVerificacao = false;

        Intent i = new Intent(EntrarActivity.this, NovaContaActivity.class);

        i.putExtra("callerIntent", "EntrarActivity");

        startActivity(i);
        Transition.enterTransition(EntrarActivity.this);
        finish();
    }

    public void entrar(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                // Prevenindo clique duplo por um limite de 3 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                email = etEmailLogin.getText().toString();
                senha = etSenhaLogin.getText().toString();

                emailInvalido = senhaInvalida = false;

                if(email.equals("") || Validacao.validarEmail(email)==false){
                    //msgDadosInvalidos += "\n   - Email válido";
                    //dadosInvalidos = true;
                    emailInvalido = true;
                }
                else{
                    emailInvalido = false;
                }

                if(senha.equals("") || senha.length()<6){
                    //msgDadosInvalidos += "\n   - Senha (6 ou mais caracteres)";
                    //dadosInvalidos = true;
                    senhaInvalida = true;
                }
                else{
                    senhaInvalida = false;
                }

                if(senhaInvalida || emailInvalido){

                    CustomToast.mostrarMensagem(getApplicationContext(), "Preencha os campos corretamente", Toast.LENGTH_SHORT);
                    configurarCamposInvalidosUI();
                }
                else{

                    pbEntrarLayout.setVisibility(View.VISIBLE);
                    configurarCamposInvalidosUI();
                    desabilitarUI();

                    // Usuário acabou de criar conta (redirecionado de NovaConta):
                    if(sessao.getUsuario()!=null){

                        if(emailVerified){

                            flagVerificacao = false;

                            sessao.getAutenticacaoUsuario().signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("CONTA - ENTRAR", "Login realizado com sucesso!");

                                        SharedPreferences.Editor editor = credentials.edit();

                                        // Recuperando key do usuário:
                                        banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for(DataSnapshot usuario : dataSnapshot.getChildren()){
                                                    if(sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())){
                                                        userKey = usuario.getKey();

                                                        // Cadastrando as credenciais do usuário no SharedPreferences
                                                        editor.putBoolean("userVerified", true);
                                                        editor.putString("userPassword", senha);
                                                        editor.putString("userKey", userKey);
                                                        editor.putString("userId", sessao.getIdUsuario());
                                                        editor.apply();

                                                        startActivity(new Intent(EntrarActivity.this, MainActivity.class));
                                                        Transition.enterTransition(EntrarActivity.this);
                                                        finish();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("CONTA - ENTRAR", "Login Falhou", task.getException());
                                        // NEW TOAST
                                        pbEntrarLayout.setVisibility(View.INVISIBLE);
                                        //CustomToast.mostrarMensagem(getApplicationContext(), "Senha incorreta", Toast.LENGTH_SHORT);
                                        CustomToast.mostrarMensagem(getApplicationContext(), "Email ou senha incorreta", Toast.LENGTH_SHORT);
                                        habilitarUI();
                                    }
                                }
                            });
                        }
                        else{
                            // Email não verificado:
                            CustomToast.mostrarMensagem(getApplicationContext(), "Email não verificado", Toast.LENGTH_SHORT);
                            Log.i("CONTA", "Falha ao entrar, email não verificado");
                            pbEntrarLayout.setVisibility(View.INVISIBLE);
                            habilitarUI();
                        }
                    }
                    // Usuário saiu do app (onDestroy), perdendo sua autenticação do Firebase, e abriu novamente (após criar conta):
                    // Ou usuário possui conta, mas saiu do aplicativo (fechou - onDestroy)... Necessário autenticar novamente:
                    else{

                        pbEntrarLayout.setVisibility(View.VISIBLE);
                        configurarCamposInvalidosUI();
                        desabilitarUI();

                        sessao.getAutenticacaoUsuario().signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                // Usuário possui conta:
                                if (task.isSuccessful()) {

                                    // Verificação de email:
                                    if(emailVerified){
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("CONTA - ENTRAR", "Login realizado com sucesso!");
                                        flagVerificacao = false;

                                        // Definindo que o usuário possui conta e email verificado (utilizado durante o ciclo de vida do app):
                                        SharedPreferences.Editor editor = credentials.edit();

                                        // Recuperando key do usuário:
                                        banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for(DataSnapshot usuario : dataSnapshot.getChildren()){
                                                    if(sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())){
                                                        userKey = usuario.getKey();

                                                        // Cadastrando as credenciais do usuário no SharedPreferences
                                                        editor.putBoolean("userVerified", true);
                                                        editor.putString("userPassword", senha);
                                                        editor.putString("userKey", userKey);
                                                        editor.apply();

                                                        startActivity(new Intent(EntrarActivity.this, MainActivity.class));
                                                        Transition.enterTransition(EntrarActivity.this);
                                                        finish();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    else{
                                        // Email não verificado...
                                        Log.i("CONTA", "Falha ao entrar, email ainda não verificado... Clicar em ENTRAR novamente");

                                        // Thread para aguardar o primeiro clique ser consumido (que inicia a verificação do email):
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                Log.i("CONTA", "Clicando novamente...");
                                                // Uma vez que o email foi verificado, ao clicar no botão, o login é realizado:
                                                btnEntrar.performClick();
                                            }
                                        }, 3000);
                                    }

                                } else {
                                    // Falhas: Email não cadastrado ou senha incorreta
                                    pbEntrarLayout.setVisibility(View.INVISIBLE);
                                    configurarCamposInvalidosUI();
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Email ou senha incorreta", Toast.LENGTH_SHORT);
                                    Log.i("CONTA", "Falha ao entrar (email não cadastrado || senha incorreta)");
                                    habilitarUI();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    // Habilitar elementos caso ocorra algum erro:
    public void habilitarUI(){
        etEmailLogin.setEnabled(true);
        etSenhaLogin.setEnabled(true);
        cbSenha.setEnabled(true);
        btnEsqueciMinhaSenha.setEnabled(true);
        //btnVoltarEntrar.setEnabled(true);
        btnEntrar.setEnabled(true);
        btnCriarConta.setEnabled(true);
        btnEntrarFacebook.setEnabled(true);
        btnEntrarGoogle.setEnabled(true);
    }

    // Desabilitar elementos da interface durante execução do Progress Bar:
    public void desabilitarUI(){
        etEmailLogin.setEnabled(false);
        etSenhaLogin.setEnabled(false);
        cbSenha.setEnabled(false);
        btnEsqueciMinhaSenha.setEnabled(false);
        //btnVoltarEntrar.setEnabled(false);
        btnEntrar.setEnabled(false);
        btnCriarConta.setEnabled(false);
        btnEntrarFacebook.setEnabled(false);
        btnEntrarGoogle.setEnabled(false);
    }

    public void carregarInformacoes(){

        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent");
        }
    }

    public void configurarCamposInvalidosUI(){

        if(emailInvalido){
            etEmailLogin.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelEmail.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etEmailLogin.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelEmail.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(senhaInvalida){
            etSenhaLogin.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelSenha.setText("Senha (6 ou mais caracteres)");
            labelSenha.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etSenhaLogin.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelSenha.setText("Senha");
            labelSenha.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }
    }
}