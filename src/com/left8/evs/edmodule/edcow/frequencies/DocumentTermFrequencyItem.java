/*
 * Copyright (C) 2016 Lefteris Paraskevas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.left8.evs.edmodule.edcow.frequencies;

import java.io.Serializable;

/**
 * Created by Farrokh on 8/27/2015.
 * Changes by Lefteris Paraskevas
 * @version 2016.01.31_1921
 */
public class DocumentTermFrequencyItem implements Serializable {
    public int doc_id;
    public int term_id;
    public int frequency;

    public DocumentTermFrequencyItem(int doc_id, int term_id, int frequency) {
        this.doc_id = doc_id;
        this.term_id = term_id;
        this.frequency = frequency;
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