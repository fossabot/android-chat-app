package com.iseoulu.android.idogu.views;


import android.content.Intent;
import android.os.Bundle;
import android.os.MemoryFile;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iseoulu.android.idogu.R;
import com.iseoulu.android.idogu.adapters.ListOfChatAdapter;
import com.iseoulu.android.idogu.adapters.ListOfFriendAdapter;
import com.iseoulu.android.idogu.customviews.RecyclerViewItemClickListener;
import com.iseoulu.android.idogu.models.Chat;
import com.iseoulu.android.idogu.models.User;

import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private FirebaseUser mFirebaseUser;

    private FirebaseDatabase mFirebaseDatase;

    private DatabaseReference mChatRef;

    private DatabaseReference mChatMemberRef;

    @BindView(R.id.chatRecylerView)
    RecyclerView mChatRecyclerView;

    private ListOfChatAdapter mListOfChatAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View chatView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, chatView);

        // 채팅방 리스너 부착
        // users/{나의 uid}/chats/
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatase = FirebaseDatabase.getInstance();
        mChatRef = mFirebaseDatase.getReference("users").child(mFirebaseUser.getUid()).child("chats");
        mChatMemberRef = mFirebaseDatase.getReference("chat_members");
        mListOfChatAdapter = new ListOfChatAdapter();
        mChatRecyclerView.setAdapter(mListOfChatAdapter);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mChatRecyclerView.addOnItemTouchListener( new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Chat chat = mListOfChatAdapter.getItem(position);
                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra("chat_id", chat.getChatId());
                startActivity(chatIntent);
            }
        }));
        addChatListener();
        return chatView;
    }


    private void addChatListener() {
        mChatRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(final DataSnapshot chatDataSnapshot, String s) {

                // ui 갱신 시켜주는 메서드로 방의 정보를 전달.

                // 방에 대한 정보를 얻어오고
                final Chat chatRoom = chatDataSnapshot.getValue(Chat.class);
                mChatMemberRef.child(chatRoom.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long memberCount = dataSnapshot.getChildrenCount();
                        Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();
                        StringBuffer memberStringBuffer = new StringBuffer();

                        int loopCount = 1;
                        while( memberIterator.hasNext()) {
                            User member = memberIterator.next().getValue(User.class);
                            if ( !mFirebaseUser.getUid().equals(member.getUid())) {
                                memberStringBuffer.append(member.getName());
                                if ( memberCount - loopCount > 1 ) {
                                    memberStringBuffer.append(", ");
                                }
                            }
                            if ( loopCount == memberCount ) {
                                // users/uid/chats/{chat_id}/title
                                String title = memberStringBuffer.toString();
                                if ( chatRoom.getTitle() == null ) {
                                    chatDataSnapshot.getRef().child("title").setValue(title);
                                } else if (!chatRoom.getTitle().equals(title)){
                                    chatDataSnapshot.getRef().child("title").setValue(title);
                                }
                                chatRoom.setTitle(title);
                                drawUI( chatRoom );
                            }
                            loopCount++;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                // 기존의 방제목과 방 멤버의 이름들을 가져와서 타이틀화 시켰을때 같지 않은 경우 방제목을 업데이트 시켜줍니다.
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // 변경된 방의 정보를 수신
                // 나의 내가 보낸 메시지가 아닌경우와 마지막 메세지가 수정이 되었다면 -> 노티출력

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void drawUI(Chat chat){
        mListOfChatAdapter.addItem(chat);
    }

}
