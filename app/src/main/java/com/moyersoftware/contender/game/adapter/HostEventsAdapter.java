package com.moyersoftware.contender.game.adapter;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.HostActivity;
import com.moyersoftware.contender.game.data.Event;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.moyersoftware.contender.R.id.event_home_img;

public class HostEventsAdapter extends RecyclerView.Adapter<HostEventsAdapter.ViewHolder> {

    // Constants
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_DATE = 1;
    public static final int TYPE_ITEM = 2;

    // Usual variables
    private final HostActivity mActivity;
    private final ArrayList<Event> mEvents;

    public HostEventsAdapter(HostActivity activity, ArrayList<Event> events) {
        mActivity = activity;
        mEvents = events;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType
                == TYPE_ITEM ? R.layout.item_event : (viewType == TYPE_HEADER
                ? R.layout.item_event_header : R.layout.item_event_date), parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return mEvents.get(position).getType();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Event event = mEvents.get(position);

        if (getItemViewType(position) == TYPE_ITEM) {
            assert holder.homeNameTxt != null;
            assert holder.timeTxt != null;

            holder.awayNameTxt.setText(event.getTeamAway().getName());
            //Picasso.get().load(event.getTeamAway().getImage()).into(holder.awayImg);
            Picasso.get().load("file:///android_asset/teams/"+event.getTeamAway().getImage().toLowerCase()+".png").into(holder.awayImg);

            holder.homeNameTxt.setText(event.getTeamHome().getName());

            Picasso.get()
                    .load("file:///android_asset/teams/"+event.getTeamHome().getImage().toLowerCase()+".png").networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.homeImg, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            //Try again online if cache failed
                            Picasso.get()
                                    .load("file:///android_asset/teams/"+event.getTeamHome().getImage().toLowerCase()+".png")
                                    .error(R.color.red)
                                    .into(holder.homeImg, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Log.v("Picasso", "Could not fetch image");
                                        }
                                    });
                        }
                    });

            holder.timeTxt.setText(Util.formatTime(event.getTime()));
        } else if (getItemViewType(position) == TYPE_DATE) {
            holder.awayNameTxt.setText(Util.formatDate(event.getTime()));
        } else {
            holder.awayNameTxt.setText(event.getWeek());
        }
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Nullable
        @BindView(R.id.event_away_img)
        ImageView awayImg;
        @BindView(R.id.event_away_name_txt)
        TextView awayNameTxt;
        @Nullable
        @BindView(event_home_img)
        ImageView homeImg;
        @Nullable
        @BindView(R.id.event_home_name_txt)
        TextView homeNameTxt;
        @Nullable
        @BindView(R.id.event_time_txt)
        TextView timeTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mActivity.setSelectedEvent(mEvents.get(getAdapterPosition()));
        }
    }
}

