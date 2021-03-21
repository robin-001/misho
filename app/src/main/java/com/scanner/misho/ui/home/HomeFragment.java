package com.scanner.misho.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.scanner.misho.Document;
import com.scanner.misho.DocumentListAdapter;
import com.scanner.misho.MainActivity;
import com.scanner.misho.R;

public class HomeFragment extends Fragment implements DocumentListAdapter.ListItemClickListener{


    private static final String TAG = "HomeFragment" ;
    private HomeViewModel homeViewModel;
    //sample data

    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        MainActivity.layoutManager = new LinearLayoutManager(getContext());
        MainActivity.layoutManager.setReverseLayout(true);
        MainActivity.layoutManager.setStackFromEnd(true);
        MainActivity.documentList = root.findViewById(R.id.document_list);
        MainActivity.documentList.setHasFixedSize(true);
        MainActivity.documentList.setItemAnimator(new DefaultItemAnimator());

        MainActivity.documentList.setLayoutManager(MainActivity.layoutManager);
        MainActivity.documentList.setAdapter(MainActivity.adapter);


        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //barcode_value.setText(s); //change this to recyclerview
            }
        });
        return root;
    }

    @Override
    public void onListItemClick(int position) {
       Document doc = MainActivity.documents.get(position);
        Log.d(TAG,Integer.toString(position));
        Toast.makeText(getContext()
                , doc.getSurname(), Toast.LENGTH_SHORT).show();
    }
}