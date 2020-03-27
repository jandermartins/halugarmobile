package br.ufc.crateus.halugar.Activities.Anuncio;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InformacoesActivity extends ViewModel {

    private MutableLiveData<String> mText;

    public InformacoesActivity() {
        mText = new MutableLiveData<>();
        mText.setValue("This is INFORMAÇÕES fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
