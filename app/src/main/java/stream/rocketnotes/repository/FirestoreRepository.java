package stream.rocketnotes.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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
import stream.rocketnotes.utils.FileUtils;

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

    public void RestoreNotes(Task<QuerySnapshot> task) {
        try {
            for (QueryDocumentSnapshot document : task.getResult()) {
                String cloudID = (String) document.get(DatabaseHelper.KEY_CLOUDID);
                Log.d("Cloud ID", cloudID);
                if (cloudID != null) {
                    firestoreDB.collection("users").document(userID).collection(DatabaseHelper.TABLE_NOTES).document(cloudID).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.getResult() != null) {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        Integer id = documentSnapshot.get(DatabaseHelper.KEY_ID) != null ? ((Long) documentSnapshot.get(DatabaseHelper.KEY_ID)).intValue() : null;
                                        Long date = (Long) documentSnapshot.get(DatabaseHelper.KEY_DATE);
                                        if (id != null && date != null) {
                                            NotesItem note = dbHelper.GetNote(id);
                                            if (note.getDate() != null) {
                                                if (note.getDate() < date) {
                                                    String imagePath = (String) documentSnapshot.get(DatabaseHelper.KEY_IMAGE);
                                                    String imagePreviewPath = (String) documentSnapshot.get(DatabaseHelper.KEY_IMAGEPREVIEW);
                                                    NotesItem noteItem = new NotesItem(id,
                                                            date,
                                                            (String) documentSnapshot.get(DatabaseHelper.KEY_NOTE),
                                                            imagePath,
                                                            imagePreviewPath,
                                                            documentSnapshot.getReference().getId());
                                                    dbHelper.UpdateOrInsertNote(noteItem);
                                                    if (imagePath != null) {
                                                        DownloadFile(imagePath);
                                                    }
                                                    if (imagePreviewPath != null) {
                                                        DownloadFile(imagePreviewPath);
                                                    }
                                                }
                                            } else {
                                                String imagePath = (String) documentSnapshot.get(DatabaseHelper.KEY_IMAGE);
                                                String imagePreviewPath = (String) documentSnapshot.get(DatabaseHelper.KEY_IMAGEPREVIEW);
                                                NotesItem noteItem = new NotesItem(id,
                                                        date,
                                                        (String) documentSnapshot.get(DatabaseHelper.KEY_NOTE),
                                                        imagePath,
                                                        imagePreviewPath,
                                                        documentSnapshot.getReference().getId());
                                                dbHelper.UpdateOrInsertNote(noteItem);
                                                if (imagePath != null) {
                                                    DownloadFile(imagePath);
                                                }
                                                if (imagePreviewPath != null) {
                                                    DownloadFile(imagePreviewPath);
                                                }
                                            }
                                        }
                                        Log.d("Restored: ", String.valueOf(id));
                                    }
                                }
                            });
                }
            }
        } catch (Exception e) {
            Log.e("Error deleting: ", e.getMessage());
        }
    }

    public void DownloadFile(String downloadPath) {
        try {
            StorageReference imageRef = firebaseStorage.getReference().child(Constants.FIRESTORE_COLLECTION_USERS).child(userID).child(FileUtils.GetFileNameFromPath(downloadPath));
            File imageFile = new File(new URI(downloadPath));
            imageRef.getFile(imageFile);
        } catch (URISyntaxException e) {
            e.printStackTrace();
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
