package th.or.nectec.twskeyboard.ldrule;

/**
 * Created by Lattapol on 6/28/16 AD.
 */
public class Word {

    private int id;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
    }
    public String getG2P() {
        return G2P;
    }
    public void setG2P(String g2p) {
        G2P = g2p;
    }
    private String word;
    private String G2P;
}
