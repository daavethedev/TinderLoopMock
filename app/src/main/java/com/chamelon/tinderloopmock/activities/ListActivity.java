package com.chamelon.tinderloopmock.activities;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.chamelon.tinderloopmock.R;
import com.chamelon.tinderloopmock.adapters.FilesListRecyclerViewAdapter;
import com.chamelon.tinderloopmock.info.Info;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity implements Info, FilesListRecyclerViewAdapter.OnItemClickedListener {

    private List<Uri> filesUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        filesUri = new ArrayList<>();
        getUriList();

        FilesListRecyclerViewAdapter adapter = new FilesListRecyclerViewAdapter(filesUri, this);

    }

    private void getUriList() {

        String path = Environment.getExternalStorageDirectory().toString() + "/MockTinderLoop";

        File directory = new File(path);
        File[] files = directory.listFiles();

        for (int i = 0; i < files.length; i++) {

            Uri fileUri = Uri.parse(files[i].getAbsolutePath());

            if (files[i].getName().contains("MockTinderLoop")) {

                filesUri.add(fileUri);
            }

            Log.v(TAG, "FileName:" + files[i].getName());

        }
    }

    @Override
    public void itemClicked(Uri fileUri) {

    }
}
