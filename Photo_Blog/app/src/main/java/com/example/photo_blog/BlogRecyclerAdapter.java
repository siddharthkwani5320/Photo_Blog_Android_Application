package com.example.photo_blog;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {
    Context context;
    public ArrayList<BlogPost> blog_list;

    private FirebaseFirestore fstore;

    public BlogRecyclerAdapter(Context context, ArrayList<BlogPost> blog_list) {
        this.context = context;
        this.blog_list = blog_list;
    }

    @Override
    public BlogRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.blog_card,parent,false);
        fstore=FirebaseFirestore.getInstance();
        return new ViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {



        BlogPost post=blog_list.get(position);
        Glide.with(context).load(post.getImageUrl()).into(holder.postImage);
        holder.descView.setText(post.getDescription());


        fstore.collection("Users").document(post.getUserId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String userName=task.getResult().getString("name");
                    String userImage=task.getResult().getString("image");
                    holder.userName.setText(userName);
                    Glide.with(context).load(userImage).into(holder.userImage);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descView;
        private TextView userName;
        private CircleImageView userImage;
        private ImageView postImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            descView=itemView.findViewById(R.id.post_desc);
            userName=itemView.findViewById(R.id.username);
            userImage=itemView.findViewById(R.id.userImage);
            postImage=itemView.findViewById(R.id.postImage);
        }
    }
}
