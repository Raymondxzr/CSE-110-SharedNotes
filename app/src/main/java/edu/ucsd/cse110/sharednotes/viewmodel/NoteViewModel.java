package edu.ucsd.cse110.sharednotes.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.sharednotes.model.Note;
import edu.ucsd.cse110.sharednotes.model.NoteDatabase;
import edu.ucsd.cse110.sharednotes.model.NoteRepository;

public class NoteViewModel extends AndroidViewModel {
    private LiveData<Note> note;
    private final NoteRepository repo;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        var context = application.getApplicationContext();
        var db = NoteDatabase.provide(context);
        var dao = db.getDao();
        this.repo = new NoteRepository(dao);
    }

    public LiveData<Note> getNote(String title) {
        // TODO: use getSynced here instead?
        // The returned live data should update whenever there is a change in
        // the database, or when the server returns a newer version of the note.
        // Polling interval: 3s.
//        note = repo.getSynced(title);
//        note = repo.getRemote(title);
        if (note == null) {
            note = repo.getLocal(title);
        }
//
//        return note;
//        var note = new MediatorLiveData<Note>();

        // Set up a polling background thread to check for remote updates every 3 seconds
//        var executor = Executors.newSingleThreadScheduledExecutor();
//        var pollingFuture = executor.scheduleAtFixedRate(() -> {
//            var remoteNote = repo.getRemote(title).getValue();
//            if (remoteNote != null) {
//                repo.upsertLocal(remoteNote);
//            }
//        }, 0, 3, TimeUnit.SECONDS);

        // If we get a local update, pass it on.
//        note.addSource(repo.getLocal(title), note::postValue);

        // If we get a synced update, update the local version (triggering the above observer)
//        note.addSource(repo.getSynced(title), note::postValue);

        // When this LiveData object is no longer observed, stop the polling thread
//        note.addOnInactiveListener(() -> {
//            pollingFuture.cancel(false);
//        });

        return note;


    }

    public void save(Note note) {
        // TODO: try to upload the note to the server.
        repo.upsertLocal(note);
//        repo.upsertLocal(repo.getRemote(note.title).getValue());
        repo.upsertRemote(note);

    }
}
