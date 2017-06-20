package com.danielkim.soundrecorder.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordFragment extends Fragment {


    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = RecordFragment.class.getSimpleName();
    ProgressBar progress;
    long timeWhenPaused = 0; //stores time when user clicks pause button
    private String mFileName = null;
    private String mFilePath = null;
    private MediaRecorder mRecorder = null;
    private DBHelper mDatabase;
    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private int mElapsedSeconds = 0;
    private RecordingService.OnTimerChangedListener onTimerChangedListener = null;
    private Timer mTimer = null;
    private TimerTask mIncrementTimerTask = null;
    private int position;
    private int progresStatus = 0;
    //Recording controls
    private FloatingActionButton mRecordButton = null;
    private Button mPauseButton = null;
    private TextView mRecordingPrompt;
    private int mRecordPromptCount = 0;
    private boolean mStartRecording = true;
    private Chronometer mChronometer = null;

    public RecordFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Record_Fragment.
     */
    public static RecordFragment newInstance(int position) {
        RecordFragment f = new RecordFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        // PreferenceManager.setDefaultValue(this, R.xml.preferences, false);
        mDatabase = new DBHelper(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View recordView = inflater.inflate(R.layout.fragment_record, container, false);
        progress = (ProgressBar) recordView.findViewById(R.id.recordProgressBar);

        mChronometer = (Chronometer) recordView.findViewById(R.id.chronometer);
        //update recording prompt text
        mRecordingPrompt = (TextView) recordView.findViewById(R.id.recording_status_text);

        mRecordButton = (FloatingActionButton) recordView.findViewById(R.id.btnRecord);
        mRecordButton.setColorNormal(getResources().getColor(R.color.primary));
        mRecordButton.setColorPressed(getResources().getColor(R.color.primary_dark));
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;
            }
        });


        return recordView;
    }

    // Recording Start/Stop
    //TODO: recording pause
    private void onRecord(boolean start) {

        final Intent intent = new Intent(getActivity(), RecordingService.class);

        if (start) {
            // start recording
            Toast.makeText(getActivity(), R.string.toast_recording_start, Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                //folder /SoundRecorder doesn't exist, create the folder
                folder.mkdir();
            }
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            int count = 0;
            File f;
            if (sharedPrefs.getBoolean("perform_sync", false)) {

                mRecordButton.setImageResource(R.drawable.ic_media_stop);
                //mPauseButton.setVisibility(View.VISIBLE);

                progress.setVisibility(View.VISIBLE);
                //start Chronometer
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();
                mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer chronometer) {

                        if (progresStatus < 60) {
                            progresStatus++;
                            progress.setProgress(progresStatus);
                        } else {
                            progresStatus = 1;
                            progress.setProgress(progresStatus);
                        }

                        if (mRecordPromptCount == 0) {
                            mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                        } else if (mRecordPromptCount == 1) {
                            mRecordingPrompt.setText(getString(R.string.record_in_progress) + "..");
                        } else if (mRecordPromptCount == 2) {
                            mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
                            mRecordPromptCount = -1;
                        }

                        mRecordPromptCount++;
                    }
                });
                do {
                    count++;
                    String data = sharedPrefs.getString("full_name", "Not known to us");
                    if (data.isEmpty()) {
                        mFileName = "M" + "№ " + (mDatabase.getCount() + count) + ".mp4";
                    } else {
                        mFileName =
                                sharedPrefs.getString("full_name", "Not known to us")
                                        + "№ " + (mDatabase.getCount() + count) + ".mp4";

                    }

                    mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    mFilePath += "/SoundRecorder/" + mFileName;

                    f = new File(mFilePath);

                } while (f.exists() && !f.isDirectory());
                intent.putExtra("filename", mFileName);
                //start RecordingService
                getActivity().startService(intent);
                //keep screen on while recording
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                mRecordPromptCount++;

            } else {
                AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(getActivity());

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View view = inflater.inflate(R.layout.dialog_add_file, null);

                final EditText input = (EditText) view.findViewById(R.id.new_name);
                renameFileBuilder.setTitle("Enter name of Record");
                renameFileBuilder.setCancelable(true);
                renameFileBuilder.setPositiveButton(getActivity().getString(R.string.dialog_action_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //close keyboard on edittext
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                try {
                                    String value = input.getText().toString().trim() + ".mp4";
                                    File f;
                                    do {

                                        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                                        mFilePath += "/SoundRecorder/" + mFileName;

                                        f = new File(mFilePath);

                                    } while (f.exists() && !f.isDirectory());
                                    mRecordButton.setImageResource(R.drawable.ic_media_stop);
                                    //mPauseButton.setVisibility(View.VISIBLE);

                                    progress.setVisibility(View.VISIBLE);
                                    //start Chronometer
                                    mChronometer.setBase(SystemClock.elapsedRealtime());
                                    mChronometer.start();
                                    mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                                        @Override
                                        public void onChronometerTick(Chronometer chronometer) {

                                            if (progresStatus < 60) {
                                                progresStatus++;
                                                progress.setProgress(progresStatus);
                                            } else {
                                                progresStatus = 1;
                                                progress.setProgress(progresStatus);
                                            }

                                            if (mRecordPromptCount == 0) {
                                                mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                                            } else if (mRecordPromptCount == 1) {
                                                mRecordingPrompt.setText(getString(R.string.record_in_progress) + "..");
                                            } else if (mRecordPromptCount == 2) {
                                                mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
                                                mRecordPromptCount = -1;
                                            }

                                            mRecordPromptCount++;
                                        }
                                    });

                                    Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT).show();
                                    intent.putExtra("filename", value);
                                    //start RecordingService
                                    getActivity().startService(intent);
                                    //keep screen on while recording
                                    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                                    mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                                    mRecordPromptCount++;
                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "exception", e);
                                }

                                dialog.cancel();
                            }
                        });
                renameFileBuilder.setNegativeButton(getActivity().getString(R.string.dialog_action_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //close keyboard on edittext
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                dialog.cancel();
                            }
                        });

                renameFileBuilder.setView(view);
                AlertDialog alert = renameFileBuilder.create();
                alert.show();
                //Focus and open keyboard on edittext
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }

        } else {
            progresStatus = 0;
            progress.setVisibility(View.INVISIBLE);
            //stop recording
            mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
            //mPauseButton.setVisibility(View.GONE);
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused = 0;
            mRecordingPrompt.setText(getString(R.string.record_prompt));

            getActivity().stopService(intent);
            //allow the screen to turn off again once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }


}