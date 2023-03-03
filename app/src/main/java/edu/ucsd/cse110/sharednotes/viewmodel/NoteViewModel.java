package edu.ucsd.cse110.sharednotes.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.sharednotes.model.Note;
import edu.ucsd.cse110.sharednotes.model.NoteAPI;
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
        note = repo.getSynced(title);
//        System.out.println(note.getValue().title);
//        note = repo.getRemote(title);
//        if (note == null) {
//            note = repo.getLocal(title);
//        }
//
//        return note;

        var executor = Executors.newSingleThreadScheduledExecutor();
        var pollingFuture = executor.scheduleAtFixedRate(() -> {
            var remoteNote = repo.getRemote(title).getValue();
            if (remoteNote != null) {
                repo.upsertLocal(remoteNote);
            }
        }, 0, 3, TimeUnit.SECONDS);

        return note;


    }

    public void save(Note note) {
        // TODO: try to upload the note to the server.
        repo.upsertLocal(note);
//        repo.upsertLocal(repo.getRemote(note.title).getValue());
        repo.upsertRemote(note);

    }
}
