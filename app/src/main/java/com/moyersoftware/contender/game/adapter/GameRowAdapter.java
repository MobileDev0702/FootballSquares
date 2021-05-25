package com.moyersoftware.contender.game.adapter;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.util.MyApplication;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class GameRowAdapter extends RecyclerView.Adapter<GameRowAdapter.ViewHolder> {

    // Usual variables
    private final ArrayList<Integer> mNumbers;
    private Boolean mLive = false;
    private int mHeight;
    private final int mDefaultCellSize;
    private final boolean mRow;
    private final Context mContext;
    private final static String mAlphabet = "abcdefghijklmnopqrstuvwxyz";

    public GameRowAdapter(Context context, ArrayList<Integer> numbers, boolean row) {
        mContext = context;
        mNumbers = numbers;
        mHeight = Util.getCellSize();
        mDefaultCellSize = (int) MyApplication.getContext().getResources().getDimension
                (R.dimen.board_cell_size);
        mRow = row;
    }

    public void setLive(Boolean live) {
        mLive = live;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_game_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.getLayoutParams().height = mHeight;
        //noinspection SuspiciousNameCombination
        holder.itemView.getLayoutParams().width = mHeight;

        if (mLive) {
            ((TextView) holder.itemView).setText(String.valueOf(mNumbers.get(position)));
        } else {
            ((TextView) holder.itemView).setText(mRow ? String.valueOf(position + 1)
                    : String.valueOf(mAlphabet.charAt(position)).toUpperCase());
        }
        ((TextView) holder.itemView).setTextColor(ContextCompat.getColor(mContext, mLive
                ? R.color.white : R.color.text_inactive));
        ((TextView) holder.itemView).setTextSize(TypedValue.COMPLEX_UNIT_SP,
                16 * mHeight / mDefaultCellSize);

    }

    @Override
    public int getItemCount() {
        return mNumbers.size();
    }

    public void scale(int height) {
        mHeight = height;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}