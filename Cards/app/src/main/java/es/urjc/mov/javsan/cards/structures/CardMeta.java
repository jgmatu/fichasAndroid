package es.urjc.mov.javsan.cards.structures;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import es.urjc.mov.javsan.cards.protocol.Message;

/**
 * This class implements the meta data of the card
 * that is the data more representative.
 *
 * The class know read and write itself form a file
 * or a socket.
 */
public class CardMeta {

    private final int MAXNAME = 12;

    private int id;
    private String name;
    private String description;
    private String skill;
    private String category;

    public CardMeta(int i, String n , String d , String s, String c) {
        id = i;
        name = n;
        description = d;
        skill = s;
        category = c;
    }

    public boolean isInvalid() {
        return name.equals("") || description.equals("") || skill.equals("None") ||
                category.equals("None") || !isValidChars(name) || !isValidChars(description) ||
                name.length() > MAXNAME;
    }

    private boolean isValidChars(String data) {
        String[] lines = data.split("\n");

        for (String l : lines) {
            if (!l.matches("^[A-Za-z0-9 _\\,\\.]*[A-Za-z0-9][A-Za-z0-9 _\\,\\.]*$")) {
                return false;
            }
        }
        return true;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSkill() {
        return skill;
    }

    public String getCategory() {
        return category;
    }

    public void write (OutputStream tx) throws IOException {
        Message.writeInt(tx, id);
        Message.writeString(tx, name);
        Message.writeString(tx, description);
        Message.writeString(tx, skill);
        Message.writeString(tx, category);
    }

    public void read (InputStream rx) throws IOException {
        id = Message.readInt(rx);
        name = Message.readString(rx);
        description = Message.readString(rx);
        skill = Message.readString(rx);
        category = Message.readString(rx);
    }

    @Override
    public String toString() {
        return String.format("Card\n Name : %s\n Description : %s\n Skill : %s\n Category : %s\n",
                name, description, skill, category);

    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Card)) {
            return false;
        }
        CardMeta c = (CardMeta) o;

        return  c.name.equals(name) && c.description.equals(description) &&
                c.skill.equals(skill) && c.category.equals(category);
    }

}

