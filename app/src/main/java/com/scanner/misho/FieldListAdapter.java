package com.scanner.misho;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FieldListAdapter extends RecyclerView.Adapter<FieldListAdapter.FieldViewHolder>{
    private final String TAG = getClass().getSimpleName();
    private ArrayList<DocumentField> mDataset;
    private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener{
        void onListItemClick(int position);
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public FieldListAdapter(ArrayList<DocumentField> myDataset) {
        mDataset = myDataset;
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class FieldViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView fieldLabel;
        public TextView fieldValue;
        private ListItemClickListener mOnClickListener;
        private String TAG = getClass().getSimpleName();

        public FieldViewHolder(View cardView) {
            super(cardView);
            this.fieldLabel = (TextView)cardView.findViewById(R.id.field_label);
            this.fieldValue = (TextView)cardView.findViewById(R.id.field_value);
           cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            int position = this.getAdapterPosition();
            mOnClickListener = new ListItemClickListener() {
                @Override
                public void onListItemClick(int position) {
                    Log.d(TAG, Integer.toString(position));
                }
            };
           mOnClickListener.onListItemClick(position);
        }
    }

    public void setOnItemClickListener(ListItemClickListener myClickListener) {
        this.mOnClickListener = myClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FieldListAdapter.FieldViewHolder onCreateViewHolder(final ViewGroup parent,
                                                            int viewType) {
        // create a new view
        final View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.document_field, parent, false);

        return new FieldViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FieldViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        DocumentField field = mDataset.get(position);
        holder.fieldLabel.setText(field.getLabel().toUpperCase());
        holder.fieldValue.setText(field.getValue().toUpperCase());

    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addItem(DocumentField fields) {
        this.mDataset.add(fields);
        notifyItemInserted(0);
    }
}