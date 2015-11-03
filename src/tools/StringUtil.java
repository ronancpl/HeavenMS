/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package tools;

public class StringUtil {
	/**
	 * Gets a string padded from the left to <code>length</code> by
	 * <code>padchar</code>.
	 *
	 * @param in The input string to be padded.
	 * @param padchar The character to pad with.
	 * @param length The length to pad to.
	 * @return The padded string.
	 */
	public static String getLeftPaddedStr(String in, char padchar, int length) {
		StringBuilder builder = new StringBuilder(length);
		for (int x = in.length(); x < length; x++) {
			builder.append(padchar);
		}
		builder.append(in);
		return builder.toString();
	}

	/**
	 * Gets a string padded from the right to <code>length</code> by
	 * <code>padchar</code>.
	 *
	 * @param in The input string to be padded.
	 * @param padchar The character to pad with.
	 * @param length The length to pad to.
	 * @return The padded string.
	 */
	public static String getRightPaddedStr(String in, char padchar, int length) {
		StringBuilder builder = new StringBuilder(in);
		for (int x = in.length(); x < length; x++) {
			builder.append(padchar);
		}
		return builder.toString();
	}

	/**
	 * Joins an array of strings starting from string <code>start</code> with
	 * a space.
	 *
	 * @param arr The array of strings to join.
	 * @param start Starting from which string.
	 * @return The joined strings.
	 */
	public static String joinStringFrom(String arr[], int start) {
		return joinStringFrom(arr, start, " ");
	}

	/**
	 * Joins an array of strings starting from string <code>start</code> with
	 * <code>sep</code> as a seperator.
	 *
	 * @param arr The array of strings to join.
	 * @param start Starting from which string.
	 * @return The joined strings.
	 */
	public static String joinStringFrom(String arr[], int start, String sep) {
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < arr.length; i++) {
			builder.append(arr[i]);
			if (i != arr.length - 1) {
				builder.append(sep);
			}
		}
		return builder.toString();
	}

	/**
	 * Makes an enum name human readable (fixes spaces, capitalization, etc)
	 *
	 * @param enumName The name of the enum to neaten up.
	 * @return The human-readable enum name.
	 */
	public static String makeEnumHumanReadable(String enumName) {
		StringBuilder builder = new StringBuilder(enumName.length() + 1);
		String[] words = enumName.split("_");
		for (String word : words) {
			if (word.length() <= 2) {
				builder.append(word); // assume that it's an abbrevation
			} else {
				builder.append(word.charAt(0));
				builder.append(word.substring(1).toLowerCase());
			}
			builder.append(' ');
		}
		return builder.substring(0, enumName.length());
	}

	/**
	 * Counts the number of <code>chr</code>'s in <code>str</code>.
	 *
	 * @param str The string to check for instances of <code>chr</code>.
	 * @param chr The character to check for.
	 * @return The number of times <code>chr</code> occurs in <code>str</code>.
	 */
	public static int countCharacters(String str, char chr) {
		int ret = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == chr) {
				ret++;
			}
		}
		return ret;
	}
}