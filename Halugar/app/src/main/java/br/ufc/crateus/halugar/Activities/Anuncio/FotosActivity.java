package br.ufc.crateus.halugar.Activities.Anuncio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.os.Bundle;

import br.ufc.crateus.halugar.R;

public class FotosActivity extends ViewModel {

    private MutableLiveData<String> mText;

    public FotosActivity() {
        mText = new MutableLiveData<>();
        mText.setValue("This is FOTO fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
