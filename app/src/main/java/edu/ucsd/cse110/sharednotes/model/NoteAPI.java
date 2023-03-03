package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: - getNote (maybe getNoteAsync)
    // TODO: - putNote (don't need putNotAsync, probably)
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;
    private static  final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final Gson gson;

    public NoteAPI() {
        this.client = new OkHttpClient();
        this.gson=new Gson();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     * <p>
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     * <p>
     * This method should can be called on a background thread (Android
     * disallows network requests on the main thread).
     */
    @WorkerThread
    public String echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        String encodedMsg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + encodedMsg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//        public final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    public boolean post(Note note) {
        var json =gson.toJson(note);

        var theBody = RequestBody.create(json,JSON);
        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + note.title)
                .put(theBody)
                .build();

        try (var response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
//        MutableLiveData<Note> noteLiveData = new MutableLiveData<>();
//        Gson gson = new Gson();
//        String json = gson.toJson(note);
//        String url = "https://sharednotes.goto.ucsd.edu/notes/" + note.title;
//        RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));
//        Request request = new Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                // Handle the failure here.
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    throw new IOException("Unexpected code " + response);
//                }
//                // Parse the response into a Note object.
//                Note savedNote = gson.fromJson(response.body().string(), Note.class);
//                noteLiveData.postValue(savedNote);
//            }
//        });
//
//        return noteLiveData;
// }   }
    }
    public Future<Boolean> postAsy(Note note){
        var executor = Executors.newSingleThreadExecutor();
        return executor.submit(()-> post(note));
    }


    public Note get(String title){
        //MutableLiveData<Note> noteLiveData = new MutableLiveData<>();
        String url = "https://sharednotes.goto.ucsd.edu/notes/" + title;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            String body = response.body().string();
            Gson gson = new Gson();
            Note note = gson.fromJson(body, Note.class);
            return note;
            //noteLiveData.postValue(note);
           // return noteLiveData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @AnyThread
    public Future<String> echoAsync(String msg) {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> echo(msg));

        // We can use future.get(1, SECONDS) to wait for the result.
        return future;
    }
}
