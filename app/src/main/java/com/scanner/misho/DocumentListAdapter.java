package com.scanner.misho;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DocumentListAdapter extends RecyclerView.Adapter<DocumentListAdapter.MyViewHolder>{
    private final String TAG = getClass().getSimpleName();
    private ArrayList<Document> mDataset;
    private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener{
        void onListItemClick(int position);
    }
    

    // Provide a suitable constructor (depends on the kind of dataset)
    public DocumentListAdapter(ArrayList<Document> myDataset) {
        mDataset = myDataset;
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public int type;
        public ImageView documentTypeImage;
        public TextView fullName;
        public TextView documentNumber;
        public TextView createdAt;
        private ListItemClickListener mOnClickListener;
        private String TAG = getClass().getSimpleName();

        public MyViewHolder(View cardView) {
            super(cardView);
            this.documentTypeImage = (ImageView)cardView.findViewById(R.id.id_list_type);
            this.fullName = (TextView)cardView.findViewById(R.id.id_list_name);
            this.documentNumber = (TextView)cardView.findViewById(R.id.id_list_number);
            this.createdAt = (TextView)cardView.findViewById(R.id.id_list_created_date);
           cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            int position = this.getAdapterPosition();
            mOnClickListener = new ListItemClickListener() {
                @Override
                public void onListItemClick(int position) {
                    Log.d(TAG, Integer.toString(position));
                    //got to Document activity
                   Context mContext = v.getContext();
                    Intent intent = new Intent(mContext,DocumentActivity.class);
                    intent.putExtra("documentId",position+1);
                    mContext.startActivity(intent);

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
    public DocumentListAdapter.MyViewHolder onCreateViewHolder(final ViewGroup parent,
                                                               int viewType) {
        // create a new view
        final View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.document_card_item, parent, false);

        return new MyViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Document doc = mDataset.get(position);
        if(doc.getType()==Document.PASSPORT){
            holder.documentTypeImage.setImageResource(R.drawable.ic_passport);
        }
        else if(doc.getType()==Document.NATIONAL_ID){
            holder.documentTypeImage.setImageResource(R.drawable.ic_id_card);
        }
        else if(doc.getType()==Document.DRIVER_LICENSE){
            holder.documentTypeImage.setImageResource(R.drawable.ic_drivers_license);
        }
        holder.fullName.setText(doc.getGivennames().toUpperCase()+" "+doc.getSurname().toUpperCase());
        holder.documentNumber.setText(doc.getDocument_number().toUpperCase());
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
        holder.createdAt.setText(sdf.format(doc.getCreated_at()).toUpperCase());

    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addItem(ArrayList<Document> documents) {
        this.mDataset = documents;
        notifyItemInserted(0);
    }
}