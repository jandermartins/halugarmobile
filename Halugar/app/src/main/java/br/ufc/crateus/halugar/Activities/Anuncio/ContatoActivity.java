package br.ufc.crateus.halugar.Activities.Anuncio;

import android.widget.ImageButton;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ContatoActivity extends ViewModel {

    private MutableLiveData<String> mText;

    public ContatoActivity() {
        mText = new MutableLiveData<>();
        mText.setValue("This is CONTATO fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
