package com.iseoulu.android.idogu.views;


import android.content.Intent;
import android.os.Bundle;
import android.os.MemoryFile;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.iseoulu.android.idogu.models.Message;
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

    public static String joinChatId = "";
    public static final int JOIN_ROOM_REQUEST_CODE = 100;

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
        mListOfChatAdapter.setFragment(this); // 지금 현재 fragment
        mChatRecyclerView.setAdapter(mListOfChatAdapter);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mChatRecyclerView.addOnItemTouchListener( new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Chat chat = mListOfChatAdapter.getItem(position);
                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra("chat_id", chat.getChatId());
                joinChatId = chat.getChatId();
                startActivityForResult(chatIntent, JOIN_ROOM_REQUEST_CODE);
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
                // 나의 내가 보낸 메시지가 아닌경우와 마지막 메세지가 수정이 되었다면 -> 노티출력

                // 변경된 방의 정보를 수신
                // 변경된 포지션 확인, ( 채팅방 아이디로 기존의 포지션을 확인 합니다)
                // 그 포지션의 아이템 중 unreadCount가 변경이 되었다면 unreadCount 변경
                // lastMessage 입니다. last 메세지의 시각과 변경된 메세지의 last 메세지 시간이 다르다면 -> 노티피케이션 출력해줍니다.
                // 현재 액티비티가 ChatActivity 이고 chat_id 가 같다면 노티는 해주지 않습니다.

                Chat updatedChat = dataSnapshot.getValue(Chat.class); // 채팅방의 정보를 가져올 수 있음

                Chat oldChat = mListOfChatAdapter.getItem(updatedChat.getChatId());

                if ( updatedChat.getLastMessage() == null || oldChat.getLastMessage() == null) {
                    return;
                }
                mListOfChatAdapter.updateItem(updatedChat);

                if(!updatedChat.getLastMessage().getMessageUser().getUid().equals(mFirebaseUser.getUid())){
                    if(updatedChat.getLastMessage().getMessageDate().getTime() > oldChat.getLastMessage().getMessageDate().getTime()){
                        if(!updatedChat.getChatId().equals(joinChatId)){
                            // 노티피케이션 알림
                            Toast.makeText(getActivity(), updatedChat.getLastMessage().getMessageText(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // 방의 실시간 삭제
                Chat item = dataSnapshot.getValue(Chat.class); // 지워질 방의 객체를 얻어옴
                mListOfChatAdapter.removeItem(item);
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

    // 나갈 방의 인자를 같이 받음
    public void leaveChat(final Chat chat){
        // 대화상자를 열어줌
        Snackbar.make(getView(),"선택한 대화방을 나가시겠습니까?", Snackbar.LENGTH_LONG).setAction("예", new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 나의 대화방 목록에서 제거
                // users/{uid}/chats
                mChatRef.child(chat.getChatId()).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        // 채팅 멤버 목록에서 제거
                        // chat_members/{chat_id}/{user_id} 제거
                        mChatMemberRef.child(chat.getChatId())
                                .child(mFirebaseUser.getUid())
                                .removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                // 메세지 unreadCount 에서도 제거
                                // getReadUserList 내가 있다면 읽어진거니깐 pass
                                // 없다면 unreadCount -1
                                // messages/{chat_id}
                                // 모든 메세지를 가져온다.
                                // 가져와서 루프를 통해서 내가 읽었는지 여부 판단

                                mFirebaseDatase.getReference("messages").child(chat.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterator<DataSnapshot> messageIterator = dataSnapshot.getChildren().iterator();

                                        while(messageIterator.hasNext()){
                                            DataSnapshot messageSnapshot = messageIterator.next();
                                            Message currentMessage = messageSnapshot.getValue(Message.class);
                                            if(currentMessage.getReadUserList().contains(mFirebaseUser.getUid())){
                                                // message
                                                messageSnapshot.child("unreadCount").getRef().setValue(currentMessage.getUnreadCount() -1);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }
                });
            }
        }).show();



        }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == JOIN_ROOM_REQUEST_CODE){
            joinChatId = "";
        }

    }

}
