package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class NoteRepository {
    private final NoteDao dao;
    private final NoteAPI api;

    public NoteRepository(NoteDao dao) {
        this.dao = dao;
        this.api=NoteAPI.provide();
    }

    // Synced Methods
    // ==============

    /**
     * This is where the magic happens. This method will return a LiveData object that will be
     * updated when the note is updated either locally or remotely on the server. Our activities
     * however will only need to observe this one LiveData object, and don't need to care where
     * it comes from!
     * <p>
     * This method will always prefer the newest version of the note.
     *
     * @param title the title of the note
     * @return a LiveData object that will be updated when the note is updated locally or remotely.
     */
    public LiveData<Note> getSynced(String title) {
        var note = new MediatorLiveData<Note>();

        Observer<Note> updateFromRemote = theirNote -> {
            var ourNote = note.getValue();
            if (theirNote == null) return; // do nothing
            if (ourNote == null || ourNote.version < theirNote.version) {
                upsertLocal(theirNote);
            }
        };

        // If we get a local update, pass it on.
        note.addSource(getLocal(title), note::postValue);
        // If we get a remote update, update the local version (triggering the above observer)
        note.addSource(getRemote(title), updateFromRemote);

        return note;
    }

    public void upsertSynced(Note note) {
        upsertLocal(note);
        upsertRemote(note);
    }

    // Local Methods
    // =============

    public LiveData<Note> getLocal(String title) {
        return dao.get(title);
    }

    public LiveData<List<Note>> getAllLocal() {
        return dao.getAll();
    }

    public void upsertLocal(Note note) {
        note.version = note.version + 1;
        dao.upsert(note);
    }

    public void deleteLocal(Note note) {
        dao.delete(note);
    }

    public boolean existsLocal(String title) {
        return dao.exists(title);
    }

    // Remote Methods
    // ==============

    public LiveData<Note> getRemote(String title) {
        // TODO: Implement getRemote!
        // TODO: Set up polling background thread (MutableLiveData?)
        // TODO: Refer to TimerService from https://github.com/DylanLukes/CSE-110-WI23-Demo5-V2.

        // Start by fetching the note from the server _once_ and feeding it into MutableLiveData.
        // Then, set up a background thread that will poll the server every 3 seconds.

        // You may (but don't have to) want to cache the LiveData's for each title, so that
        // you don't create a new polling thread every time you call getRemote with the same title.
//        LiveData<Note> remoteNote = getSynced(title);

        // Then, set up a background thread that will poll the server every 3 seconds.
//        var executor = Executors.newSingleThreadScheduledExecutor();
//        executor.scheduleAtFixedRate(() -> {
//            getSynced(title);
//        }, 0, 3, TimeUnit.SECONDS);
        MutableLiveData<Note> noteLiveData = new MutableLiveData<>();
        Note note = api.get(title);
        if(note!=null){
            upsertLocal(note);
            noteLiveData.postValue(note);
        }
        ScheduledExecutorService temp = Executors.newSingleThreadScheduledExecutor();
        temp.scheduleAtFixedRate(()->{
            Note tempN = api.get(title);
            if (tempN != null) {
                upsertLocal(tempN);
                noteLiveData.postValue(tempN);
            }
        },0,20,TimeUnit.SECONDS);

        // Check if the note exists on the server
//        try {
//            NoteAPI p = new NoteAPI();
//            Note existingNote = p.get(title).getValue();
//            if (existingNote != null) {
//                noteLiveData.postValue(existingNote);
//                return noteLiveData;
//            }
//        } catch (IOException e) {
//            // Handle the exception
//            e.printStackTrace();
//        }

        // If the note doesn't exist on the server or there was an error fetching it, return an empty note
//        Note emptyNote = new Note();
//        emptyNote.title = title;
//        noteLiveData.postValue(emptyNote);
//        return noteLiveData;
        return noteLiveData;

    }

//        return remoteNote;
    // You don't need to worry about killing background threads.

//        throw new UnsupportedOperationException("Not implemented yet");

//        throw new UnsupportedOperationException("Not implemented yet");


    public void upsertRemote(Note note) {
        // TODO: Implement upsertRemote!
//        dao.upsert(note);
        //NoteAPI example = new NoteAPI();
        //note.version = note.version + 1;
       api.postAsy(note);
//
//        throw new UnsupportedOperationException("Not implemented yet");
    }
}
