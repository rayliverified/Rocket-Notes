package stream.rocketnotes.repository;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import stream.rocketnotes.DatabaseHelper;
import stream.rocketnotes.NotesItem;
import stream.rocketnotes.interfaces.FirestoreInterface;

public class FirestoreRepository {

    private final String TAG = this.getClass().getSimpleName();

    public FirebaseFirestore firestoreDB;

    public FirestoreRepository(Context context) {
        firestoreDB = FirebaseFirestore.getInstance();
    }

    public void AddNote(NotesItem notesItem, FirestoreInterface firestoreInterface) {
        Map<String, Object> item = new HashMap<>();
        item.put(DatabaseHelper.KEY_ID, notesItem.getID());
        item.put(DatabaseHelper.KEY_DATE, notesItem.getDate());
        item.put(DatabaseHelper.KEY_NOTE, notesItem.getNote());
        item.put(DatabaseHelper.KEY_IMAGE, notesItem.getImage());
        item.put(DatabaseHelper.KEY_IMAGEPREVIEW, notesItem.getImagePreview());

        firestoreDB.collection(DatabaseHelper.TABLE_NOTES).add(item)
                .addOnSuccessListener(firestoreInterface.getSuccessListener())
                .addOnFailureListener(firestoreInterface.getFailureListener());
    }

    public FirebaseFirestore getFirestoreDB() {
        return firestoreDB;
    }

    public void setFirestoreDB(FirebaseFirestore firestoreDB) {
        this.firestoreDB = firestoreDB;
    }
}
