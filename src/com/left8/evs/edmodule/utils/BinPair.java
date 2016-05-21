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
package com.left8.evs.edmodule.utils;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.31_1921
 */
public class BinPair<B,V> {
    private final B bin;
    private final V value;

    public BinPair(B bin, V value) {
        this.bin = bin;
        this.value = value;
    }

    public B getBin() { return bin; }
    public V getValue() { return value; }

    @Override
    public int hashCode() { return bin.hashCode() ^ value.hashCode(); }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof BinPair)) {
          return false;
      }
      BinPair pairo = (BinPair) o;
      return this.bin.equals(pairo.getBin()) &&
             this.value.equals(pairo.getValue());
    }
}
