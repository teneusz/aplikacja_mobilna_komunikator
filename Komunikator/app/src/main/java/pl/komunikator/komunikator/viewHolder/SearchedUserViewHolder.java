package pl.komunikator.komunikator.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import pl.komunikator.komunikator.R;

/**
 * Created by adrian on 22.05.2017.
 */
public class SearchedUserViewHolder extends RecyclerView.ViewHolder {

    public TextView nameTextView, emailTextView;
    public ImageView avatarImageView;
    public ImageButton inviteImageButton;

    public SearchedUserViewHolder(View view) {
        super(view);

        nameTextView = (TextView) view.findViewById(R.id.contactName);
        emailTextView = (TextView) view.findViewById(R.id.contactEmail);
        avatarImageView = (ImageView) view.findViewById(R.id.contactImageView);
        inviteImageButton = (ImageButton) view.findViewById(R.id.addFriendImageButton);
    }

}
