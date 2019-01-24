package com.example.lei29.myapp1;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    private FirebaseFirestore firebaseFirestore;
    public Context context;
    private String user_image_url;

    private String user_name;





    public BlogRecyclerAdapter(List<BlogPost> blog_list){
        this.blog_list = blog_list;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_list_item, viewGroup, false);
        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        String desc_data = blog_list.get(i).getDesc();

        viewHolder.setDescText(desc_data);
        String image_url = blog_list.get(i).getImage_url();
        String thumb_image_url = blog_list.get(i).getThumb_url();
        viewHolder.setBlogImage(image_url, thumb_image_url);


        String user_id = blog_list.get(i).getUser_id();
        System.out.println("---------------"+user_id);

        DocumentReference docRef = firebaseFirestore.collection("Users").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        user_image_url = document.getString("image");
                        System.out.println(user_image_url);
                        user_name = document.getString("name");
                        viewHolder.setUsername(user_name);
                        System.out.println("----------------"+user_name);
                        viewHolder.setUserImage(user_image_url);

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        long millisecond = blog_list.get(i).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy HH:mm:ss", new Date(millisecond)).toString();
        viewHolder.setDate(dateString);



    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mview;
        private TextView descView;
        private TextView usernameView;
        private ImageView blogImageView;



        public ViewHolder(View itemView){
            super(itemView);
            mview = itemView;

        }

        public void setDescText(String descText){
            descView = mview.findViewById(R.id.post_desc);
            //System.out.println("--------------"+descView);
            descView.setText(descText);
        }

        public void setBlogImage(String image_url, String thumbUri){
            blogImageView = mview.findViewById(R.id.post_image);
            Glide.with(context).load(image_url).thumbnail(
                    Glide.with(context).load(thumbUri)).into(blogImageView);
        }

        public void setUserImage(String image_url){
            blogImageView = mview.findViewById(R.id.post_user_image);
            Glide.with(context).load(image_url).into(blogImageView);
        }

        public void setUsername(String user_name){

            usernameView = mview.findViewById(R.id.post_username);
            System.out.println("--------------==============="+user_name);
            usernameView.setText(user_name);
        }

        public void setDate(String date){

            usernameView = mview.findViewById(R.id.post_date);
            usernameView.setText(date);
        }

    }
}
