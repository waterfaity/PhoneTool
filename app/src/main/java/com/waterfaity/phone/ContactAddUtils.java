package com.waterfaity.phone;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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


    /**
     * 批量添加通讯录
     *
     * @throws OperationApplicationException
     * @throws RemoteException
     */
    public static void addContactList(Context context, List<ContactEntity> list, int perLimit) throws OperationApplicationException, RemoteException {


        if (list.size() > perLimit) {
            int num = list.size() / perLimit + (list.size() % perLimit == 0 ? 0 : 1);
            for (int i = 0; i < num; i++) {
                int from = i * perLimit;
                int end = from + perLimit;

                if (end > list.size()) {
                    end = list.size();
                }
                addContactSubList(context, list.subList(from, end));
            }
        } else {
            addContactSubList(context, list);
        }
    }

    private static void addContactSubList(Context context, List<ContactEntity> subList) throws OperationApplicationException, RemoteException {

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = 0;
        for (ContactEntity contact : subList) {
            rawContactInsertIndex = ops.size(); // 有了它才能给真正的实现批量添加

            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .withYieldAllowed(true).build());

            // 添加姓名
            ops.add(ContentProviderOperation
                    .newInsert(
                            ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID,
                            rawContactInsertIndex)
                    .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName())
                    .withYieldAllowed(true).build());
            // 添加号码
            ops.add(ContentProviderOperation
                    .newInsert(
                            ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                            rawContactInsertIndex)
                    .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhone())
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, "").withYieldAllowed(true).build());
        }
        // 真正添加
        context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
    }


    public static void deleteContact(Context context) {
//        String name = "员工";
//        //根据姓名求id
//        Uri uri = ContactsContract.RawContacts.CONTENT_URI;
//        ContentResolver resolver = context.getContentResolver();
//        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.RawContacts.Data._ID}, " display_name like ? ", new String[]{name}, null);
//        if (cursor.moveToFirst()) {
//            int id = cursor.getInt(0);
//            //根据id删除data中的相应数据
//            resolver.delete(uri, " display_name like ?", new String[]{name});
//            uri = ContactsContract.Data.CONTENT_URI;
//            resolver.delete(uri, " raw_contact_id=? ", new String[]{id + ""});
//            Log.i("TAG", "deleteContact: ");
//        }

        try {
            Cursor cursor = context.getContentResolver()
                    .query(
                            ContactsContract.Data.CONTENT_URI,
                            //需要拿到的参数
                            new String[]{
                                    ContactsContract.Data._ID,
                                    ContactsContract.Data.RAW_CONTACT_ID,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                            },
                            ContactsContract.Data.MIMETYPE + " = ? AND " +
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ? ",
                            new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, "员工%"},
                            null);


            ArrayList<MainBean> deleteDataList = new ArrayList<>();


            while (cursor.moveToNext()) {
                String raw_contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
                String data_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
                deleteDataList.add(new MainBean(data_id, raw_contact_id));
            }
            delete(context, deleteDataList, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static void delete(Context context, ArrayList<MainBean> deleteDataList, int perLimit) {

        if (deleteDataList.size() > perLimit) {
            int num = deleteDataList.size() / perLimit + (deleteDataList.size() % perLimit == 0 ? 0 : 1);
            for (int i = 0; i < num; i++) {
                int from = i * perLimit;
                int end = from + perLimit;

                if (end > deleteDataList.size()) {
                    end = deleteDataList.size();
                }
                delete(context, getSubDeleteArrayList(deleteDataList.subList(from, end)));
            }
        } else {
            delete(context, deleteDataList);
        }

    }

    private static ArrayList<MainBean> getSubDeleteArrayList(List<MainBean> subList) {
        ArrayList<MainBean> arrayList = new ArrayList<>();
        arrayList.addAll(subList);
        return arrayList;
    }

    private static void delete(Context context, ArrayList<MainBean> deleteDataList) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (int i = 0; i < deleteDataList.size(); i++) {
            MainBean mainBean = deleteDataList.get(i);
            ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection(ContactsContract.RawContacts._ID + " = ? ", new String[]{mainBean.raw_contact_id}).build());
            ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data._ID + " = ? ", new String[]{mainBean.data_id}).build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static class MainBean {
        public MainBean(String data_id, String raw_contact_id) {
            this.data_id = data_id;
            this.raw_contact_id = raw_contact_id;
        }

        private String data_id;
        private String raw_contact_id;
    }

    public static void delete(Context context) {
        int dataDelete = context.getContentResolver().delete(ContactsContract.Data.CONTENT_URI,
                ContactsContract.Data.DISPLAY_NAME + " LIKE ? "
                        + " and " + ContactsContract.Data.MIMETYPE + " = ? "
                , new String[]{"员工%", ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE});

        int rawDelete = context.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI,
                ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY + " LIKE ? ",
                new String[]{"员工%"});

    }
}
