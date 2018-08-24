package com.iseoulu.android.idogu.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

    private int selectionMode = UNSELECTION_MODE;

    private ArrayList<User> listOfFriend;

    public ListOfFriendAdapter(){
        listOfFriend = new ArrayList<>();
    }

    public void addItem(User friend){
        listOfFriend.add(friend);
        notifyDataSetChanged();
    }

    public void setSelectionMode(int selectionMode){
        this.selectionMode = selectionMode;
        notifyDataSetChanged(); // UI가 변동
    }

    public int getSelectionMode(){
        return this.selectionMode;
    }

    public int getSelectionUsersCount(){
        int selectedCount = 0;

        for(User user:listOfFriend){
            if(user.isSelection()){
                selectedCount++;
            }
        }
        return selectedCount;
    }

    public String [] getSelectedUids(){
       String [] selecedUids = new String[getSelectionUsersCount()];
        int i=0;

        for(User user:listOfFriend){
            if(user.isSelection()){
                selecedUids[i++]=user.getUid();
            }
        }
        return selecedUids;
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

        if(getSelectionMode() == UNSELECTION_MODE){
            holder.friendSelectedView.setVisibility(View.GONE);
        }else{
            holder.friendSelectedView.setVisibility(View.VISIBLE);
        }

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

        @BindView(R.id.checkbox)
        CheckBox friendSelectedView;

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
