/* 
 * Copyright (C) 2016 Adrien Guille <adrien.guille@univ-lyon2.fr>
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
package com.left8.evs.evs.edcow.event;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 * 
 * @author  Lefteris Paraskevas
 * @version 2016.03.28_0004
 */
public class SentimentEDCoWEvents implements Serializable {
    public LinkedList<SentimentEDCoWEvent> list = new LinkedList<>();
    public ObservableList<SentimentEDCoWEvent> observableList;

    public SentimentEDCoWEvents(ObservableList<SentimentEDCoWEvent> ol) {
        observableList = ol;
        observableList.stream().forEach((e) -> {
            list.add(e);
        });
    }

    public SentimentEDCoWEvents() {
        observableList = FXCollections.observableArrayList();
    }

    public void setFullList() {
        observableList.addAll(list);
    }

    public void filterList(String term) {
        if (term.length() > 0) {
            if (observableList.size() == 0)
                setFullList();
            List<Integer> indexes = new ArrayList<>();
            for (int i = observableList.size() - 1; i >= 0 ; i--) {
                if (!observableList.get(i).getTextualDescription().contains(term)) {
                    indexes.add(i);
                }
            }
            indexes.stream().forEach(observableList::remove);
        } else {
            setFullList();
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(list.size());
        for (SentimentEDCoWEvent evt : list) {
            out.writeObject(evt);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException {
        try {
            int count = in.readInt();
            list = new LinkedList<>();
            observableList = FXCollections.observableArrayList();
            for (int i = 0; i < count; i++) {
                list.add((SentimentEDCoWEvent)in.readObject());
            }
            setFullList();
        } catch (ClassNotFoundException ignored) {
            throw new IOException(ignored);
        }
    }
}