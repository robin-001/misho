package com.scanner.misho;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DocumentActivity extends AppCompatActivity {


    private SQLiteDocumentDatabaseHandler db;
    private int documentId;
    FieldListAdapter documentAdapter;
    public static LinearLayoutManager layoutManager;
    RecyclerView documentDetails;
    private Document document;
    private static final String TAG = "DocumentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Saving...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        documentId = getIntent().getIntExtra("documentId",1);

        db = new SQLiteDocumentDatabaseHandler(this);
        try {
            document = db.getDocument(documentId);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG,document.toString());
        setTitle(document.getGivennames()+" "+document.getSurname());

        ArrayList<DocumentField> fields = new ArrayList<DocumentField>();

        //populate the fields based on available values.
        fields.add(new DocumentField("TYPE",document.getReadableType()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (document.getDocument_number() != null) {
            fields.add(new DocumentField("DOCUMENT NUMBER", document.getDocument_number()));
        }
        if (document.getIdentification_number() != null) {
            fields.add(new DocumentField("IDENTIFICATION NUMBER", document.getIdentification_number()));
        }
        if (document.getSurname() != null) {
            fields.add(new DocumentField("SURNAME", document.getSurname()));
        }
        if (document.getFirstname() != null) {
            fields.add(new DocumentField("FIRSTNAME", document.getFirstname()));
        }
        if (document.getGivennames() != null) {
            fields.add(new DocumentField("GIVEN NAMES", document.getGivennames()));
        }
        if (document.getGender() != null) {
            fields.add(new DocumentField("GENDER", document.getGender()));
        }
        if (document.getDob() != null) {
            fields.add(new DocumentField("DATE OF BIRTH", sdf.format(document.getDob())));
        }
        if (document.getIssuing_date() != null) {
            fields.add(new DocumentField("ISSUING DATE", sdf.format(document.getIssuing_date())));
        }
        if (document.getExpiry_date() != null) {
            fields.add(new DocumentField("EXPIRY DATE", sdf.format(document.getExpiry_date())));
        }
        if (document.getIssuing_authority() != null) {
            fields.add(new DocumentField("ISSUING AUTHORITY", document.getIssuing_authority()));
        }
        if (document.getNationality() != null) {
            fields.add(new DocumentField("NATIONALITY", document.getNationality()));
        }


        documentAdapter = new FieldListAdapter(fields);
        documentDetails = (RecyclerView) findViewById(R.id.document_detail);
        documentDetails.setHasFixedSize(true);
        documentDetails.setItemAnimator(new DefaultItemAnimator());

        layoutManager = new LinearLayoutManager(this) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return null;
            }
        };
        documentDetails.setLayoutManager(layoutManager);

        documentDetails.setAdapter(documentAdapter);

    }
}