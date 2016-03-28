/*
 * Created on 28.03.2016
 * Autor: Markov.
 */
package de.markov.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main {

	private static final byte	Ue_WIN	= (byte) 0xdc;
	private static final byte	ue_WIN	= (byte) 0xfc;
	private static final byte	Oe_WIN	= (byte) 0xd6;
	private static final byte	oe_WIN	= (byte) 0xf6;
	private static final byte	Ae_WIN	= (byte) 0xc4;
	private static final byte	ae_WIN	= (byte) 0xe4;
	private static final byte	ss_WIN	= (byte) 0xdf;

	private static Set<String>	extensions;
	private static FileFilter	filesToModifyFilter;
	private static FileFilter	subdirectoryFilter;

	public static void main(String[] args) {
		String path = args[0];
		extensions = new HashSet<>(Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
		filesToModifyFilter = new FileFilterImplementation(extensions);
		subdirectoryFilter = new DirectoryFilter();
		File directory = new File(path);
		modifyFilesRecusivly(directory);
	}

	private static void modifyFilesRecusivly(File rootDirectory) {
		File[] filesToModify = rootDirectory.listFiles(filesToModifyFilter);
		for (File fileToModify : filesToModify) {
			modifyFile(fileToModify);
		}

		File[] subdirectories = rootDirectory.listFiles(subdirectoryFilter);
		for (File directory : subdirectories) {
			modifyFilesRecusivly(directory);
		}
	}

	private static void modifyFile(File fileToModify) {

		try {
			FileInputStream fis = new FileInputStream(fileToModify);
			long bytesInFile = fis.getChannel().size();
			byte[] fileBytes = new byte[(int) bytesInFile];
			fis.read(fileBytes);
			fis.close();

			int umlautsInFile = countUmlauts(fileBytes);
			byte[] bytesToWrite = new byte[fileBytes.length + umlautsInFile];

			for (int readIndex = 0, writeIndex = 0; readIndex < fileBytes.length; readIndex++, writeIndex++) {
				byte b = fileBytes[readIndex];
				switch (b) {
				case Ue_WIN:
					bytesToWrite[writeIndex] = (byte) 0xc3;
					writeIndex++;
					bytesToWrite[writeIndex] = (byte) 0x9c;
					break;
				case ue_WIN:
					bytesToWrite[writeIndex] = (byte) 0xc3;
					writeIndex++;
					bytesToWrite[writeIndex] = (byte) 0xbc;
					break;
				case Oe_WIN:
					bytesToWrite[writeIndex] = (byte) 0xc3;
					writeIndex++;
					bytesToWrite[writeIndex] = (byte) 0x96;
					break;
				case oe_WIN:
					bytesToWrite[writeIndex] = (byte) 0xc3;
					writeIndex++;
					bytesToWrite[writeIndex] = (byte) 0xb6;
					break;
				case Ae_WIN:
					bytesToWrite[writeIndex] = (byte) 0xc3;
					writeIndex++;
					bytesToWrite[writeIndex] = (byte) 0x84;
					break;
				case ae_WIN:
					bytesToWrite[writeIndex] = (byte) 0xc3;
					writeIndex++;
					bytesToWrite[writeIndex] = (byte) 0xa4;
					break;
				case ss_WIN:
					bytesToWrite[writeIndex] = (byte) 0xc3;
					writeIndex++;
					bytesToWrite[writeIndex] = (byte) 0x9f;
					break;
				default:
					bytesToWrite[readIndex] = b;
				}
			}
			FileOutputStream fos = new FileOutputStream(fileToModify);
			fos.write(bytesToWrite);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static int countUmlauts(byte[] fileBytes) {
		int count = 0;
		for (byte b : fileBytes) {
			switch (b) {
			case Ae_WIN:
			case ae_WIN:
			case Oe_WIN:
			case oe_WIN:
			case Ue_WIN:
			case ue_WIN:
			case ss_WIN:
				count++;
			}
		}
		return count;
	}

	private static final class FileFilterImplementation implements FileFilter {

		private Set<String> extensions;

		public FileFilterImplementation(Set<String> extensions) {
			this.extensions = extensions;
		}

		public boolean accept(File pathname) {
			if (!pathname.exists())
				return false;
			if (!pathname.isFile())
				return false;

			String fileName = pathname.getName();
			int extensionIndex = fileName.lastIndexOf('.');
			if (extensionIndex < 1)
				return false;
			String extension = fileName.substring(extensionIndex + 1);

			if (extensions.contains(extension))
				return true;
			return false;
		}
	}

	public static class DirectoryFilter implements FileFilter {

		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	}
}
