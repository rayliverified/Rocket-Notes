package stream.rocketnotes.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;

import androidx.annotation.NonNull;
import stream.rocketnotes.NotesItem;
import stream.rocketnotes.interfaces.FirestoreInterface;
import stream.rocketnotes.repository.FirestoreRepository;

public class CloudUtils {
    public static void SaveNoteCloud(FirestoreRepository firestoreRepository, NotesItem notesItem) {
        FirestoreInterface firestoreInterface = new FirestoreInterface() {
            @Override
            public void onSuccess() {

            }

            @Override
            public OnFailureListener getFailureListener() {
                return new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                };
            }
        };
        firestoreRepository.AddNote(notesItem, firestoreInterface);
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = null;
        if (cm != null) {
            netInfo = cm.getActiveNetworkInfo();
        }

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
