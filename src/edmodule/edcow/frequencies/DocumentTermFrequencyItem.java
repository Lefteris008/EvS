package edmodule.edcow.frequencies;

import java.io.Serializable;

/**
 * Created by Farrokh on 8/27/2015.
 * Changes by Lefteris Paraskevas
 * @version 2015.12.21_2002_gargantua
 */
public class DocumentTermFrequencyItem implements Serializable {
    public int doc_id;
    public int term_id;
    public int frequency;
    public short short_frequency;

    public DocumentTermFrequencyItem(int doc_id, int term_id, int frequency) {
        this.doc_id = doc_id;
        this.term_id = term_id;
        this.frequency = frequency;
        this.short_frequency = 0;
    }

    @Override
    public String toString() {
        return "DocumentTermFrequencyItem{" +
            "doc_id=" + doc_id +
            ", term_id=" + term_id +
            ", frequency=" + frequency +
            '}';
    }
}