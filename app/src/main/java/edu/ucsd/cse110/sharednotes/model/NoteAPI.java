package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     */
    public void echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        msg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + msg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class PostNote {
        public final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        private final OkHttpClient client = new OkHttpClient();

        public String post(Note note) throws IOException {
            Gson gson = new Gson();
            String json = gson.toJson(note);
            RequestBody body = RequestBody.create(json, JSON);
            String url = "https://sharednotes.goto.ucsd.edu/note/" + note.title;
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                return response.body().toString();
            }
        }
        public LiveData<Note> get(String title) throws IOException {
            MutableLiveData<Note> noteLiveData = new MutableLiveData<>();
            String url = "https://sharednotes.goto.ucsd.edu/note/" + title;
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String json = response.body().string();
                Gson gson = new Gson();
                Note note = gson.fromJson(json, Note.class);
                noteLiveData.postValue(note);
                return noteLiveData;

            }
        }


    }

}
