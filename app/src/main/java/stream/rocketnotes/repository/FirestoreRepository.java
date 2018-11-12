package stream.rocketnotes.repository;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

import stream.rocketnotes.Constants;
import stream.rocketnotes.DatabaseHelper;
import stream.rocketnotes.NotesItem;
import stream.rocketnotes.interfaces.FirestoreInterface;

public class FirestoreRepository {

    private final String TAG = this.getClass().getSimpleName();

    public String userID = "";

    public FirebaseFirestore firestoreDB;
    public DatabaseHelper dbHelper;

    public FirestoreRepository(Context context, String userID, DatabaseHelper dbHelper) {
        this.userID = userID;
        if (firestoreDB == null) {
            firestoreDB = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build();
            firestoreDB.setFirestoreSettings(settings);
        }

        this.dbHelper = dbHelper;
    }

    public void AddNote(final NotesItem notesItem, final FirestoreInterface firestoreInterface) {
        Map<String, Object> item = new HashMap<>();
        item.put(DatabaseHelper.KEY_ID, notesItem.getID());
        item.put(DatabaseHelper.KEY_DATE, notesItem.getDate());
        item.put(DatabaseHelper.KEY_NOTE, notesItem.getNote());
        item.put(DatabaseHelper.KEY_IMAGE, notesItem.getImage());
        item.put(DatabaseHelper.KEY_IMAGEPREVIEW, notesItem.getImagePreview());
        item.put(Constants.FIREBASE_USER_ID, userID);

        firestoreDB.collection(DatabaseHelper.TABLE_NOTES).add(item)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(final DocumentReference documentReference) {
                        Map<String, Object> item = new HashMap<>();
                        item.put(DatabaseHelper.KEY_ID, notesItem.getID());
                        item.put(DatabaseHelper.KEY_CLOUDID, documentReference.getId());
                        firestoreDB.collection("users").document(userID).collection("notesindex").document(notesItem.getID().toString()).set(item)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        NotesItem updateNote = notesItem;
                                        updateNote.setCloudId(documentReference.getId());
                                        dbHelper.UpdateNote(updateNote);
                                        firestoreInterface.onSuccess();
                                    }
                                })
                                .addOnFailureListener(firestoreInterface.getFailureListener());
                    }
                })
                .addOnFailureListener(firestoreInterface.getFailureListener());
    }

    public FirebaseFirestore getFirestoreDB() {
        return firestoreDB;
    }

    public void setFirestoreDB(FirebaseFirestore firestoreDB) {
        this.firestoreDB = firestoreDB;
    }
}
