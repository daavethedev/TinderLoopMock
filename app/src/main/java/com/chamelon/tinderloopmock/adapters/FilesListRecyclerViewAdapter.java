package com.chamelon.tinderloopmock.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.chamelon.tinderloopmock.R;

import java.util.List;

public class FilesListRecyclerViewAdapter extends RecyclerView.Adapter<FilesListRecyclerViewAdapter.ViewHolderList> {

    private List<Uri> uriList;
    private Context context;
    private OnItemClickedListener onItemClickedListener;

    public interface OnItemClickedListener {

        void itemClicked(Uri fileUri);
    }

    public FilesListRecyclerViewAdapter(List<Uri> uriList, Context context) {
        this.uriList = uriList;
        this.context = context;
        onItemClickedListener = (OnItemClickedListener) this;
    }

    @NonNull
    @Override
    public ViewHolderList onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_video_file, viewGroup, false);
        return new ViewHolderList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderList viewHolderList, int i) {

        viewHolderList.ivThumbnail.setImageBitmap(getThumbnailBitmap(uriList.get(i)));

    }

    private Bitmap getThumbnailBitmap(Uri fileUri) {

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(fileUri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        return ThumbnailUtils.createVideoThumbnail(picturePath, MediaStore.Video.Thumbnails.MICRO_KIND);

    }

    @Override
    public int getItemCount() {
        return uriList.size();
    }

    public class ViewHolderList extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView ivThumbnail;
        private ImageButton ibPlayVideo;

        public ViewHolderList(@NonNull View itemView) {
            super(itemView);

            ivThumbnail = itemView.findViewById(R.id.iv_image_icon);
            ibPlayVideo = itemView.findViewById(R.id.ib_play_video);

            ibPlayVideo.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            if (v == ibPlayVideo) {

                onItemClickedListener.itemClicked(uriList.get(getAdapterPosition()));

            }
        }
    }
}
