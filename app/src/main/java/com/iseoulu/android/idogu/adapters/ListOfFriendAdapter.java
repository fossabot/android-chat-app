package com.iseoulu.android.idogu.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iseoulu.android.idogu.R;
import com.iseoulu.android.idogu.customviews.RoundedImageView;
import com.iseoulu.android.idogu.models.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListOfFriendAdapter extends RecyclerView.Adapter<ListOfFriendAdapter.FriendHolder> {

    public static final int UNSELECTION_MODE = 1;
    public static final int SELECTION_MODE = 2;

    private ArrayList<User> listOfFriend;

    public ListOfFriendAdapter(){
        listOfFriend = new ArrayList<>();
    }

    public void addItem(User friend){
        listOfFriend.add(friend);
        notifyDataSetChanged();
    }

    public User getItem(int position){
        return this.listOfFriend.get(position);
    }


    @Override
    public FriendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friend_item,parent,false);
        FriendHolder friendHolder = new FriendHolder(view);
        return friendHolder;
    }

    @Override
    public void onBindViewHolder(FriendHolder holder, int position) {
        User friend = getItem(position);
        holder.mEmailView.setText(friend.getEmail());
        holder.mNameView.setText(friend.getName());

        if(friend.getProfileUrl() != null) {
            Glide.with(holder.itemView)
                    .load(friend.getProfileUrl())
                    .into(holder.mProfileView);
        }

    }

    @Override
    public int getItemCount() {
        return listOfFriend.size();
    }

    public static class FriendHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.thumb)
        RoundedImageView mProfileView;

        @BindView(R.id.name)
        TextView mNameView;

        @BindView(R.id.email)
        TextView mEmailView;

        private FriendHolder(View view){
            super(view);

            ButterKnife.bind(this,view);
        }
    }
}
