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

import static ipleiria.project.add.view.google_sign_in.GoogleSignInPresenter.SCOPES;

/**
 * Created by Lisboa on 17-May-17.
 */

public class RequestMailsTask extends AsyncTask<Void, Void, List<ItemFile>> {

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

        void onComplete(List<ItemFile> pendingFiles);

    }

    @Override
    protected void onPostExecute(List<ItemFile> pendingFiles) {
        super.onPostExecute(pendingFiles);
        callback.onComplete(pendingFiles);
    }

    @Override
    protected List<ItemFile> doInBackground(Void... params) {
        String user = "me";
        List<ItemFile> attachments = new ArrayList<>();

        String[] emailAux = userEmail.split("@");
        String email = emailAux[0] + "+addestg@" + emailAux[1];

        try {
            ListMessagesResponse listResponse =
                    gmailService.users().messages().list(user).setQ("to:" + email).execute();
            System.out.println(listResponse.getMessages());
            for (int i = 0; i < listResponse.size(); i++) {
                Message m = gmailService.users().messages().get(user, listResponse.getMessages().get(i).getId()).execute();

                /* Message message = gmailService.users().messages().get(user, listResponse.getMessages().get(i).getId()).setFormat("raw").execute();
                try {
                    byte[] emailBytes = Base64.decodeBase64(message.getRaw());

                    Properties props = new Properties();
                    Session session = Session.getDefaultInstance(props, null);
                    MimeMessage eml = new MimeMessage(session, new ByteArrayInputStream(emailBytes));
                    File emlFile = new File(Application.getAppContext().getFilesDir(), "test.eml");
                    eml.writeTo(new FileOutputStream(emlFile));

                } catch (MessagingException e) {
                    e.printStackTrace();
                }*/

                List<MessagePart> parts = m.getPayload().getParts();
                for (MessagePart part : parts) {
                    if (part.getFilename() != null && part.getFilename().length() > 0) {
                        String filename = part.getFilename();
                        String attId = part.getBody().getAttachmentId();

                        MessagePartBody attachPart = gmailService.users().messages().attachments().
                                get(user, m.getId(), attId).execute();

                        byte[] fileByteArray = Base64.decodeBase64(attachPart.getData());
                        filesRepository.saveEmailAttachment(filename, fileByteArray);
                        attachments.add(new ItemFile(filename));
                    }
                }
            }
        } catch (UserRecoverableAuthIOException userRecoverableException) {
            // user didn't give permissions when signing up - ignore
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return attachments;
    }
}
