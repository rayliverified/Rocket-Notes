package stream.rocketnotes.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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
    public FirebaseStorage firebaseStorage;
    public DatabaseHelper dbHelper;

    public FirestoreRepository(Context context, String userID) {
        this(context, userID, null);
    }

    public FirestoreRepository(Context context, String userID, DatabaseHelper dbHelper) {
        this.userID = userID;
        if (firestoreDB == null) {
            firestoreDB = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build();
            firestoreDB.setFirestoreSettings(settings);
        }

        if (firebaseStorage == null) {
            firebaseStorage = FirebaseStorage.getInstance();
        }

        if (dbHelper == null) {
            this.dbHelper = new DatabaseHelper(context);
        } else {
            this.dbHelper = dbHelper;
        }
    }

    /**
     * Backup note to Firestore and uploads image to Firebase Storage.
     *
     * @param notesItem          - note data to backup.
     * @param firestoreInterface - success and failure callback for backup status.
     */
    public void SaveNoteCloud(final NotesItem notesItem, final FirestoreInterface firestoreInterface) {
        if (notesItem.getImage() != null) {
            Log.d(TAG, "Upload Image: " + notesItem.getImage());
            try {
                File imageTemp = null;
                imageTemp = new File(new URI(notesItem.getImage()));
                if (imageTemp.exists()) {
                    Log.d(TAG, "Image exists.");
                    FirestoreInterface imageUploadInterface = new FirestoreInterface() {
                        @Override
                        public void onSuccess() {
                            AddNote(notesItem, firestoreInterface);
                        }

                        @Override
                        public OnFailureListener getFailureListener() {
                            return firestoreInterface.getFailureListener();
                        }
                    };
                    UploadImage(notesItem, imageUploadInterface);
                } else {
                    Log.d(TAG, "Image does not exist.");
                    firestoreInterface.getFailureListener().onFailure(new Exception("Image does not exist."));
                }
            } catch (URISyntaxException e) {
                Log.d(TAG, "Image does not exist.");
                firestoreInterface.getFailureListener().onFailure(new Exception("Image does not exist."));
            }
        } else {
            AddNote(notesItem, firestoreInterface);
        }
    }

    /**
     * Backup note to Firestore. Adds note to user's note collection and updates notes index with ID of latest note.
     * Updates local
     *
     * @param notesItem          - note data to backup.
     * @param firestoreInterface - success and failure callback for backup status.
     */
    public void AddNote(final NotesItem notesItem, final FirestoreInterface firestoreInterface) {
        Map<String, Object> item = new HashMap<>();
        item.put(DatabaseHelper.KEY_ID, notesItem.getID());
        item.put(DatabaseHelper.KEY_DATE, notesItem.getDate());
        item.put(DatabaseHelper.KEY_NOTE, notesItem.getNote());
        item.put(DatabaseHelper.KEY_IMAGE, notesItem.getImage());
        item.put(DatabaseHelper.KEY_IMAGEPREVIEW, notesItem.getImagePreview());
        item.put(Constants.FIRESTORE_USER_ID, userID);

        firestoreDB.collection(Constants.FIRESTORE_COLLECTION_USERS).document(userID).collection(DatabaseHelper.TABLE_NOTES).add(item)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(final DocumentReference documentReference) {
                        Map<String, Object> item = new HashMap<>();
                        item.put(DatabaseHelper.KEY_ID, notesItem.getID());
                        item.put(DatabaseHelper.KEY_CLOUDID, documentReference.getId());
                        firestoreDB.collection(Constants.FIRESTORE_COLLECTION_USERS).document(userID).collection("notesindex").document(notesItem.getID().toString()).set(item)
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

    public void UploadImage(final NotesItem notesItem, final FirestoreInterface firestoreInterface) {
        if (notesItem.getImage() == null) {
            return;
        }

        try {
            Uri imageFile = Uri.parse(notesItem.getImage());
            StorageReference imageRef = firebaseStorage.getReference().child(Constants.FIRESTORE_COLLECTION_USERS).child(userID).child(imageFile.getLastPathSegment());
            UploadTask uploadImageTask = imageRef.putFile(imageFile);
            uploadImageTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (notesItem.getImagePreview() == null) {
                        firestoreInterface.onSuccess();
                    } else {
                        Uri imagePreviewFile = Uri.parse(notesItem.getImagePreview());
                        StorageReference imagePreviewRef = firebaseStorage.getReference().child(Constants.FIRESTORE_COLLECTION_USERS).child(userID).child(imagePreviewFile.getLastPathSegment());
                        UploadTask uploadImagePreviewTask = imagePreviewRef.putFile(imagePreviewFile);
                        uploadImagePreviewTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                firestoreInterface.onSuccess();
                            }
                        }).addOnFailureListener(firestoreInterface.getFailureListener());
                    }
                }
            }).addOnFailureListener(firestoreInterface.getFailureListener());
        } catch (NullPointerException e) {
            Log.e(TAG, String.valueOf(e));
        }
    }

    public FirebaseFirestore getFirestoreDB() {
        return firestoreDB;
    }

    public void setFirestoreDB(FirebaseFirestore firestoreDB) {
        this.firestoreDB = firestoreDB;
    }

    public FirebaseStorage getFirebaseStorage() {
        return firebaseStorage;
    }

    public void setFirebaseStorage(FirebaseStorage firebaseStorage) {
        this.firebaseStorage = firebaseStorage;
    }
}
