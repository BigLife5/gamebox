/*
 * GameBox
 * Copyright (C) 2019  Niklas Eicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.nikl.gamebox.exceptions.module;

/**
 * Exception for issues concerning the module cloud
 */
public class CloudModuleNotFoundException extends GameBoxCloudException {
    private static final long serialVersionUID = 1L;

    public CloudModuleNotFoundException(String message) {
        super(message);
    }

    public CloudModuleNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public CloudModuleNotFoundException() {
        super("The module cannot be found in the GameBox cloud");
    }

    public CloudModuleNotFoundException(Throwable throwable) {
        super("The module cannot be found in the GameBox cloud", throwable);
    }
}
