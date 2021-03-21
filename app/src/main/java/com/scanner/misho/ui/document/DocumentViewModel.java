package com.scanner.misho.ui.document;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.scanner.misho.Document;

public class DocumentViewModel extends ViewModel {

    private MutableLiveData<Document> mDocument;

    public DocumentViewModel(Document document) {
        mDocument = new MutableLiveData<>();
        mDocument.setValue(document);
    }

    public LiveData<Document> getDocument() {
        return mDocument;
    }
}