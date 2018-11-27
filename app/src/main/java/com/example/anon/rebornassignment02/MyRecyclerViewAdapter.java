package com.example.anon.rebornassignment02;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter
        .MyRecyclerViewHolder> {
    private List<MyData> myDatas;
    int layout;

    public MyRecyclerViewAdapter(List<MyData> myDatas, int layout) {
        this.myDatas = myDatas;
        this.layout = layout;
    }

    @NonNull
    @Override
    public MyRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(layout, viewGroup, false);
        return new MyRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyRecyclerViewHolder myRecyclerViewHolder, int i) {
        final MyData myData = myDatas.get(i);
        myRecyclerViewHolder.itemImage.setImageURI(myData.getUri());
        myRecyclerViewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDatas.remove(myRecyclerViewHolder.getAdapterPosition());
                notifyItemRemoved(myRecyclerViewHolder.getAdapterPosition());
                notifyItemRangeChanged(
                        myRecyclerViewHolder.getAdapterPosition(),
                        myDatas.size());
            }
        });
    }


    @Override
    public int getItemCount() {
        return myDatas.size();
    }

    public class MyRecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        Button btnRemove;

        public MyRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
