package com.waterfaity.phone;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactAddUtils {


    /**
     * 添加联系人到本机
     *
     * @param context
     * @param contact
     * @return
     */
    public static boolean addContact(Context context, ContactEntity contact) {
        try {
            ContentValues values = new ContentValues();

            // 下面的操作会根据RawContacts表中已有的rawContactId使用情况自动生成新联系人的rawContactId
            Uri rawContactUri = context.getContentResolver().insert(
                    ContactsContract.RawContacts.CONTENT_URI, values);
            long rawContactId = ContentUris.parseId(rawContactUri);

            // 向data表插入姓名数据
            String name = contact.getName();
            if (name != "") {
                values.clear();
                values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
            }

            // 向data表插入电话数据
            String mobile_number = contact.getPhone();
            if (mobile_number != "") {
                values.clear();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, mobile_number);
                values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                context.getContentResolver().insert(
                        ContactsContract.Data.CONTENT_URI, values);
            }

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 添加联系人到本机
     *
     * @param context
     * @param phone
     * @return
     */
    public static void deleteContact(Context context, String phone) {

        Uri parse = Uri.parse("content://com.android.contacts/data");

        Uri rawUri = ContactsContract.RawContacts.CONTENT_URI;
        Cursor query = context.getContentResolver().query(parse, new String[]{ContactsContract.Data._ID},
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?", new String[]{phone}, null);
        if (query.moveToNext()) {
            int id = query.getInt(0);
            context.getContentResolver().delete(rawUri, "display_number=?", new String[]{phone});
            int state = context.getContentResolver().delete(parse, "raw_contact_id = ? "
                    , new String[]{id + ""});
            Log.i("PhotoUtils", "deleteContact: " + state);

        }

    }
}
