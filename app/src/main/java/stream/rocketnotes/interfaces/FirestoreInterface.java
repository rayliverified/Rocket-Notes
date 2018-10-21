package stream.rocketnotes.interfaces;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

public interface FirestoreInterface {

    OnSuccessListener<DocumentReference> getSuccessListener();
    OnFailureListener getFailureListener();
}
