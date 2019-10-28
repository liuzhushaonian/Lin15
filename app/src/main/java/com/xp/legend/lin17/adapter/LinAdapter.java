package com.xp.legend.lin17.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.xp.legend.lin17.R;
import com.xp.legend.lin17.bean.Result;
import com.xp.legend.lin17.hooks.SlitImageView;
import com.xp.legend.lin17.utils.LinApp;

import java.util.ArrayList;
import java.util.List;

public class LinAdapter extends BaseAdapter<LinAdapter.ViewHolder> {

    private List<Result> resultList=new ArrayList<>();

    private OnItemClick itemClick;

    private int width,height;

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public LinAdapter() {

        width= LinApp.getContext().getSharedPreferences("lin17_sh", Context.MODE_PRIVATE).getInt("shu_width",0);

        height=LinApp.getContext().getSharedPreferences("lin17_sh", Context.MODE_PRIVATE).getInt("shu_height",0);
    }

    public void setItemClick(OnItemClick itemClick) {
        this.itemClick = itemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.lin_item,parent,false);

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(view1 -> {
            if (itemClick!=null){
                int p=viewHolder.getAdapterPosition();
                Result r=resultList.get(p);
                itemClick.itemClick(p,r);
            }
        });

        viewHolder.view.setOnLongClickListener(view1 -> {

            if (itemClick!=null){

                int p=viewHolder.getAdapterPosition();
                Result r=resultList.get(p);
                itemClick.itemLongClick(p,r);

                return true;
            }

            return false;
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Result r=resultList.get(position);

        if (!r.getShuHeaderFile().isEmpty()){

            Glide.with(holder.view)
                    .asBitmap()
                    .load(r.getShuHeaderFile())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.book.setBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });

        }else if (!r.getShuFile().isEmpty()){

            Glide.with(holder.view)
                    .asBitmap()
                    .load(r.getShuFile())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.book.setBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }

    }

    @Override
    public int getItemCount() {

        return resultList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        SlitImageView book;
        View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view=itemView;
            this.book=itemView.findViewById(R.id.book);
            reDraw();
        }


        private void reDraw(){




            if (width>0&&height>0){

                RecyclerView.LayoutParams layoutParams= (RecyclerView.LayoutParams) view.getLayoutParams();

                layoutParams.width=width;
                layoutParams.height=height;

                view.setLayoutParams(layoutParams);

            }

        }


    }


    public void remove(Result result){

        resultList.remove(result);

    }

    public void addItem(Result result){

        resultList.add(result);

        notifyDataSetChanged();

    }

    public void addItems(List<Result> results){

        resultList.addAll(results);

        notifyDataSetChanged();

    }


}
