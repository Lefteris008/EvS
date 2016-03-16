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
package exception;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.03.16_1214
 */
public class EmptyDatabaseException extends Exception {
    
    public EmptyDatabaseException() {
        ///
    }
    
    /**
     * Constructor with a custom message.
     * @param message A String containing a message to be passed to the error stream.
     */
    public EmptyDatabaseException(String message) {
        super(message);
    }
}
