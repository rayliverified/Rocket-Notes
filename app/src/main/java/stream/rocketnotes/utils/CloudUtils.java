package stream.rocketnotes.utils;

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
}
