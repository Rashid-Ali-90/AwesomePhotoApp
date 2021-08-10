package com.awesome.photo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageListHolder> {

        private Context context;
        private List<ImageListModel> imageListModel;

        public ImageListAdapter(Context context, List<ImageListModel> imageListModel)
        {
            this.context = context;
            this.imageListModel = imageListModel;
        }

        @NonNull
        @Override
        public ImageListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.adapter_image_item, parent, false);
            return new ImageListHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageListHolder holder, int position)
        {
            ImageListModel imageList = imageListModel.get(position);

                Glide.with(context)
                    .load(imageList.getImageURL())
                    .thumbnail(Glide.with(context).load(R.drawable.loading))
                    .centerCrop()
                    .into(holder.ivImageHolder);

            holder.tvAuthorName.setText(imageList.getAuthorName());
            holder.tvDescription.setText(imageList.getDescription());

            holder.ivImageHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    openImageUrl(imageList.getImageURL());
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return imageListModel.size();
        }

        public class ImageListHolder extends RecyclerView.ViewHolder
        {
            @BindView(R.id.iv_image_holder)
            ImageView ivImageHolder;
            @BindView(R.id.tv_author_name)
            TextView tvAuthorName;
            @BindView(R.id.tv_description)
            TextView tvDescription;

            public ImageListHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        void openImageUrl(String url)
        {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(url));
            context.startActivity(browserIntent);
        }
}