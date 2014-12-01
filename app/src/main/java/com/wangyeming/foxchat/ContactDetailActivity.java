package com.wangyeming.foxchat;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wangyeming.custom.ContactDetailAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI;


public class ContactDetailActivity extends Activity {

    protected List<Map<String, Object>> ContactDisplay = new ArrayList<Map<String, Object>>();
    protected ListView lt2;
    protected TextView tv1;
    protected String contactName;
    protected Long ContactId;
    protected Long RawContactId;
    protected Uri photo_uri;
    protected ContentResolver cr;
    protected Button starButton;
    protected TextView starTextView;
    protected boolean isStarred = false;
    private static final String[] PHONES_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, //display_name
            ContactsContract.CommonDataKinds.Phone.NUMBER, //data1
            ContactsContract.CommonDataKinds.Photo.PHOTO_ID, //photo_id
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI,//
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,  //contact_id
            ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY, //sort_key
            ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.STARRED
    };
    private static final Map<String, String> PHONE_TYPE = new HashMap<String, String>() {
        {
            put("0", "自定义");
            put("1", "住宅");
            put("2", "手机");
            put("3", "单位");
            put("4", "单位传真");
            put("5", "住宅传真");
            put("6", "寻呼机");
            put("7", "其他");
            put("12", "总机");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        init();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contact_detail, menu);
        return true;
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        clearData(); //清除缓存数据
        Intent intent = getIntent();
        ContactId = intent.getLongExtra("ContactId", 1);
        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void init() {
        cr = getContentResolver();
        getContactMessage();//获取联系人信息
        displayListView(); //显示listView
        displayStarred(); //设置收藏/未收藏的图标
    }

    public void clearData() {
        ContactDisplay.clear();
    }

    //读取联系人信息
    public void getContactMessage() {
        Intent intent = getIntent();
        ContactId = intent.getLongExtra("ContactId", 1);
        //System.out.println(ContactId);
        readContactName(ContactId);
        photo_uri = readContactPhoneBim(ContactId);
        readContactPhoneNum(ContactId);
    }

    //读取联系人姓名
    public void readContactName(Long ContactId) {
        Cursor cursorID = getContentResolver().query(CONTENT_URI, PHONES_PROJECTION, PHONES_PROJECTION[4] + "=" + ContactId, null, "sort_key");
        cursorID.moveToNext();
        contactName = cursorID.getString(cursorID.getColumnIndex(PHONES_PROJECTION[0]));
        cursorID.close();
    }

    //读取联系人头像
    public Uri readContactPhoneBim(Long ContactId) {
        Cursor cursorID = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION, PHONES_PROJECTION[4] + "=" + ContactId, null, "sort_key");
        cursorID.moveToFirst();
        RawContactId = cursorID.getLong(cursorID.getColumnIndex(PHONES_PROJECTION[6]));
        System.out.println(RawContactId);
        int starred = cursorID.getInt(cursorID.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED));
        System.out.println("starred " + starred);
        isStarred = starred == 1 ? true : false;
        String photo_string = cursorID.getString(cursorID.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI));
        //System.out.println("this 3" +photo_string);
        Uri photoUri;
        if (photo_string == null) {
            //没有头像
            photoUri = Uri.parse("content://com.android.contacts/display_photo/38");
        } else {
            photoUri = Uri.parse(photo_string);
        }
        //InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), photo_uri);
        //Bitmap bmp_head = BitmapFactory.decodeStream(input);
        ImageView imageView = (ImageView) findViewById(R.id.pic1);
        imageView.setImageURI(photoUri);
        cursorID.close();
        return photoUri;
    }

    //读取联系人手机号
    public void readContactPhoneNum(Long ContactId) {
        ContentResolver cr = getContentResolver();//得到ContentResolver对象
        Cursor phoneID = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                PHONES_PROJECTION[4] + "=" + ContactId, null, null);//设置手机号光标
        while (phoneID.moveToNext()) {
            Map<String, Object> PhoneNumMap = new HashMap<String, Object>();
            String phoneNumber = phoneID.getString(phoneID.getColumnIndex(PHONES_PROJECTION[1]));
            String phoneNumberType = phoneID.getString(phoneID.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            //System.out.println(phoneNumberType);
            String phoneNumberTypeTrans = PHONE_TYPE.get(phoneNumberType);
            System.out.println("手机号： " + phoneNumber + " 手机号类型： " + phoneNumberType + " ");
            //PhoneNumMap.put("phone_icon", phoneIconMap.get(isFirstNum));
            PhoneNumMap.put("phone_png", R.drawable.type_icon_phone);
            PhoneNumMap.put("phone_num", phoneNumber);
            PhoneNumMap.put("phone_type", phoneNumberTypeTrans);
            PhoneNumMap.put("phone_location", "北京");
            PhoneNumMap.put("message_png", R.drawable.ic_send_sms_p);
            ContactDisplay.add(PhoneNumMap);
        }
        phoneID.close();
    }

    //设置lisView布局
    public void displayListView() {
        tv1 = (TextView) findViewById(R.id.contactName);
        tv1.setText(contactName);
        lt2 = (ListView) findViewById(R.id.list_contact_phone_display);
        if (ContactDisplay == null) {
            //System.out.println("ContactDisplay is nil");
        }
        System.out.println(ContactDisplay.size());
        ContactDetailAdapter adapter = new ContactDetailAdapter(ContactDisplay, this);
        lt2.setAdapter(adapter);
    }

    //设置收藏/未收藏的图标
    public void displayStarred() {
        starButton = (Button) findViewById(R.id.starButton);
        starTextView = (TextView) findViewById(R.id.starText);
        if (isStarred) {
            starButton.setBackground(this.getResources().getDrawable(R.drawable.favorite_icon_normal_dark));
            starTextView.setText("取消收藏");
        } else {
            starButton.setBackground(this.getResources().getDrawable(R.drawable.unfavorite_icon_normal_dark));
            starTextView.setText("收藏");
        }
    }

    //返回主页面按钮
    public void backToMain(View view) {
        Intent intent = new Intent(this, LineActivity.class);
        startActivity(intent);
    }

    //编辑联系人详细信息
    public void editContactDetail(View view) {
        Intent intent = new Intent(this, EditContactDetailActivity.class);
        intent.putExtra("ContactId", ContactId);
        intent.putExtra("RawContactId",RawContactId);
        intent.setData(photo_uri);
        intent.putExtra("ContactDisplay", (Serializable) ContactDisplay);
        intent.putExtra("contactName", contactName);
        startActivity(intent);
    }

    //发送联系人详细信息
    public void sendContactDetail(View view) {

    }

    //收藏联系人
    public void starContact(View view) {
        // Uri rawContactUri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, RawContactId);
        Uri ContactUri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, ContactId);
        ContentValues values = new ContentValues();
        if (isStarred) {
            values.put(ContactsContract.CommonDataKinds.Phone.STARRED, 0);
            starButton.setBackground(this.getResources().getDrawable(R.drawable.unfavorite_icon_normal_dark));
            starTextView.setText("收藏");
            cr.update(ContactUri, values, null, null);
            //String Where = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND   " + ContactsContract.Data.MIMETYPE + " = ?";
            // cr.update(rawContactUri, values, null, null);
            isStarred = false;
        } else {
            values.put(ContactsContract.CommonDataKinds.Phone.STARRED, 1);
            starButton.setBackground(this.getResources().getDrawable(R.drawable.favorite_icon_normal_dark));
            starTextView.setText("取消收藏");
            cr.update(ContactUri, values, null, null);
            //String Where = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND   " + ContactsContract.Data.MIMETYPE + " = ?";
            //cr.update(rawContactUri, values, null, null);
            isStarred = true;
        }
    }
}
