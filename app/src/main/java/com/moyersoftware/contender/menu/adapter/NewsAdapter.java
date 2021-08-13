package com.moyersoftware.contender.menu.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<String> imageList;
    private ArrayList<String> titleList;
    private ArrayList<String> dateList;
    private ArrayList<String> authorList;
    private ArrayList<String> descList;
    private ArrayList<String> urlList;

    public NewsAdapter(Context ctx, ArrayList<String> images, ArrayList<String> titles, ArrayList<String> dates,
                       ArrayList<String> authors, ArrayList<String> descs, ArrayList<String> urls) {
        inflater = LayoutInflater.from(ctx);
        this.context = ctx;
        this.imageList = images;
        this.titleList = titles;
        this.dateList = dates;
        this.authorList = authors;
        this.descList = descs;
        this.urlList = urls;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_news, parent, false);
        NewsAdapter.MyViewHolder holder = new NewsAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        Picasso.get().load(imageList.get(position)).into(holder.articleImage);
        holder.titleText.setText(titleList.get(position));
        //---------------------
        String strDate = dateList.get(position);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(strDate);
            SimpleDateFormat sdfnewformat = new SimpleDateFormat("MMM d, yyyy");
            String finalDateString = sdfnewformat.format(convertedDate);
            holder.dateText.setText(finalDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //----------------
        holder.authorText.setText(authorList.get(position));
        holder.descText.setText(descList.get(position));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(urlList.get(position)));
                context.startActivity(browse);
            }
        });
    }

    @Override
    public int getItemCount() {
        return titleList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView articleImage;
        TextView titleText;
        TextView dateText;
        TextView authorText;
        TextView descText;

        public MyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            articleImage = itemView.findViewById(R.id.Article_img);
            titleText = itemView.findViewById(R.id.Title_txt);
            dateText = itemView.findViewById(R.id.Date_txt);
            authorText = itemView.findViewById(R.id.Author_txt);
            descText = itemView.findViewById(R.id.Desc_txt);
        }
    }
}
