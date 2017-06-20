package com.danielkim.soundrecorder.fragments;

import android.os.Bundle;
import android.os.FileObserver;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.activities.MainActivity;
import com.danielkim.soundrecorder.adapters.FileViewerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 12/23/2014.
 */
public class FileViewerFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "FileViewerFragment";
    public boolean setPlay;
    Menu menu;
    List<String> list = new ArrayList<String>();
    private ActionMode mActionMode;
    private boolean choosen;
    private int position;
    private FileViewerAdapter mFileViewerAdapter;
    FileObserver observer =
            new FileObserver(android.os.Environment.getExternalStorageDirectory().toString()
                    + "/SoundRecorder") {
                // set up a file observer to watch this directory on sd card
                @Override
                public void onEvent(int event, String file) {
                    if (event == FileObserver.DELETE) {
                        // user deletes a recording file out of the app

                        String filePath = android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]";

                        Log.d(LOG_TAG, "File deleted ["
                                + android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]");

                        // remove file from database and recyclerview
                        mFileViewerAdapter.removeOutOfApp(filePath);
                    }
                }
            };
    private int curPosition = -1;

    public static FileViewerFragment newInstance(int position) {
        FileViewerFragment f = new FileViewerFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        observer.startWatching();
        setHasOptionsMenu(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_licenses:
                return true;
            case R.id.Settings:
                return true;
            case R.id.Delete:
                choosen = false;
                Toast.makeText(getActivity(), "delete basildi " + list, Toast.LENGTH_SHORT).show();
                onPrepareOptionsMenu(menu);
                ((MainActivity) getActivity()).enableToolbar();
                mFileViewerAdapter.remove(5);   //turn all of choosen contents to white
                mFileViewerAdapter.choosen = false;
                mFileViewerAdapter.list.clear();
                list.clear();
                return true;
            case R.id.cancel:
                choosen = false;
                onPrepareOptionsMenu(menu);
                ((MainActivity) getActivity()).enableToolbar();
                mFileViewerAdapter.refresh();   //turn all of choosen contents to white
                mFileViewerAdapter.choosen = false;
                mFileViewerAdapter.list.clear();
                list.clear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuItem deleteItem = menu.findItem(R.id.Delete);
        MenuItem item1 = menu.findItem(R.id.a);
        MenuItem item2 = menu.findItem(R.id.aa);
        MenuItem item3 = menu.findItem(R.id.aaa);
        MenuItem item4 = menu.findItem(R.id.cancel);
        if (choosen == true) {
            deleteItem.setVisible(true);
            item1.setVisible(true);
            item2.setVisible(true);
            item3.setVisible(true);
            item4.setVisible(true);
            item1.setEnabled(false);
            item2.setEnabled(false);
            item3.setEnabled(false);

        } else {
            deleteItem.setVisible(false);
            item1.setVisible(false);
            item2.setVisible(false);
            item3.setVisible(false);
            item4.setVisible(false);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file_viewer, container, false);
        final RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        //newest to oldest order (database stores from oldest to newest)
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mFileViewerAdapter = new FileViewerAdapter(getActivity(), llm, list, false, false, -1);
        mRecyclerView.setAdapter(mFileViewerAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new RecyclerClick_Listener() {
            @Override
            public void onClick(View view, int position) {
                //If ActionMode not null select item
                Toast.makeText(getActivity(), "onclick basildi", Toast.LENGTH_SHORT).show();
                if (choosen == true) {
                    if (!list.contains(String.valueOf(position))) {
                        list.add(String.valueOf(position));
                        Toast.makeText(getActivity(), "CurList " + list, Toast.LENGTH_SHORT).show();
                        mFileViewerAdapter = new FileViewerAdapter(getActivity(), llm, list, choosen, false, -1);
                        mRecyclerView.setAdapter(mFileViewerAdapter);
                    } else {
                        list.remove(String.valueOf(position));
                        Toast.makeText(getActivity(), "CurList " + list, Toast.LENGTH_SHORT).show();
                        mFileViewerAdapter = new FileViewerAdapter(getActivity(), llm, list, choosen, false, -1);
                        mRecyclerView.setAdapter(mFileViewerAdapter);
                    }
                } else {
                    if (curPosition != position) {
                        setPlay = true;
                        mFileViewerAdapter = new FileViewerAdapter(getActivity(), llm, list, choosen, setPlay, position);
                        mRecyclerView.setAdapter(mFileViewerAdapter);
                        curPosition = position;
                    } else {
                        if (mFileViewerAdapter.setPlay) {
                            Log.e(LOG_TAG, "setplay true");
                        } else {
                            setPlay = true;
                            mFileViewerAdapter = new FileViewerAdapter(getActivity(), llm, list, choosen, setPlay, position);
                            mRecyclerView.setAdapter(mFileViewerAdapter);
                            Log.e(LOG_TAG, "setplay false");
                        }
                    }

                }
            }

            @Override
            public void onLongClick(View view, int position) {

                choosen = true;

                if (choosen) {
                    if (!list.contains(String.valueOf(position))) {
                        list.add(String.valueOf(position));
                        Toast.makeText(getActivity(), "CurList " + list, Toast.LENGTH_SHORT).show();
                        mFileViewerAdapter = new FileViewerAdapter(getActivity(), llm, list, choosen, false, -1);
                        mRecyclerView.setAdapter(mFileViewerAdapter);
                    } else {
                        list.remove(String.valueOf(position));
                        Toast.makeText(getActivity(), "CurList " + list, Toast.LENGTH_SHORT).show();
                        mFileViewerAdapter = new FileViewerAdapter(getActivity(), llm, list, choosen, false, -1);
                        mRecyclerView.setAdapter(mFileViewerAdapter);
                    }
                    ((MainActivity) getActivity()).disableToolbar();
                    onPrepareOptionsMenu(menu);
                }

                //Select item on long click
                Toast.makeText(getActivity(), "onlongclick basildi", Toast.LENGTH_SHORT).show();

            }
        }));
        return v;
    }
}




