package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
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
//    private ScheduledFuture<?> pollingFuture;
    private Map<String, ScheduledFuture<?>> pollingFutures = new HashMap<>();
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
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

//    public LiveData<Note> getRemote(String title) {
//        // TODO: Implement getRemote!
//        // TODO: Set up polling background thread (MutableLiveData?)
//        // TODO: Refer to TimerService from https://github.com/DylanLukes/CSE-110-WI23-Demo5-V2.
//
//        // Start by fetching the note from the server _once_ and feeding it into MutableLiveData.
//        // Then, set up a background thread that will poll the server every 3 seconds.
//
//        // You may (but don't have to) want to cache the LiveData's for each title, so that
//        // you don't create a new polling thread every time you call getRemote with the same title.
//
//        MutableLiveData<Note> noteLiveData = new MutableLiveData<>();
//        Note note = api.get(title);
//// && note.version > localNote.version
//        Note localNote = getLocal(title).getValue();
////        if(note!=null){
////            upsertLocal(note);
////            noteLiveData.postValue(note);
////        }
//        if (pollingFuture != null) {
//            pollingFuture.cancel(true);
//        }
//        ScheduledExecutorService temp = Executors.newSingleThreadScheduledExecutor();
//        temp.scheduleAtFixedRate(()->{
//            Note pulledNote = api.get(title);
//            if (pulledNote != null) {
//                upsertLocal(pulledNote);
//                noteLiveData.postValue(pulledNote);
//            }
//        },0,3,TimeUnit.SECONDS);
//
//        return noteLiveData;
//
//    }


    public LiveData<Note> getRemote(String title) {
        MutableLiveData<Note> noteLiveData = new MutableLiveData<>();
        Note note = api.get(title);

        Note localNote = getLocal(title).getValue();
        if (note != null && (localNote == null || note.version > localNote.version)) {
            upsertLocal(note);
        }

        ScheduledFuture<?> pollingFuture = pollingFutures.get(title);
        if (pollingFuture != null) {
            // A polling task already exists for this title, cancel it and remove it from the map
            pollingFuture.cancel(true);
            pollingFutures.remove(title);
        }

        pollingFuture = executor.scheduleAtFixedRate(() -> {
            Note tempNote = api.get(title);
            if (tempNote != null) {
//                || tempNote.version > localNote.version
                if (localNote == null ) {
                    upsertLocal(tempNote);
                }
                noteLiveData.postValue(tempNote);
            }
        }, 0, 3, TimeUnit.SECONDS);

        // Add the new polling task to the map
        pollingFutures.put(title, pollingFuture);

        return noteLiveData;
    }


    public void upsertRemote(Note note) {
        // TODO: Implement upsertRemote!
        note.version = note.version + 1;
        api.postAsy(note);

    }
}
