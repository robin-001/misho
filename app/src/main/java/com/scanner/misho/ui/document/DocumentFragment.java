package com.scanner.misho.ui.document;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.scanner.misho.Document;
import com.scanner.misho.R;
import com.scanner.misho.SQLiteDocumentDatabaseHandler;

public class DocumentFragment extends Fragment {

    private DocumentViewModel documentViewModel;
    private SQLiteDocumentDatabaseHandler db;
    private Document document;

    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
  //      documentViewModel = ViewModelProviders.of(this).get(DocumentViewModel.class);
        View root = inflater.inflate(R.layout.fragment_document, container, false);

        //final TextView textView = root.findViewById(R.id.text_slideshow);
       // final TextView documentNumber = root.findViewById(R.id.document_number);

        return root;
    }
}