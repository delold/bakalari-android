package cz.duong.skolar.server;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import cz.duong.skolar.utils.DatabaseHandler;

/**
 * Created by David on 10. 4. 2014.
 */
public class Users {

    protected DatabaseHandler handler;

    private static final String TABLE_USERS = "users";

    public Users(Context context) {
        handler = DatabaseHandler.getInstance(context);
    }

    public class User implements Parcelable {
        public final Integer id;
        public final String user;
        public final String pass;
        public final String url;

        public String name = "";
        public String title = "";

        public User(Integer id, String user, String pass, String url) {
            this(id, user, pass, url, "", "");
        }

        public User(Integer id, String user, String pass, String url, String name, String title) {
            this.id = id;
            this.user = user;
            this.pass = pass;
            this.url = url;

            this.name = name;
            this.title = title;
        }

        public User(Parcel source) {
            this.id = source.readInt();
            this.user = source.readString();
            this.pass = source.readString();
            this.url = source.readString();

            this.name = source.readString();
            this.title = source.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(user);
            dest.writeString(pass);
            dest.writeString(url);

            dest.writeString(name);
            dest.writeString(title);
        }

        public final Parcelable.Creator CREATOR = new Parcelable.Creator<User>() {
            public User createFromParcel(Parcel source) {
                return new User(source);
            }
            public User[] newArray(int size) {
                return new User[size];
            }
        };
    }

    public List<User> getUsers() {
        SQLiteDatabase db = handler.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_USERS, null);
        List<User> result = new ArrayList<User>();

        while(cursor.moveToNext()) {
            result.add(new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
            ));
        }

        /*if(cursor.moveToFirst()) {

            do {
                result.add(new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
                ));

            } while (cursor.moveToFirst());
        }*/

        return result;
    }

    public User getUser(Integer id) {
        SQLiteDatabase db = handler.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, null, "id =?", new String[] { id.toString() }, null, null, null);

        if(cursor != null) {
            cursor.moveToFirst();
        }

        return new User(
            cursor.getInt(0),
            cursor.getString(1),
            cursor.getString(2),
            cursor.getString(3),
            cursor.getString(4),
            cursor.getString(5)
        );
    }

    public int getFirstID() {
        SQLiteDatabase db = handler.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[] { "id" }, null, null, null, null, null);

        if(cursor != null) {
            cursor.moveToFirst();

            return cursor.getInt(0);
        }

        return 0;
    }

    public User getCurrentUser() {
        return this.getUser(this.getFirstID());
        //return new User(0, "971031r", "dfiypam4", "http://intranet.wigym.cz:6040/bakaweb/");
    }


}
