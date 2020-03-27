package br.ufc.crateus.halugar.Banco;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import br.ufc.crateus.halugar.Model.Anuncio;
import br.ufc.crateus.halugar.Model.Usuario;

// Persistência no banco de dados utilizando o padrão de projeto Singleton:

public class Banco {

    private FirebaseDatabase database;
    private DatabaseReference tabelaUsuario, tabelaAnuncio;
    private StorageReference imagens;
    private FirebaseStorage storage;

    private static Banco instancia = new Banco();

    private Banco(){

        database = FirebaseDatabase.getInstance("https://halugar-59f53.firebaseio.com/");
        tabelaUsuario = database.getReference("usuario");
        tabelaAnuncio = database.getReference("anuncio");
        imagens = FirebaseStorage.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
    }

    public static Banco getInstance(){

        if(instancia==null) return new Banco();

        return instancia;
    }

    public DatabaseReference getTabelaUsuario(){
        return tabelaUsuario;
    }

    public DatabaseReference getTabelaAnuncio(){
        return tabelaAnuncio;
    }

    public StorageReference getImagem(String idImagem){

        return imagens.child("images/" + idImagem);
    }

    public DatabaseReference getTabelaFavoritos(String keyUsuario){
        return instancia.getTabelaUsuario().child(keyUsuario).child("meus_favoritos");
    }

    public DatabaseReference getTabelaMeusAnuncios(String keyUsuario){
        return instancia.getTabelaUsuario().child(keyUsuario).child("meus_anuncios");
    }

    // Persistência do usuário:

    public void adicionarUsuario(Usuario usuario){

        instancia.getTabelaUsuario().push().setValue(usuario, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                // Recuperando a key do usuário cadastrado - Realizar acesso em O(1):
                String uKey = databaseReference.getKey();
                instancia.getTabelaUsuario().child(uKey).child("uKey").setValue(uKey);
            }
        });
    }

    public void removerUsuario(String keyUsuario){
        instancia.getTabelaUsuario().child(keyUsuario).removeValue();
    }

    public void setNomeCompleto(String keyUsuario, String nomeCompleto){
        instancia.getTabelaUsuario().child(keyUsuario).child("nomeCompleto").setValue(nomeCompleto);
    }

    public void setTelefone(String keyUsuario, String telefone){
        instancia.getTabelaUsuario().child(keyUsuario).child("telefone").setValue(telefone);
    }

    // Persistência do anúncio:

    public void adicionarAnuncio(String idUsuario, Anuncio anuncio){

        instancia.getTabelaAnuncio().push().setValue(anuncio, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                // Recuperando o id do anúncio criado:
                String anuncioId = databaseReference.getKey();
                instancia.getTabelaAnuncio().child(anuncioId).child("aId").setValue(anuncioId);

                instancia.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                            if (idUsuario.equals(snapshot.getValue(Usuario.class).getuId())) {

                                // Recuperando a key do usuário que criou o anúncio:
                                String userKey = snapshot.getKey();

                                // Adicionando a key do usuário ao anúncio:
                                instancia.getTabelaAnuncio().child(anuncioId).child("keyUsuario").setValue(userKey);

                                // Setando o id do anúncio criado na tabela de anúncios do usuário:
                                instancia.getTabelaMeusAnuncios(userKey).push().setValue(anuncioId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    public void removerAnuncio(String idUsuario, String idAnuncio){

        // Removendo imagens do anúncio do storage:

        instancia.getTabelaAnuncio().addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot anuncio : dataSnapshot.getChildren()){

                    if(idAnuncio.equals(anuncio.getValue(Anuncio.class).getaId())){

                        if(!anuncio.getValue(Anuncio.class).getUrlImagemPrincipal().equals("")){
                            removerImagem(anuncio.getValue(Anuncio.class).getUrlImagemPrincipal());
                        }

                        if(!anuncio.getValue(Anuncio.class).getUrlImagemDois().equals("")){
                            removerImagem(anuncio.getValue(Anuncio.class).getUrlImagemDois());
                        }

                        if(!anuncio.getValue(Anuncio.class).getUrlImagemTres().equals("")){
                            removerImagem(anuncio.getValue(Anuncio.class).getUrlImagemTres());
                        }

                        if(!anuncio.getValue(Anuncio.class).getUrlImagemQuatro().equals("")){
                            removerImagem(anuncio.getValue(Anuncio.class).getUrlImagemQuatro());
                        }

                        if(!anuncio.getValue(Anuncio.class).getUrlImagemCinco().equals("")){
                            removerImagem(anuncio.getValue(Anuncio.class).getUrlImagemCinco());
                        }

                        // Removendo anúncio da tabela anuncio:
                        instancia.getTabelaAnuncio().child(idAnuncio).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Removendo idAnuncio da tabela meus_anuncios:

        instancia.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot usuario : dataSnapshot.getChildren()){

                    if(idUsuario.equals(usuario.getValue(Usuario.class).getuId())){

                        String usuarioKey = usuario.getKey();

                        instancia.getTabelaMeusAnuncios(usuarioKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for(DataSnapshot anuncio: dataSnapshot.getChildren()){

                                    if(idAnuncio.equals(anuncio.getValue().toString())){
                                        String anuncioKey = anuncio.getKey();
                                        instancia.getTabelaMeusAnuncios(usuarioKey).child(anuncioKey).removeValue();
                                    }
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

    public void setEndereco(String idAnuncio, String endereco){
        instancia.getTabelaAnuncio().child(idAnuncio).child("endereco").setValue(endereco);
    }

    public void setNumero(String idAnuncio, String numero){
        instancia.getTabelaAnuncio().child(idAnuncio).child("numero").setValue(Integer.parseInt(numero));
    }

    public void setComplemento(String idAnuncio, String complemento){
        instancia.getTabelaAnuncio().child(idAnuncio).child("complemento").setValue(complemento);
    }

    public void setCep(String idAnuncio, String cep){
        instancia.getTabelaAnuncio().child(idAnuncio).child("cep").setValue(cep);
    }

    public void setBairro(String idAnuncio, String bairro){
        instancia.getTabelaAnuncio().child(idAnuncio).child("bairro").setValue(bairro);
    }

    public void setCidade(String idAnuncio, String cidade){
        instancia.getTabelaAnuncio().child(idAnuncio).child("cidade").setValue(cidade);
    }

    public void setEstado(String idAnuncio, String estado){
        instancia.getTabelaAnuncio().child(idAnuncio).child("estado").setValue(estado);
    }

    public void setPrecoAluguel(String idAnuncio, String preco){
        instancia.getTabelaAnuncio().child(idAnuncio).child("precoAluguel").setValue(Double.parseDouble(preco));
    }

    public void setQuantidadeVagas(String idAnuncio, String vagas){
        instancia.getTabelaAnuncio().child(idAnuncio).child("qtdVagas").setValue(Integer.parseInt(vagas));
    }

    public void setInformacoesAdicionais(String idAnuncio, String informacoes){
        instancia.getTabelaAnuncio().child(idAnuncio).child("informacoesAdicionais").setValue(informacoes);
    }

    public void setLatitude(String idAnuncio, String latitude){
        instancia.getTabelaAnuncio().child(idAnuncio).child("latitude").setValue(Double.parseDouble(latitude));
    }

    public void setLongitude(String idAnuncio, String longitude){
        instancia.getTabelaAnuncio().child(idAnuncio).child("longitude").setValue(Double.parseDouble(longitude));
    }

    public void setImagemPrincipal(String idAnuncio, String urlImagemPrincipal){
        instancia.getTabelaAnuncio().child(idAnuncio).child("urlImagemPrincipal").setValue(urlImagemPrincipal);
    }

    public void setImagemDois(String idAnuncio, String urlImagemDois){
        instancia.getTabelaAnuncio().child(idAnuncio).child("urlImagemDois").setValue(urlImagemDois);
    }

    public void setImagemTres(String idAnuncio, String urlImagemTres){
        instancia.getTabelaAnuncio().child(idAnuncio).child("urlImagemTres").setValue(urlImagemTres);
    }

    public void setImagemQuatro(String idAnuncio, String urlImagemQuatro){
        instancia.getTabelaAnuncio().child(idAnuncio).child("urlImagemQuatro").setValue(urlImagemQuatro);
    }

    public void setImagemCinco(String idAnuncio, String urlImagemCinco){
        instancia.getTabelaAnuncio().child(idAnuncio).child("urlImagemCinco").setValue(urlImagemCinco);
    }

    public void removerImagem(String urlImagem) {

        StorageReference imagem = storage.getReferenceFromUrl(urlImagem);

        imagem.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("REMOVENDO", "onSuccess: deleted file");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("REMOVENDO", "onFailure: did not delete file");
            }
        });
    }
}
