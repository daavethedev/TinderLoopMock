package com.chamelon.tinderloopmock.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.chamelon.tinderloopmock.info.Info;
import com.chamelon.tinderloopmock.utils.PathUtils;
import com.chamelon.tinderloopmock.R;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditActivity extends AppCompatActivity implements Info, View.OnClickListener {

    private FFmpeg fFmpeg;
    private Handler mHandler;
    private TextView tvProgress;
    private ImageButton ibSpeedUp;
    private VideoView vvVideoPlayer;
    private SeekBar sbVideoProgress;
    private ImageButton ibEditVideo;
    private SweetAlertDialog pDialog;
    private ImageButton ibSelectVideo;
    private CrystalRangeSeekbar crsTrimmer;

    private String filePath1;
    private String filePath2;
    private String finalfile;
    private String destFileName;
    private String fileReversedJoined;

    private Number minValue;
    private Uri selectedVideoUri;

    private boolean speedUpVideo;
    private boolean isVideoLoaded;

    private String[] cutComplexCommand;
    private String[] speedUpComplexCommand;
    private String[] reverseComplexCommand;
    private String[] concatenateComplexCommand;

    private int width, height;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        initMembers();

        try {

            initFFmpeg();

        } catch (FFmpegNotSupportedException e) {

            Log.e(TAG, "This device does not support FFmpeg.");
        }
    }

    private void initMembers() {

        mHandler = new Handler();

        ibEditVideo = findViewById(R.id.ib_edit);
        ibSpeedUp = findViewById(R.id.ib_speed_up);
        tvProgress = findViewById(R.id.tv_progress);
        crsTrimmer = findViewById(R.id.range_seekbar);
        vvVideoPlayer = findViewById(R.id.vv_video_player);
        ibSelectVideo = findViewById(R.id.ib_select_video);
        sbVideoProgress = findViewById(R.id.sb_video_progress);

        if (selfPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_WRITE_PERMISSION);
            }
        }

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#D81B60"));
        pDialog.setTitleText("Working on it.");
        pDialog.setCancelable(false);

        setListeners();
    }

    private void setListeners() {

        ibSpeedUp.setOnClickListener(this);
        ibEditVideo.setOnClickListener(this);
        ibSelectVideo.setOnClickListener(this);

        crsTrimmer.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {

                EditActivity.this.minValue = minValue;

                if (isVideoLoaded) {
                    vvVideoPlayer.seekTo((minValue.intValue() * 1000));
                    vvVideoPlayer.start();
                }

                updateProgressBar();
            }
        });

        sbVideoProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                tvProgress.setText((progress / 1000) + " / " + (vvVideoPlayer.getDuration() / 1000));

                if (progress / 1000 >= minValue.intValue() + 4) {

                    vvVideoPlayer.seekTo(minValue.intValue() * 1000);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                mHandler.removeCallbacks(updateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                vvVideoPlayer.pause();
                mHandler.removeCallbacks(updateTimeTask);
                vvVideoPlayer.seekTo(sbVideoProgress.getProgress());
                updateProgressBar();

            }
        });
    }

    public boolean selfPermissionGranted(String permission) {
        // For Android < Android M, self permissions are always granted.
        boolean result = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (28 >= Build.VERSION_CODES.M) {
                result = checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                result = PermissionChecker.checkSelfPermission(this, permission)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }

        return result;
    }

    private void initFFmpeg() throws FFmpegNotSupportedException {

        if (fFmpeg == null) {

            fFmpeg = FFmpeg.getInstance(this);
            fFmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {

                    Log.v(TAG, "Failed to load.");
                    Toast.makeText(EditActivity.this, "Sorry, Your android version is not supported by this app.", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onSuccess() {

                    Log.v(TAG, "Successfully loaded.");

                }

                @Override
                public void onStart() {

                    Log.v(TAG, "Loading Started.");
                }

                @Override
                public void onFinish() {

                    Log.v(TAG, "Loading Finished.");

                }
            });
        }
    }

    private void updateProgressBar() {
        mHandler.postDelayed(updateTimeTask, 0);
    }

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            sbVideoProgress.setProgress(vvVideoPlayer.getCurrentPosition());
            sbVideoProgress.setMax(vvVideoPlayer.getDuration());
            mHandler.postDelayed(this, 0);
        }
    };

    @Override
    public void onClick(View v) {

        if (v == ibSelectVideo) {

            if (selfPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Intent intent = new Intent();
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), RC_GALLERY_VIDEO);

            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_WRITE_PERMISSION);
                }
            }
        }

        if (v == ibEditVideo) {

            if (isVideoLoaded) {

                executeCutVideoCommand((minValue.intValue() * 1000), (minValue.intValue() + 4) * 1000);

                vvVideoPlayer.pause();
                pDialog.show();
            }
        }


        if (v == ibSpeedUp) {

            if (speedUpVideo) {

                ibSpeedUp.setImageResource(R.drawable.ic_play);
                speedUpVideo = false;

            } else {

                ibSpeedUp.setImageResource(R.drawable.ic_fast);
                speedUpVideo = true;

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_GALLERY_VIDEO) {
                selectedVideoUri = data.getData();
                if (selectedVideoUri != null) {

                    Toast.makeText(this, "Seek to position you want to apply the effect.", Toast.LENGTH_SHORT).show();

                    vvVideoPlayer.start();
                    vvVideoPlayer.setVideoURI(selectedVideoUri);

                    updateProgressBar();


                    vvVideoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(final MediaPlayer mp) {
                            width = mp.getVideoWidth();
                            height = mp.getVideoHeight();


                            crsTrimmer.setMaxValue(vvVideoPlayer.getDuration() / 1000);
                            crsTrimmer.setFixGap(30);

                            Log.v(TAG, "Loaded.");
                        }
                    });

                    ibSpeedUp.setVisibility(View.VISIBLE);
                    ibEditVideo.setVisibility(View.VISIBLE);
                    crsTrimmer.setVisibility(View.VISIBLE);
                    isVideoLoaded = true;

                }
            }
        }
    }

    private void executeCutVideoCommand(long startMs, long endMs) {

        String destPath = "/storage/emulated/0/MockTinderLoop/";//Replace your dest Path

        File externalStoragePublicDirectory = new File(destPath);

        if (!externalStoragePublicDirectory.exists() ? externalStoragePublicDirectory.mkdir() : true) {
            String yourRealPath = PathUtils.getPathFromUri(this, selectedVideoUri);

            String filePrefix = yourRealPath.substring(yourRealPath.lastIndexOf("."));
            String destFileName = "cut_video";

            File dest = (filePrefix.equals(".webm") || filePrefix.equals(".mkv")) ? new File(externalStoragePublicDirectory, destFileName + ".mp4") : new File(externalStoragePublicDirectory, destFileName + filePrefix);

            filePath1 = dest.getAbsolutePath();

            if (new File(filePath1).exists()) {

                new File(filePath1).delete();

            }

            cutComplexCommand = new String[]{"-ss", "" + startMs / 1000, "-y", "-i", yourRealPath, "-t", "" + (endMs - startMs) / 1000, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", "-preset", "ultrafast", filePath1};

            execFFmpegBinary(cutComplexCommand);
        }
    }

    private void executeReverseVideoCommand(String inputFileAbsolutePath) {

        String destPath = "/storage/emulated/0/MockTinderLoop/";

        File externalStoragePublicDirectory = new File(destPath);

        if (!externalStoragePublicDirectory.exists() ? externalStoragePublicDirectory.mkdir() : true) {

            String yourRealPath = PathUtils.getPathFromUri(this, selectedVideoUri);
            String filePrefix = yourRealPath.substring(yourRealPath.lastIndexOf("."));
            String destFileName = "reverse_video";

            File dest = (filePrefix.equals(".webm") || filePrefix.equals(".mkv")) ? new File(externalStoragePublicDirectory, destFileName + ".mp4") : new File(externalStoragePublicDirectory, destFileName + filePrefix);

            filePath2 = dest.getAbsolutePath();

            if (new File(filePath2).exists()) {

                new File(filePath2).delete();

            }

            reverseComplexCommand = new String[]{"-i", inputFileAbsolutePath, "-vf", "reverse", "-preset", "ultrafast", filePath2};
            execFFmpegBinary(reverseComplexCommand);

        }
    }

    private void executeConcatenateCommand() {

        String destPath = "/storage/emulated/0/MockTinderLoop/";
        File externalStoragePublicDirectory = new File(destPath);

        if (!externalStoragePublicDirectory.exists() ? externalStoragePublicDirectory.mkdir() : true) {

            String yourRealPath = PathUtils.getPathFromUri(this, selectedVideoUri);
            String filePrefix = yourRealPath.substring(yourRealPath.lastIndexOf("."));
            String destFileName = "cut_video";

            File dest = (filePrefix.equals(".webm") || filePrefix.equals(".mkv")) ? new File(externalStoragePublicDirectory, destFileName + ".mp4") : new File(externalStoragePublicDirectory, destFileName + filePrefix);

            filePath1 = dest.getAbsolutePath();

            fileReversedJoined = Environment.getExternalStorageDirectory() + "/MockTinderLoop/reversed_joined.mp4";

            concatenateComplexCommand = new String[]{"-y", "-i", "/storage/emulated/0/MockTinderLoop/cut_video.mp4", "-i", "/storage/emulated/0/MockTinderLoop/reverse_video.mp4", "-strict", "experimental", "-filter_complex",
                    "[0:v]scale=iw*min(" + width + "/iw\\," + height + "/ih):ih*min(" + width + "/iw\\," + height + "/ih), pad=" + width + ":" + height + ":(" + width + "-iw*min(" + width + "/iw\\," + height + "/ih))/2:(" + height + "-ih*min(" + width + "/iw\\," + height + "/ih))/2,setsar=1:1[v0];[1:v] scale=iw*min(" + width + "/iw\\," + height + "/ih):ih*min(" + width + "/iw\\," + height + "/ih), pad=" + width + ":" + height + ":(" + width + "-iw*min(" + width + "/iw\\," + height + "/ih))/2:(" + height + "-ih*min(" + width + "/iw\\," + height + "/ih))/2,setsar=1:1[v1];[v0][0:a][v1][1:a] concat=n=2:v=1:a=1",
                    "-ab", "48000", "-ac", "2", "-ar", "22050", "-s", "" + width + "x" + height + "", "-vcodec", "libx264", "-crf", "27", "-q", "4", "-preset", "ultrafast", fileReversedJoined};


            execFFmpegBinary(concatenateComplexCommand);

        }
    }

    private void executeSpeedUpCommand(String inputFileAbsolutePath, float setpts) {

        String destPath = "/storage/emulated/0/MockTinderLoop/";

        File externalStoragePublicDirectory = new File(destPath);

        if (!externalStoragePublicDirectory.exists() ? externalStoragePublicDirectory.mkdir() : true) {

            String yourRealPath = PathUtils.getPathFromUri(this, selectedVideoUri);

            String filePrefix = yourRealPath.substring(yourRealPath.lastIndexOf("."));
            destFileName = "TinderLoop";

            File dest = (filePrefix.equals(".webm") || filePrefix.equals(".mkv")) ? new File(externalStoragePublicDirectory, destFileName + ".mp4") : new File(externalStoragePublicDirectory, destFileName + filePrefix);

            int fileNo = 0;
            while (dest.exists()) {
                fileNo++;
                dest = (filePrefix.equals(".webm") || filePrefix.equals(".mkv")) ? new File(externalStoragePublicDirectory, destFileName + fileNo + ".mp4") : new File(externalStoragePublicDirectory, destFileName + fileNo + filePrefix);
            }

            finalfile = dest.getAbsolutePath();
            speedUpComplexCommand = new String[]{"-y", "-i", inputFileAbsolutePath, "-an", "-filter:v", "[0:v]setpts=" + setpts + "*PTS[v]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", finalfile};
            execFFmpegBinary(speedUpComplexCommand);
        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            fFmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "FAILED with output : " + s);
                    Toast.makeText(EditActivity.this, "Failed.", Toast.LENGTH_SHORT).show();
                    crsTrimmer.setVisibility(View.GONE);
                    pDialog.hide();
                    deleteTempFiles();

                }

                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "SUCCESS with output : " + s);

                    if (isTheSame(command, cutComplexCommand)) {

                        executeReverseVideoCommand(filePath1);
                        Toast.makeText(EditActivity.this, "Trimming Done.", Toast.LENGTH_SHORT).show();
                    } else if (isTheSame(command, reverseComplexCommand)) {

                        executeConcatenateCommand();
                        Toast.makeText(EditActivity.this, "Reversing Done.", Toast.LENGTH_SHORT).show();

                    } else if (isTheSame(command, concatenateComplexCommand)) {

                        if (speedUpVideo) {

                            executeSpeedUpCommand(fileReversedJoined, (float) 0.5);
                            ibSpeedUp.setImageResource(R.drawable.ic_play);
                            speedUpVideo = false;

                        } else {

                            executeSpeedUpCommand(fileReversedJoined, (float) 1);
                        }

                        Toast.makeText(EditActivity.this, "Merging Done.", Toast.LENGTH_SHORT).show();

                    } else if (isTheSame(command, speedUpComplexCommand)) {

                        vvVideoPlayer.stopPlayback();
                        vvVideoPlayer.setVideoPath(finalfile);
                        vvVideoPlayer.start();

                        vvVideoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.setLooping(true);
                            }
                        });

                        updateProgressBar();

                        Toast.makeText(EditActivity.this, "Done.", Toast.LENGTH_SHORT).show();

                        if (new File(fileReversedJoined).exists()) {

                            new File(fileReversedJoined).delete();
                        }

                        crsTrimmer.setVisibility(View.GONE);
                        pDialog.hide();
                        ibSpeedUp.setVisibility(View.GONE);
                        ibEditVideo.setVisibility(View.GONE);

                        refreshGallery(finalfile);

                        deleteTempFiles();

                    }
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);

                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);

                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);


                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    public void refreshGallery(String path) {

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(path));
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);

    }
    private void deleteTempFiles() {

        try {

            if (new File(filePath1).exists()) {

                new File(filePath1).delete();
            }

            if (new File(filePath2).exists()) {

                new File(filePath2).delete();
            }

        } catch (NullPointerException e) {

            e.printStackTrace();
        }
    }

    public boolean isTheSame(String[] arr1, String[] arr2) {
        if (arr1.length == arr2.length) {
            for (int i = 0; i < arr1.length; i++) {
                if ((arr1[i] != null && arr2[i] != null && !arr1[i].equals(arr2[i]))
                        || (arr1[i] != null && arr2[i] == null) ||
                        (arr2[i] != null && arr1[i] == null)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
}
