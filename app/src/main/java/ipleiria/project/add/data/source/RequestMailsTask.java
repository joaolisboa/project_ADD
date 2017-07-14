package ipleiria.project.add.data.source;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.StringUtils;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.sql.DataSource;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.PendingFile;

import static ipleiria.project.add.view.google_sign_in.GoogleSignInPresenter.SCOPES;

/**
 * Created by Lisboa on 17-May-17.
 */

public class RequestMailsTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "REQUEST_MAIL_TASK";

    private MailCallback callback;

    private FilesRepository filesRepository;
    private Gmail gmailService;
    private String userEmail;

    public RequestMailsTask(Gmail gmailService, FilesRepository filesRepository,
                            String userEmail, MailCallback callback) {
        this.gmailService = gmailService;
        this.filesRepository = filesRepository;
        this.callback = callback;
        this.userEmail = userEmail;
    }

    public interface MailCallback {

        void onEmailAdded(PendingFile pendingEmail);

        void onError();

    }

    @Override
    protected Void doInBackground(Void... params) {
        String user = "me";

        String[] emailAux = userEmail.split("@");
        String email = emailAux[0] + "+addestg@" + emailAux[1];

        try {
            ListMessagesResponse listResponse = gmailService.users().messages().list(user).setQ("to:" + email).execute();
            if(listResponse.getMessages() == null){
                throw new IOException("No messages found");
            }
            for (int i = 0; i < listResponse.getMessages().size(); i++) {
                LinkedList<String> meta = new LinkedList<>();
                meta.add("Subject");
                Message header = gmailService.users()
                        .messages().get(user, listResponse.getMessages().get(i).getId())
                        .setMetadataHeaders(meta)
                        .setFormat("metadata").execute();

                String subject = header.getPayload().getHeaders().get(0).getValue();
                String filename = "Assunto: " + subject + ".eml";
                File emlFile = new File(Application.getAppContext().getFilesDir(), filename);
                // avoid redownloading emails
                if(emlFile.exists()){
                    PendingFile emailFile = new PendingFile(new ItemFile(filename), PendingFile.EMAIL);
                    callback.onEmailAdded(emailFile);
                    continue;
                }

                Message message = gmailService.users()
                        .messages().get(user, listResponse.getMessages().get(i).getId())
                        .setFormat("raw").execute();
                try {
                    byte[] emailBytes = Base64.decodeBase64(message.getRaw());

                    Properties props = new Properties();
                    Session session = Session.getDefaultInstance(props, null);
                    MimeMessage eml = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

                    eml.writeTo(new FileOutputStream(emlFile));
                    PendingFile emailFile = new PendingFile(new ItemFile(filename), PendingFile.EMAIL);
                    callback.onEmailAdded(emailFile);

                } catch (MessagingException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        } catch (UserRecoverableAuthIOException userRecoverableException) {
            // user didn't give permissions when signing up - catch and ignore
            Log.d(TAG, "User didn't give permission to access to Gmail");
            callback.onError();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            callback.onError();
        }

        return null;
    }
}
