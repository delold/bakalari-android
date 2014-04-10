package cz.duong.skolar.server;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by David on 10. 4. 2014.
 */
public class Users {

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

    public User getCurrentUser() {
        return new User(0, "971031r", "dfiypam4", "http://intranet.wigym.cz:6040/bakaweb/");
    }


}
