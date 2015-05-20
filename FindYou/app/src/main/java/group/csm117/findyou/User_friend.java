package group.csm117.findyou;

/**
 * Created by Mitchell on 5/19/2015.
 */
public class User_friend {
    public String id;
    public String title;
    public String img_url;


    public User_friend(String name, String photo) {
        title = name;
        img_url = photo;
    }
}
