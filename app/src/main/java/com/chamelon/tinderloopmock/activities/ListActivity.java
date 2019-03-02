package com.chamelon.tinderloopmock.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.chamelon.tinderloopmock.R;
import com.chamelon.tinderloopmock.adapters.FilesListRecyclerViewAdapter;
import com.chamelon.tinderloopmock.info.Info;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity implements Info, FilesListRecyclerViewAdapter.OnItemClickedListener {

    private List<Uri> filesUri;
    private RecyclerView rvFiles;
    private TextView tvFilesCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        rvFiles = findViewById(R.id.rv_files);
        tvFilesCount = findViewById(R.id.tv_files_count);

        filesUri = new ArrayList<>();
        getUriList();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        FilesListRecyclerViewAdapter adapter = new FilesListRecyclerViewAdapter(filesUri, this);
        rvFiles.setAdapter(adapter);
        rvFiles.setLayoutManager(gridLayoutManager);
        adapter.notifyDataSetChanged();

        tvFilesCount.setText("Files(" + filesUri.size() + ")");
    }

    private void getUriList() {

        String path = Environment.getExternalStorageDirectory().toString() + "/MockTinderLoop";

        File directory = new File(path);
        File[] files = directory.listFiles();

        for (int i = 0; i < files.length; i++) {

            Uri fileUri = Uri.parse(files[i].getAbsolutePath());

            if (files[i].getName().contains("TinderLoop")) {

                filesUri.add(fileUri);
            }

            Log.v(TAG, "FileName:" + files[i].getName());

        }
    }

    @Override
    public void itemClicked(Uri fileUri) {

        Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
        intent.setDataAndType(fileUri, "video/mp4");
        startActivity(intent);
    }
}
