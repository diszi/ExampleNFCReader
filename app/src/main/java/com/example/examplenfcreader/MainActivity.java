package com.example.examplenfcreader;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;

    ToggleButton tgl;
    EditText tx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        tgl = (ToggleButton)findViewById(R.id.tglReadWrite);
        tx = (EditText) findViewById(R.id.textTagContent);

       //  NFC verification -> available OR not
//        if (nfcAdapter != null && nfcAdapter.isEnabled()){
//            Toast.makeText(this," NFC available!",Toast.LENGTH_SHORT).show();
//        }else{
//            Toast.makeText(this," NFC not available!",Toast.LENGTH_SHORT).show();
//            finish();
//        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        /**
         * Write text to an NFC tag ---> createNdefMessage method + writeNdefMessage method
         */
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)){
            Toast.makeText(this," NFC intent",Toast.LENGTH_SHORT).show();


            if (tgl.isChecked()){
                //read
                System.out.println(" checked = true");
                Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                System.out.println(" parcelables.length = "+parcelables.length);

                if (parcelables != null && parcelables.length >0){
                    readTextFromMessage((NdefMessage) parcelables[0]);

                    Toast.makeText(this,"NDEF message exists",Toast.LENGTH_SHORT).show();

                    Intent secondIntent = new Intent(this,SecondActivity.class);
                    startActivity(secondIntent);
                }else
                {
                    Toast.makeText(this,"No NDEF message found",Toast.LENGTH_SHORT).show();
                }

            }else{ //write
                System.out.println(" WRITE");
                Tag tag  = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                System.out.println(" message  =" +tx.getText());
                NdefMessage ndefMessage = createNdefMessage(tx.getText()+"");

                writeNdefMessage(tag,ndefMessage);
            }


        }//end of wirte Text to an NFC tag


    }


    private void readTextFromMessage(NdefMessage ndefMessage){
        NdefRecord[] ndefRecords = ndefMessage.getRecords();

        if (ndefRecords != null && ndefRecords.length > 0){
            NdefRecord ndefRecord = ndefRecords[0];

            String tagContetn = getTextFromNdfRecord(ndefRecord);
            System.out.println(" readTextFromMessage ---> tagContent = "+tagContetn); //ezt kell megjegyezni s tovabb adni
            tx.setText(tagContetn);

        }else{
            Toast.makeText(this,"No NDEF records found",Toast.LENGTH_SHORT).show();
        }
    }

    public String getTextFromNdfRecord(NdefRecord ndefRecord){
        String tagContent  = null;

        try{
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128 ) == 0)? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent =  new String(payload,languageSize+1,payload.length-languageSize-1,textEncoding);

        }catch (UnsupportedEncodingException e){
            Log.e("getTextFromNdefRecord",e.getMessage(),e);
        }
        return tagContent;
    }



    @Override
    protected void onResume() {
       // enableForegroundDispatchSystem
        Intent intent = new Intent(this,MainActivity.class);

        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        IntentFilter[] intentFilter = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this,pendingIntent,intentFilter,null);

        super.onResume();
    }

    @Override
    protected void onPause() {
        nfcAdapter.disableForegroundDispatch(this);

        super.onPause();
    }



    private void formatTag(Tag tag, NdefMessage ndefMessage){
        try{
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            if (ndefFormatable == null){
                Toast.makeText(this,"Tag is not ndef formatable",Toast.LENGTH_SHORT).show();
            }
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();

            Toast.makeText(this,"Tag writen",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e("formatTAg",e.getMessage());
        }
    }

    private void writeNdefMessage(Tag tag, NdefMessage msg){
        try{
            System.out.println(" msg = "+msg+"   tag = "+tag.toString());

            if (tag == null){
                Toast.makeText(this,"Tag object cannot be null",Toast.LENGTH_SHORT).show();
                return;

            }

            Ndef ndef = Ndef.get(tag);
            System.out.println(" ndef = "+ndef);
            if (ndef == null){
                //format tag with the ndef format and writes the message
                formatTag(tag,msg);
            }else{
                ndef.connect();
                if (!ndef.isWritable()){
                    Toast.makeText(this,"Tag is not writable",Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(msg);
                ndef.close();

                Toast.makeText(this,"Tag writen",Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Log.e("writeNdefMessage",e.getMessage());
        }
    }



    private NdefMessage createNdefMessage(String content){

        NdefRecord ndefRecord = createTextRecord(content);

        System.out.println(" createNdefMessage ---> Content = "+content);

        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});

        return ndefMessage;
    }

    private NdefRecord createTextRecord(String content){
        try{
            byte[] language;
            language = Locale.getDefault().getLanguage().getBytes("UTF-8");

            final byte[] text = content.getBytes("UTF-8");
            final int languageSize = language.length;
            final int textLength = text.length;
            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1+languageSize+textLength);

            payload.write((byte)(languageSize & 0x1F));
            payload.write(language,0,languageSize);
            payload.write(text,0,textLength);

            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_TEXT,new byte[0],payload.toByteArray());

        }catch (UnsupportedEncodingException e){
            Log.e("createTextRecord",e.getMessage());
        }
        return null;
    }

    //lefuttatas: NFC intent > Tag writen


    //READ+Write ToggleButton
    public void tglReadWriteOnClick(View view){
//        tx.setText("");
    }



}
