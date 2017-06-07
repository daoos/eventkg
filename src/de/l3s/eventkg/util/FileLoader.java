package de.l3s.eventkg.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import de.l3s.eventkg.meta.Language;
import de.l3s.eventkg.pipeline.Config;

public class FileLoader {

	public static final String ONLINE_RESULTS_FOLDER_SUFFIX = "results/";
	public static final String ONLINE_RAW_DATA_FOLDER_SUFFIX = "raw_data/";
	public static final String ONLINE_META_FOLDER_SUFFIX = "meta/";

	public static SimpleDateFormat PARSE_DATE_FORMAT = new SimpleDateFormat("G yyyy-MM-dd");
	public static SimpleDateFormat PRINT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public static String getFileNameWithPath(FileName fileName) {
		return getPath(fileName);
	}

	public static String getFileNameWithPath(FileName fileName, Language language) {
		return getPath(fileName, language);
	}

	public static File getFile(FileName fileName, Language language) {
		return new File(getPath(fileName, language));
	}

	public static BufferedReader getReader(FileName fileName) throws FileNotFoundException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(getPath(fileName))));
		if (fileName.hasColumnNamesInFirstLine()) {
			try {
				br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return br;
	}

	public static BufferedReader getReader(FileName fileName, Language language) throws IOException {
		System.out.println(getPath(fileName, language));
		BufferedReader br = null;
		if (fileName.isGZipped())
			br = new BufferedReader(
					new InputStreamReader(new GZIPInputStream(new FileInputStream(getPath(fileName, language)))));
		else
			br = new BufferedReader(new InputStreamReader(new FileInputStream(getPath(fileName, language))));

		if (fileName.hasColumnNamesInFirstLine()) {
			try {
				br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return br;
	}

	public static PrintWriter getWriter(FileName fileName, Language language) throws FileNotFoundException {
		return new PrintWriter(getPath(fileName, language));
	}

	public static PrintWriter getWriter(FileName fileName) throws FileNotFoundException {
		return new PrintWriter(getPath(fileName));
	}

	public static PrintStream getPrintStream(FileName fileName) throws IOException {
		return new PrintStream(new FileOutputStream(getPath(fileName)));
	}

	public static String getPath(FileName fileName) {
		return getPath(fileName, null);
	}

	public static String getPath(FileName fileName, Language language) {

		// boolean local = true;
		// if (!new File(LOCAL_RESULTS_FOLDER).exists())
		// boolean local = false;

		String path = null;

		// if (local) {
		// if (fileName.isRawData())
		// path = LOCAL_RAW_DATA_FOLDER;
		// else if (fileName.isResultsData())
		// path = LOCAL_RESULTS_FOLDER;
		// else if (fileName.isMetaData())
		// path = LOCAL_META_FOLDER;
		// } else {
		// if (fileName.isRawData())
		// path = ONLINE_RAW_DATA_FOLDER;
		// else if (fileName.isResultsData())
		// path = ONLINE_RESULTS_FOLDER;
		// else if (fileName.isMetaData())
		// path = ONLINE_META_FOLDER;
		// }

		if (fileName.isRawData())
			path = Config.getValue("data_folder") + ONLINE_RAW_DATA_FOLDER_SUFFIX;
		else if (fileName.isResultsData())
			path = Config.getValue("data_folder") + ONLINE_RESULTS_FOLDER_SUFFIX;
		else if (fileName.isMetaData())
			path = Config.getValue("data_folder") + ONLINE_META_FOLDER_SUFFIX;

		if (language == null) {
			path = path + fileName.getSource().name().toLowerCase();
		} else {
			path = path + fileName.getSource().name().toLowerCase() + "/" + language.getLanguage();
		}

		if (!fileName.isFolder()) {
			String fileNameString = fileName.getFileName();
			if (language != null && fileNameString.contains("$lang$"))
				fileNameString = fileNameString.replace("$lang$", language.getLanguage().toLowerCase());

			return path + "/" + fileNameString;
		} else
			return path;
	}

	public static List<File> getFilesList(FileName folderName, Language language) {

		if (!folderName.isFolder())
			throw new IllegalArgumentException("Folder expected, file given: " + folderName.getFileName() + ".");

		File dir = new File(getPath(folderName, language));

		File[] directoryListing = dir.listFiles();
		List<File> directoryListingWithPrefix = new ArrayList<File>();

		for (File file : directoryListing) {
			if (file.getName().startsWith(folderName.getFileName())) {
				directoryListingWithPrefix.add(file);
			}
		}

		return directoryListingWithPrefix;
	}

	public static List<File> getFilesList(FileName folderName) {

		if (!folderName.isFolder())
			throw new IllegalArgumentException("Folder expected, file given: " + folderName.getFileName() + ".");

		File dir = new File(getPath(folderName));

		File[] directoryListing = dir.listFiles();
		List<File> directoryListingWithPrefix = new ArrayList<File>();

		for (File file : directoryListing) {
			if (file.getName().startsWith(folderName.getFileName())) {
				directoryListingWithPrefix.add(file);
			}
		}

		return directoryListingWithPrefix;
	}

	public static PrintStream getPrintStream(FileName fileName, Language language) throws IOException {
		return new PrintStream(new FileOutputStream(getPath(fileName, language)));
	}

	public static String readFile(File file) throws IOException {

		String path = file.getAbsolutePath();

		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}
}